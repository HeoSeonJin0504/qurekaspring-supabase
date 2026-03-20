package com.qureka.domain.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SaveQuestionRequest {

    @NotNull(message = "사용자 ID가 필요합니다.")
    private Long userId;

    @NotBlank(message = "파일명이 필요합니다.")
    private String fileName;

    private String questionName = "Untitled Question";

    @NotBlank(message = "문제 타입이 필요합니다.")
    private String questionType;

    @NotNull(message = "문제 데이터가 필요합니다.")
    private Map<String, Object> questionData;
}
