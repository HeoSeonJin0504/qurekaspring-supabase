package com.qureka.domain.summary;

import com.qureka.domain.summary.dto.SaveSummaryRequest;
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
public class SummaryService {

    private final SummaryRepository summaryRepository;
    private final UserRepository    userRepository;

    @Transactional
    public UserSummary save(SaveSummaryRequest req) {
        User user = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return summaryRepository.save(UserSummary.builder()
                .user(user).fileName(req.getFileName())
                .summaryName(req.getSummaryName() != null ? req.getSummaryName() : "Untitled Summary")
                .summaryType(parseSummaryType(req.getSummaryType()))
                .summaryText(req.getSummaryText()).build());
    }

    @Transactional(readOnly = true)
    public List<UserSummary> getByUserId(Long userId) {
        return summaryRepository.findByUserUserIndexOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public UserSummary getById(Long id) {
        return summaryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUMMARY_NOT_FOUND));
    }

    @Transactional
    public UserSummary updateName(Long id, Long requesterId, String newName) {
        if (newName == null || newName.isBlank())
            throw new CustomException(ErrorCode.INVALID_INPUT, "요약 이름을 입력해주세요.");
        UserSummary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUMMARY_NOT_FOUND));
        if (!summary.getUser().getUserIndex().equals(requesterId))
            throw new CustomException(ErrorCode.SUMMARY_FORBIDDEN);
        summary.setSummaryName(newName.trim());
        return summaryRepository.save(summary);
    }

    @Transactional
    public void delete(Long id, Long requesterId) {
        UserSummary summary = summaryRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.SUMMARY_NOT_FOUND));
        if (!summary.getUser().getUserIndex().equals(requesterId))
            throw new CustomException(ErrorCode.SUMMARY_FORBIDDEN);
        summaryRepository.delete(summary);
    }

    @Transactional(readOnly = true)
    public List<UserSummary> search(Long userId, String query, String typeStr) {
        SummaryType type = (typeStr != null && !typeStr.isBlank()) ? parseSummaryType(typeStr) : null;
        return summaryRepository.searchByUserId(userId, (query != null && !query.isBlank()) ? query : null, type);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getMeta(Long userId) {
        return summaryRepository.findMetaByUserId(userId);
    }

    private SummaryType parseSummaryType(String type) {
        return switch (type) {
            case "기본 요약", "basic"       -> SummaryType.basic;
            case "핵심 내용", "key_points"  -> SummaryType.key_points;
            case "주제별 정리", "topic"     -> SummaryType.topic;
            case "개요 작성", "outline"     -> SummaryType.outline;
            case "키워드 추출", "keywords"  -> SummaryType.keywords;
            default -> {
                try { yield SummaryType.valueOf(type); }
                catch (IllegalArgumentException e) {
                    throw new CustomException(ErrorCode.INVALID_INPUT, "지원하지 않는 요약 타입입니다: " + type);
                }
            }
        };
    }
}
