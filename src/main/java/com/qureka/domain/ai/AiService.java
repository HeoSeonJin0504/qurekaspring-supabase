package com.qureka.domain.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private static final int MAX_TOKENS_SUMMARY  = 20_000;
    private static final int MAX_TOKENS_QUESTION = 15_000;

    private final ChatClient         chatClient;
    private final FileExtractService fileExtractService;
    private final PromptManager      promptManager;
    private final ObjectMapper       objectMapper;

    public SummarizeResult summarize(MultipartFile file, String summaryType, String level, String field) throws IOException {
        String originalName  = resolveFilename(file);
        String extractedText = extractText(file, originalName);

        if (extractedText == null || extractedText.trim().length() < 50)
            throw new CustomException(ErrorCode.FILE_EXTRACT_FAILED);

        String safeContent    = fileExtractService.truncateByTokens(extractedText, MAX_TOKENS_SUMMARY);
        String summaryTypeKey = promptManager.normalizeSummaryTypeKey(summaryType);
        PromptManager.PromptPair prompt = promptManager.getSummaryPrompt(summaryTypeKey, safeContent, level, field);

        if (prompt == null)
            throw new CustomException(ErrorCode.INVALID_SUMMARY_TYPE, "지원하지 않는 요약 타입입니다: " + summaryType);

        log.info("요약 생성 - 타입:{}, 레벨:{}, 분야:{}", summaryTypeKey, level, field);
        String result = callChatGpt(prompt.system(), prompt.user());
        log.info("요약 완료 - 파일:{}", originalName);

        return new SummarizeResult(originalName, summaryType, level, field, result);
    }

    public QuestionResult generateQuestions(String summaryText, String questionType, int questionCount, String level, String field) {
        if (summaryText == null || summaryText.trim().length() < 30)
            throw new CustomException(ErrorCode.SUMMARY_TEXT_TOO_SHORT);

        String questionTypeKey = promptManager.normalizeQuestionTypeKey(questionType);
        if (questionTypeKey == null)
            throw new CustomException(ErrorCode.INVALID_QUESTION_TYPE, "유효하지 않은 문제 타입입니다: " + questionType);

        int count = Math.min(Math.max(questionCount, 1), 20);
        String safeContent = fileExtractService.truncateByTokens(summaryText, MAX_TOKENS_QUESTION);
        PromptManager.PromptPair prompt = promptManager.getQuestionPrompt(questionTypeKey, safeContent, level, field, count);

        if (prompt == null)
            throw new CustomException(ErrorCode.INVALID_QUESTION_TYPE, "지원하지 않는 문제 타입입니다: " + questionType);

        log.info("문제 생성 - 타입:{}, 개수:{}, 레벨:{}", questionType, count, level);
        String rawResult = callChatGpt(prompt.system(), prompt.user());
        List<Object> questions = parseQuestionsJson(rawResult);
        boolean parsed = questions != null;

        if (!parsed) {
            log.warn("JSON 파싱 실패 - 원본 반환");
            return new QuestionResult(questionType, count, level, field, rawResult, false);
        }
        log.info("문제 생성 완료 - {}개", questions.size());
        return new QuestionResult(questionType, count, level, field, questions, true);
    }

    private String callChatGpt(String systemMessage, String userMessage) {
        try {
            return chatClient.prompt().system(systemMessage).user(userMessage).call().content();
        } catch (Exception e) {
            log.error("OpenAI 호출 오류: {}", e.getMessage());
            throw new CustomException(ErrorCode.AI_SERVICE_ERROR, e.getMessage());
        }
    }

    private String resolveFilename(MultipartFile file) {
        String name = file.getOriginalFilename();
        if (name == null) return "unknown";
        try {
            return new String(name.getBytes("ISO-8859-1"), "UTF-8");
        } catch (Exception e) {
            return name;
        }
    }

    private String extractText(MultipartFile file, String name) throws IOException {
        String mime  = file.getContentType() != null ? file.getContentType().toLowerCase() : "";
        String lower = name.toLowerCase();
        if (mime.contains("pdf") || lower.endsWith(".pdf"))
            return fileExtractService.extractFromPdf(file.getBytes());
        if (mime.contains("presentationml") || lower.endsWith(".pptx"))
            return fileExtractService.extractFromPptx(file.getBytes());
        throw new CustomException(ErrorCode.UNSUPPORTED_FILE_TYPE);
    }

    @SuppressWarnings("unchecked")
    private List<Object> parseQuestionsJson(String raw) {
        try {
            String cleaned = raw.replaceAll("```json\\n?|\\n?```", "").trim();
            Object parsed  = objectMapper.readValue(cleaned, Object.class);
            if (parsed instanceof List<?> list) return (List<Object>) list;
            if (parsed instanceof Map<?, ?> map) {
                Object q = map.get("questions");
                if (q instanceof List<?> qList) return (List<Object>) qList;
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public record SummarizeResult(String fileName, String summaryType, String level, String field, String summary) {}
    public record QuestionResult(String questionType, int count, String level, String field, Object questions, boolean parsed) {}
}
