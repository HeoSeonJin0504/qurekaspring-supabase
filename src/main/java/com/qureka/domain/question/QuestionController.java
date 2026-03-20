package com.qureka.domain.question;

import com.qureka.domain.question.dto.SaveQuestionRequest;
import com.qureka.global.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    /** 문제 저장 — userId는 JWT에서 추출 */
    @PostMapping
    public ResponseEntity<Map<String, Object>> save(
            @Valid @RequestBody SaveQuestionRequest req,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        req.setUserId(principal.getId());   // 클라이언트 값 무시, 토큰 기준으로 덮어씀
        UserQuestion saved = questionService.save(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "문제가 성공적으로 저장되었습니다.",
                "selection_id", saved.getSelectionId()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getByUser(@PathVariable Long userId) {
        List<UserQuestion> questions = questionService.getByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", questions.size(),
                "questions", questions
        ));
    }

    @GetMapping("/search/{userId}")
    public ResponseEntity<Map<String, Object>> search(
            @PathVariable Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type
    ) {
        List<UserQuestion> questions = questionService.search(userId, query, type);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", questions.size(),
                "questions", questions
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "question", questionService.getById(id)
        ));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Map<String, Object>> updateName(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserQuestion updated = questionService.updateName(id, principal.getId(), body.get("questionName"));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "문제 이름이 성공적으로 변경되었습니다.",
                "question", updated
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        questionService.delete(id, principal.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "문제가 성공적으로 삭제되었습니다.",
                "deletedQuestionId", id
        ));
    }
}