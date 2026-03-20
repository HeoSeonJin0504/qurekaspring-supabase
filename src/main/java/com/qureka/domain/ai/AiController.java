package com.qureka.domain.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/summarize")
    public ResponseEntity<Map<String, Object>> summarize(
            @RequestPart(value = "file",        required = false) MultipartFile file,
            @RequestPart(value = "summaryType", required = false) String summaryType,
            @RequestPart(value = "level",       required = false) String level,
            @RequestPart(value = "field",       required = false) String field
    ) throws IOException {
        if (file == null || file.isEmpty())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "파일이 필요합니다. (PDF 또는 PPTX)"));

        AiService.SummarizeResult result = aiService.summarize(
                file,
                summaryType != null ? summaryType : "기본 요약",
                level       != null ? level       : "비전공자",
                field       != null ? field       : "일반"
        );
        return ResponseEntity.ok(Map.of(
                "success",     true,
                "fileName",    result.fileName(),
                "summaryType", result.summaryType(),
                "level",       result.level(),
                "field",       result.field(),
                "summary",     result.summary()
        ));
    }

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateQuestions(@RequestBody Map<String, Object> body) {
        String summaryText  = (String) body.getOrDefault("summaryText",  "");
        String questionType = (String) body.getOrDefault("questionType", "n지선다");
        int    count        = body.get("questionCount") != null
                ? Integer.parseInt(String.valueOf(body.get("questionCount"))) : 5;
        String level        = (String) body.getOrDefault("level", "비전공자");
        String field        = (String) body.getOrDefault("field", "일반");

        AiService.QuestionResult result =
                aiService.generateQuestions(summaryText, questionType, count, level, field);

        return ResponseEntity.ok(Map.of(
                "success",      true,
                "questionType", result.questionType(),
                "count",        result.count(),
                "level",        result.level(),
                "field",        result.field(),
                "questions",    result.questions(),
                "parsed",       result.parsed()
        ));
    }
}