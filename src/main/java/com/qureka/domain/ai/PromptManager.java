package com.qureka.domain.ai;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptManager {

    private static final Map<String, String> SUMMARY_SYSTEM = Map.of(
            "내용 요약_기본 요약",
            "당신은 학습 콘텐츠 요약 전문가입니다. 제공된 문서를 학습자가 이해하기 쉽도록 핵심 내용을 중심으로 요약해 주세요. 학습 수준과 전공 분야를 고려하여 적절한 난이도와 용어를 사용해 주세요.",
            "내용 요약_핵심 내용",
            "당신은 핵심 내용 추출 전문가입니다. 제공된 문서에서 가장 중요한 핵심 포인트만을 bullet point 형식으로 추출해 주세요.",
            "내용 요약_주제별 정리",
            "당신은 주제별 내용 정리 전문가입니다. 제공된 문서의 내용을 주제별로 분류하고 체계적으로 정리해 주세요.",
            "내용 요약_개요 작성",
            "당신은 학술 문서 개요 작성 전문가입니다. 제공된 문서의 구조적 개요(outline)를 대주제, 중주제, 소주제의 계층적 구조로 작성해 주세요.",
            "내용 요약_키워드 추출",
            "당신은 학습 키워드 추출 전문가입니다. 제공된 문서에서 핵심 키워드와 개념을 추출하고 각 키워드에 대한 간결한 정의를 제공해 주세요."
    );

    private static final Map<String, String> QUESTION_SYSTEM = Map.of(
            "문제 생성_n지 선다형",
            "당신은 객관식 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"문제\",\"options\":[\"선택지1\",\"선택지2\",\"선택지3\",\"선택지4\"],\"answer\":\"정답\",\"explanation\":\"해설\"}]}",
            "문제 생성_순서 배열형",
            "당신은 순서 배열 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"문제\",\"items\":[\"항목1\",\"항목2\"],\"answer\":[\"순서1\",\"순서2\"],\"explanation\":\"해설\"}]}",
            "문제 생성_참거짓형",
            "당신은 참/거짓 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"문제\",\"answer\":true,\"explanation\":\"해설\"}]}",
            "문제 생성_빈칸 채우기형",
            "당신은 빈칸 채우기 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"빈칸이 포함된 문장(빈칸은 ___)\",\"answer\":\"정답\",\"explanation\":\"해설\"}]}",
            "문제 생성_단답형",
            "당신은 단답형 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"문제\",\"answer\":\"정답\",\"explanation\":\"해설\"}]}",
            "문제 생성_서술형",
            "당신은 서술형 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n{\"questions\":[{\"question\":\"문제\",\"sampleAnswer\":\"모범답안\",\"keyPoints\":[\"기준1\"],\"explanation\":\"해설\"}]}"
    );

    private static final String SUMMARY_USER_TMPL  = "학습 수준: %s\n전공 분야: %s\n\n다음 문서를 요약해 주세요:\n\n%s";
    private static final String QUESTION_USER_TMPL = "학습 수준: %s\n전공 분야: %s\n생성할 문제 수: %d개\n\n다음 학습 내용을 바탕으로 문제를 생성해 주세요:\n\n%s";

    public PromptPair getSummaryPrompt(String key, String content, String level, String field) {
        String system = SUMMARY_SYSTEM.get(key);
        if (system == null) return null;
        return new PromptPair(system, String.format(SUMMARY_USER_TMPL, level, field, content));
    }

    public PromptPair getQuestionPrompt(String key, String content, String level, String field, int count) {
        String system = QUESTION_SYSTEM.get(key);
        if (system == null) return null;
        return new PromptPair(system, String.format(QUESTION_USER_TMPL, level, field, count, content));
    }

    public String normalizeSummaryTypeKey(String type) {
        if (type == null) return "내용 요약_기본 요약";
        return type.contains("_") ? type : "내용 요약_" + type;
    }

    private static final Map<String, String> QUESTION_TYPE_MAP = Map.ofEntries(
            Map.entry("n지선다",           "문제 생성_n지 선다형"),
            Map.entry("n지선다형",          "문제 생성_n지 선다형"),
            Map.entry("multiple_choice",   "문제 생성_n지 선다형"),
            Map.entry("순서배열",           "문제 생성_순서 배열형"),
            Map.entry("순서배열형",          "문제 생성_순서 배열형"),
            Map.entry("sequence",          "문제 생성_순서 배열형"),
            Map.entry("참거짓",             "문제 생성_참거짓형"),
            Map.entry("참거짓형",            "문제 생성_참거짓형"),
            Map.entry("true_false",        "문제 생성_참거짓형"),
            Map.entry("빈칸채우기",          "문제 생성_빈칸 채우기형"),
            Map.entry("fill_in_the_blank", "문제 생성_빈칸 채우기형"),
            Map.entry("단답",              "문제 생성_단답형"),
            Map.entry("단답형",             "문제 생성_단답형"),
            Map.entry("short_answer",      "문제 생성_단답형"),
            Map.entry("서술",              "문제 생성_서술형"),
            Map.entry("서술형",             "문제 생성_서술형"),
            Map.entry("descriptive",       "문제 생성_서술형")
    );

    public String normalizeQuestionTypeKey(String type) {
        return QUESTION_TYPE_MAP.get(type);
    }

    public record PromptPair(String system, String user) {}
}
