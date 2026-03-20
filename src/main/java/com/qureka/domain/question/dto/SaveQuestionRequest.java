package com.qureka.domain.question.dto;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SaveQuestionRequest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @NotNull(message = "사용자 ID가 필요합니다.")
    private Long userId;

    @NotBlank(message = "파일명이 필요합니다.")
    private String fileName;

    private String questionName = "Untitled Question";

    @NotBlank(message = "문제 타입이 필요합니다.")
    private String questionType;

    // questionData는 내부 저장용 (직접 set 가능)
    private Map<String, Object> questionData;

    /**
     * 프론트가 questionText(JSON 문자열)로 전송 시 호출
     */
    public void setQuestionText(String questionText) {
        if (questionText == null || questionText.isBlank()) return;
        // Node.js: JSON.stringify({ question_text: questionText })
        this.questionData = Map.of("question_text", questionText);
    }
}