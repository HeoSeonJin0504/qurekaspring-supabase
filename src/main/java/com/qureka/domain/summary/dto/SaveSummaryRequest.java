package com.qureka.domain.summary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class SaveSummaryRequest {

    @NotNull(message = "사용자 ID가 필요합니다.")
    private Long userId;

    @NotBlank(message = "파일명이 필요합니다.")
    private String fileName;

    private String summaryName = "Untitled Summary";

    @NotBlank(message = "요약 타입이 필요합니다.")
    private String summaryType;

    @NotBlank(message = "요약 내용이 필요합니다.")
    private String summaryText;
}
