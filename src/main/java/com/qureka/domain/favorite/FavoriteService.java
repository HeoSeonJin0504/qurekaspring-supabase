package com.qureka.domain.favorite;

import com.qureka.domain.question.QuestionRepository;
import com.qureka.domain.question.UserQuestion;
import com.qureka.domain.user.User;
import com.qureka.domain.user.UserRepository;
import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteService {

    private static final String DEFAULT_FOLDER = "기본 폴더";

    private final FavoriteFolderRepository   folderRepository;
    private final FavoriteQuestionRepository questionRepository;
    private final UserRepository             userRepository;
    private final QuestionRepository         userQuestionRepository;

    @Transactional
    public List<FavoriteFolder> getFolders(Long userId) {
        List<FavoriteFolder> folders = folderRepository.findByUserUserIndexOrderByCreatedAtAsc(userId);
        boolean hasDefault = folders.stream().anyMatch(f -> DEFAULT_FOLDER.equals(f.getFolderName()));
        if (folders.isEmpty() || !hasDefault) {
            getOrCreateDefaultFolder(userId);
            folders = folderRepository.findByUserUserIndexOrderByCreatedAtAsc(userId);
        }
        return folders;
    }

    @Transactional
    public FavoriteFolder getOrCreateDefaultFolder(Long userId) {
        return folderRepository.findByUserUserIndexAndFolderName(userId, DEFAULT_FOLDER)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                    return folderRepository.save(FavoriteFolder.builder()
                            .user(user).folderName(DEFAULT_FOLDER).description("기본 즐겨찾기 폴더").build());
                });
    }

    @Transactional
    public FavoriteFolder createFolder(Long userId, String folderName, String description) {
        if (folderRepository.existsByUserUserIndexAndFolderName(userId, folderName))
            throw new CustomException(ErrorCode.FOLDER_ALREADY_EXISTS);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return folderRepository.save(FavoriteFolder.builder()
                .user(user).folderName(folderName).description(description).build());
    }

    @Transactional
    public void deleteFolder(Long folderId, Long userId) {
        FavoriteFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND, "폴더를 찾을 수 없습니다."));
        if (DEFAULT_FOLDER.equals(folder.getFolderName()))
            throw new CustomException(ErrorCode.FOLDER_DEFAULT_PROTECTED);
        if (!folder.getUser().getUserIndex().equals(userId))
            throw new CustomException(ErrorCode.FORBIDDEN);
        folderRepository.delete(folder);
    }

    @Transactional
    public FavoriteQuestion addQuestion(Long userId, Long folderId, Long questionId, Short questionIndex) {
        if (questionRepository.existsByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
                userId, questionId, questionIndex))
            throw new CustomException(ErrorCode.FAVORITE_ALREADY_EXISTS);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        FavoriteFolder folder = folderId != null
                ? folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND, "존재하지 않는 폴더입니다."))
                : getOrCreateDefaultFolder(userId);
        UserQuestion question = userQuestionRepository.findById(questionId)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND, "존재하지 않는 문제입니다."));

        return questionRepository.save(FavoriteQuestion.builder()
                .user(user).folder(folder).question(question)
                .questionIndex(questionIndex != null ? questionIndex : 0).build());
    }

    @Transactional
    public void removeQuestion(Long favoriteId, Long userId) {
        FavoriteQuestion fq = questionRepository.findById(favoriteId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND, "즐겨찾기 항목을 찾을 수 없습니다."));
        if (!fq.getUser().getUserIndex().equals(userId))
            throw new CustomException(ErrorCode.FORBIDDEN);
        questionRepository.delete(fq);
    }

    @Transactional(readOnly = true)
    public Optional<FavoriteQuestion> checkQuestion(Long userId, Long questionId, Short questionIndex) {
        return questionRepository.findByUserUserIndexAndQuestionSelectionIdAndQuestionIndex(
                userId, questionId, questionIndex != null ? questionIndex : 0);
    }

    @Transactional(readOnly = true)
    public List<FavoriteQuestion> getAllQuestions(Long userId) {
        return questionRepository.findByUserUserIndexOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public FolderWithQuestions getQuestionsByFolder(Long folderId, Long userId) {
        FavoriteFolder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLDER_NOT_FOUND, "폴더를 찾을 수 없습니다."));
        return new FolderWithQuestions(folder,
                questionRepository.findByFolderFolderIdAndUserUserIndex(folderId, userId));
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> checkMultipleQuestions(Long userId, List<Map<String, Object>> questions) {
        // null인 questionId 필터링 후 파싱
        List<Long> ids = questions.stream()
                .map(q -> q.get("questionId"))
                .filter(Objects::nonNull)
                .map(v -> {
                    try { return Long.parseLong(String.valueOf(v)); }
                    catch (NumberFormatException e) { return null; }
                })
                .filter(Objects::nonNull)
                .toList();

        if (ids.isEmpty()) return List.of();

        List<FavoriteQuestion> favorites = questionRepository.findByUserIndexAndQuestionIds(userId, ids);

        return questions.stream().map(q -> {
            Object qIdRaw = q.get("questionId");
            // null 또는 파싱 불가 항목은 즐겨찾기 아님으로 처리
            if (qIdRaw == null) {
                Map<String, Object> status = new HashMap<>();
                status.put("questionId", null);
                status.put("questionIndex", (short) 0);
                status.put("isFavorite", false);
                return status;
            }
            Long qId;
            try { qId = Long.parseLong(String.valueOf(qIdRaw)); }
            catch (NumberFormatException e) {
                Map<String, Object> status = new HashMap<>();
                status.put("questionId", qIdRaw);
                status.put("questionIndex", (short) 0);
                status.put("isFavorite", false);
                return status;
            }

            Short qIdx = q.get("questionIndex") != null
                    ? Short.parseShort(String.valueOf(q.get("questionIndex"))) : (short) 0;

            Optional<FavoriteQuestion> match = favorites.stream()
                    .filter(f -> f.getQuestion().getSelectionId().equals(qId)
                            && f.getQuestionIndex().equals(qIdx))
                    .findFirst();

            Map<String, Object> status = new HashMap<>();
            status.put("questionId", qId);
            status.put("questionIndex", qIdx);
            status.put("isFavorite", match.isPresent());
            match.ifPresent(fq -> {
                status.put("favoriteId", fq.getFavoriteId());
                status.put("folderId",   fq.getFolder().getFolderId());
            });
            return status;
        }).toList();
    }

    public record FolderWithQuestions(FavoriteFolder folder, List<FavoriteQuestion> questions) {}
}