package com.qureka.domain.question;

import com.qureka.domain.question.dto.SaveQuestionRequest;
import com.qureka.domain.user.User;
import com.qureka.domain.user.UserRepository;
import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final UserRepository     userRepository;

    @Transactional
    public UserQuestion save(SaveQuestionRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return questionRepository.save(UserQuestion.builder()
                .user(user).fileName(req.getFileName())
                .questionName(req.getQuestionName() != null ? req.getQuestionName() : "Untitled Question")
                .questionType(parseQuestionType(req.getQuestionType()))
                .questionData(req.getQuestionData()).build());
    }

    @Transactional(readOnly = true)
    public List<UserQuestion> getByUserId(Long userId) {
        return questionRepository.findByUserUserIndexOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public UserQuestion getById(Long id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
    }

    @Transactional
    public UserQuestion updateName(Long id, Long requesterId, String newName) {
        if (newName == null || newName.isBlank())
            throw new CustomException(ErrorCode.INVALID_INPUT, "문제 이름을 입력해주세요.");
        UserQuestion q = questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
        if (!q.getUser().getUserIndex().equals(requesterId))
            throw new CustomException(ErrorCode.QUESTION_FORBIDDEN);
        q.setQuestionName(newName.trim());
        return questionRepository.save(q);
    }

    @Transactional
    public void delete(Long id, Long requesterId) {
        UserQuestion q = questionRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.QUESTION_NOT_FOUND));
        if (!q.getUser().getUserIndex().equals(requesterId))
            throw new CustomException(ErrorCode.QUESTION_FORBIDDEN);
        questionRepository.delete(q);
    }

    @Transactional(readOnly = true)
    public List<UserQuestion> search(Long userId, String query, String typeStr) {
        QuestionType type = (typeStr != null && !typeStr.isBlank()) ? parseQuestionType(typeStr) : null;
        return questionRepository.searchByUserId(userId,
                (query != null && !query.isBlank()) ? query : null, type);
    }

    private QuestionType parseQuestionType(String type) {
        return switch (type) {
            case "n지선다", "multiple_choice"     -> QuestionType.multiple_choice;
            case "순서배열", "sequence"            -> QuestionType.sequence;
            case "빈칸채우기", "fill_in_the_blank" -> QuestionType.fill_in_the_blank;
            case "참거짓", "true_false"            -> QuestionType.true_false;
            case "단답", "단답형", "short_answer"   -> QuestionType.short_answer;
            case "서술", "서술형", "descriptive"    -> QuestionType.descriptive;
            default -> {
                try { yield QuestionType.valueOf(type); }
                catch (IllegalArgumentException e) {
                    throw new CustomException(ErrorCode.INVALID_QUESTION_TYPE,
                            "유효하지 않은 문제 타입입니다: " + type);
                }
            }
        };
    }
}
