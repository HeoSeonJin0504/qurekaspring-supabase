package com.qureka.domain.ai;

import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class PromptManager {

    // ── 요약 시스템 프롬프트 ─────────────────────────────────────────────
    private static final Map<String, String> SUMMARY_SYSTEM = Map.of(
            "내용 요약_기본 요약",
            "당신은 학습 콘텐츠 요약 전문가입니다. 제공된 문서를 학습자가 이해하기 쉽도록 핵심 내용을 중심으로 요약해 주세요. 학습 수준과 전공 분야를 고려하여 적절한 난이도와 용어를 사용해 주세요.",

            "내용 요약_핵심 요약",
            "당신은 핵심 내용 추출 전문가입니다. 제공된 문서에서 가장 중요한 핵심 포인트만을 bullet point 형식으로 추출해 주세요.",

            "내용 요약_주제 요약",
            "당신은 주제별 내용 정리 전문가입니다. 제공된 문서의 내용을 주제별로 분류하고 체계적으로 정리해 주세요.",

            "내용 요약_목차 요약",
            "당신은 학술 문서 개요 작성 전문가입니다. 제공된 문서의 구조적 개요(outline)를 대주제, 중주제, 소주제의 계층적 구조로 작성해 주세요.",

            "내용 요약_키워드 요약",
            "당신은 학습 키워드 추출 전문가입니다. 제공된 문서에서 핵심 키워드와 개념을 추출하고 각 키워드에 대한 간결한 정의를 제공해 주세요."
    );

    // ── 문제 시스템 프롬프트 ─────────────────────────────────────────────
    private static final Map<String, String> QUESTION_SYSTEM = Map.of(
            "문제 생성_n지 선다형",
            "당신은 객관식 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"문제\",\"options\":[{\"id\":\"A\",\"text\":\"선택지1\"},{\"id\":\"B\",\"text\":\"선택지2\"},{\"id\":\"C\",\"text\":\"선택지3\"},{\"id\":\"D\",\"text\":\"선택지4\"}],\"correct_answer\":\"A\",\"explanation\":\"해설\"}]}",

            "문제 생성_순서 배열형",
            "당신은 순서 배열 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"문제\",\"items\":[{\"id\":\"A\",\"text\":\"항목1\"},{\"id\":\"B\",\"text\":\"항목2\"},{\"id\":\"C\",\"text\":\"항목3\"}],\"correct_sequence\":[\"B\",\"A\",\"C\"],\"explanation\":\"해설\"}]}",

            "문제 생성_참거짓형",
            "당신은 참/거짓 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"문제\",\"correct_answer\":true,\"explanation\":\"해설\"}]}",

            "문제 생성_빈칸 채우기형",
            "당신은 빈칸 채우기 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"빈칸이 포함된 문장 (빈칸은 ___ 로 표시)\",\"options\":[{\"id\":\"A\",\"text\":\"보기1\"},{\"id\":\"B\",\"text\":\"보기2\"},{\"id\":\"C\",\"text\":\"보기3\"}],\"correct_answers\":[\"A\"],\"explanation\":\"해설\"}]}",

            "문제 생성_단답형",
            "당신은 단답형 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"문제\",\"correct_answer\":\"정답\",\"alternative_answers\":[\"대체답안1\"],\"explanation\":\"해설\"}]}",

            "문제 생성_서술형",
            "당신은 서술형 문제 출제 전문가입니다. 반드시 다음 JSON 형식으로만 응답하세요:\n" +
                    "{\"questions\":[{\"question_text\":\"문제\",\"answer_keywords\":[\"키워드1\",\"키워드2\"],\"model_answer\":\"모범답안\",\"explanation\":\"해설\"}]}"
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

    /** 요약 타입 키 정규화 — 프론트가 보내는 값을 SUMMARY_SYSTEM 키로 변환 */
    public String normalizeSummaryTypeKey(String type) {
        if (type == null) return "내용 요약_기본 요약";
        // 이미 올바른 키 형식이면 그대로
        if (SUMMARY_SYSTEM.containsKey(type)) return type;
        // "내용 요약_" 없이 들어온 경우 붙여주기
        String withPrefix = "내용 요약_" + type;
        if (SUMMARY_SYSTEM.containsKey(withPrefix)) return withPrefix;
        // 구버전 키 호환 매핑
        return switch (type) {
            case "기본 요약", "basic"      -> "내용 요약_기본 요약";
            case "핵심 내용", "핵심 요약"   -> "내용 요약_핵심 요약";
            case "주제별 정리", "주제 요약" -> "내용 요약_주제 요약";
            case "개요 작성", "목차 요약"   -> "내용 요약_목차 요약";
            case "키워드 추출", "키워드 요약"-> "내용 요약_키워드 요약";
            default -> "내용 요약_기본 요약";
        };
    }

    // 문제 타입 키 정규화 — 프론트가 보내는 모든 형태 포함
    private static final Map<String, String> QUESTION_TYPE_MAP = Map.ofEntries(
            // 프론트 AiQuestionPromptKey_Korean (띄어쓰기 포함) ← 이것이 핵심 추가
            Map.entry("n지 선다형",        "문제 생성_n지 선다형"),
            Map.entry("순서 배열형",        "문제 생성_순서 배열형"),
            Map.entry("빈칸 채우기형",      "문제 생성_빈칸 채우기형"),
            Map.entry("참거짓형",           "문제 생성_참거짓형"),
            Map.entry("단답형",             "문제 생성_단답형"),
            Map.entry("서술형",             "문제 생성_서술형"),
            // 띄어쓰기 없는 기존 키 (DB 저장값 등 하위 호환)
            Map.entry("n지선다",            "문제 생성_n지 선다형"),
            Map.entry("n지선다형",           "문제 생성_n지 선다형"),
            Map.entry("multiple_choice",    "문제 생성_n지 선다형"),
            Map.entry("순서배열",            "문제 생성_순서 배열형"),
            Map.entry("순서배열형",           "문제 생성_순서 배열형"),
            Map.entry("sequence",           "문제 생성_순서 배열형"),
            Map.entry("참거짓",              "문제 생성_참거짓형"),
            Map.entry("true_false",         "문제 생성_참거짓형"),
            Map.entry("빈칸채우기",           "문제 생성_빈칸 채우기형"),
            Map.entry("fill_in_the_blank",  "문제 생성_빈칸 채우기형"),
            Map.entry("단답",               "문제 생성_단답형"),
            Map.entry("short_answer",       "문제 생성_단답형"),
            Map.entry("서술",               "문제 생성_서술형"),
            Map.entry("descriptive",        "문제 생성_서술형")
    );

    public String normalizeQuestionTypeKey(String type) {
        return QUESTION_TYPE_MAP.get(type);
    }

    public record PromptPair(String system, String user) {}
}