package com.qureka.domain.summary;

import com.qureka.domain.summary.dto.SaveSummaryRequest;
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
@RequestMapping("/api/summaries")
@RequiredArgsConstructor
public class SummaryController {

    private final SummaryService summaryService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> save(@Valid @RequestBody SaveSummaryRequest req) {
        UserSummary saved = summaryService.save(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "message", "요약이 성공적으로 저장되었습니다.",
                "selection_id", saved.getSelectionId()
        ));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getByUser(@PathVariable Long userId) {
        List<UserSummary> summaries = summaryService.getByUserId(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", summaries.size(),
                "summaries", summaries
        ));
    }

    @GetMapping("/search/{userId}")
    public ResponseEntity<Map<String, Object>> search(
            @PathVariable Long userId,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String type
    ) {
        List<UserSummary> summaries = summaryService.search(userId, query, type);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", summaries.size(),
                "summaries", summaries
        ));
    }

    @GetMapping("/user/{userId}/meta")
    public ResponseEntity<Map<String, Object>> getMeta(@PathVariable Long userId) {
        List<Object[]> meta = summaryService.getMeta(userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "count", meta.size(),
                "summaries", meta
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "summary", summaryService.getById(id)
        ));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<Map<String, Object>> updateName(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UserSummary updated = summaryService.updateName(id, principal.getId(), body.get("summaryName"));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "요약 이름이 성공적으로 변경되었습니다.",
                "summary", updated
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        summaryService.delete(id, principal.getId());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "요약이 성공적으로 삭제되었습니다.",
                "deletedSummaryId", id
        ));
    }
}