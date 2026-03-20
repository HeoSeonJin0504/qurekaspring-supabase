package com.qureka.domain.favorite;

import com.qureka.domain.favorite.dto.FavoriteFolderResponse;
import com.qureka.domain.favorite.dto.FavoriteQuestionResponse;
import com.qureka.global.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    private Long parseLongSafe(Object value) {
        if (value == null) return null;
        String str = String.valueOf(value).trim();
        if (str.isEmpty() || str.equalsIgnoreCase("null")) return null;
        try { return Long.parseLong(str); }
        catch (NumberFormatException e) { return null; }
    }

    private Short parseShortSafe(Object value, short defaultVal) {
        if (value == null) return defaultVal;
        String str = String.valueOf(value).trim();
        if (str.isEmpty() || str.equalsIgnoreCase("null")) return defaultVal;
        try { return Short.parseShort(str); }
        catch (NumberFormatException e) { return defaultVal; }
    }

    /** 폴더 목록 — question_count 포함 */
    @GetMapping("/folders/{userId}")
    public ResponseEntity<Map<String, Object>> getFolders(@PathVariable Long userId) {
        List<FavoriteFolderResponse> folders = favoriteService.getFolders(userId);
        return ResponseEntity.ok(Map.of("success", true, "folders", folders));
    }

    @PostMapping("/folders")
    public ResponseEntity<Map<String, Object>> createFolder(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        String folderName = (String) body.get("folderName");
        if (folderName == null || folderName.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "필수 입력값이 누락되었습니다."));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "folder", favoriteService.createFolder(principal.getId(), folderName, (String) body.get("description"))
        ));
    }

    @PostMapping("/folders/ensure-default")
    public ResponseEntity<Map<String, Object>> ensureDefault(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folder", favoriteService.getOrCreateDefaultFolder(principal.getId())
        ));
    }

    @GetMapping("/folders/default/{userId}")
    public ResponseEntity<Map<String, Object>> getDefaultFolder(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folder", favoriteService.getOrCreateDefaultFolder(userId)
        ));
    }

    @DeleteMapping("/folders/{folderId}")
    public ResponseEntity<Map<String, Object>> deleteFolder(
            @PathVariable Long folderId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.deleteFolder(folderId, principal.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "폴더가 삭제되었습니다."));
    }

    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(
            @RequestBody Map<String, Object> body,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long  questionId = parseLongSafe(body.get("questionId"));
        Long  folderId   = parseLongSafe(body.get("folderId"));
        Short qIdx       = parseShortSafe(body.get("questionIndex"), (short) 0);

        if (questionId == null)
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "questionId가 필요합니다."));

        FavoriteQuestion fq = favoriteService.addQuestion(principal.getId(), folderId, questionId, qIdx);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "favoriteId", fq.getFavoriteId(),
                "message", "즐겨찾기에 추가되었습니다."
        ));
    }

    @DeleteMapping("/questions/{favoriteId}")
    public ResponseEntity<Map<String, Object>> removeQuestion(
            @PathVariable Long favoriteId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        favoriteService.removeQuestion(favoriteId, principal.getId());
        return ResponseEntity.ok(Map.of("success", true, "message", "즐겨찾기에서 제거되었습니다."));
    }

    @GetMapping("/check/{userId}/{questionId}")
    public ResponseEntity<Map<String, Object>> checkQuestion(
            @PathVariable Long userId,
            @PathVariable Long questionId,
            @RequestParam(required = false) Short questionIndex
    ) {
        Optional<FavoriteQuestion> result = favoriteService.checkQuestion(userId, questionId, questionIndex);
        if (result.isEmpty())
            return ResponseEntity.ok(Map.of("success", true, "isFavorite", false));
        FavoriteQuestion fq = result.get();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "isFavorite", true,
                "favoriteId", fq.getFavoriteId(),
                "folderId", fq.getFolder().getFolderId(),
                "questionIndex", fq.getQuestionIndex()
        ));
    }

    @PostMapping("/check-multiple/{userId}")
    public ResponseEntity<Map<String, Object>> checkMultiple(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> body
    ) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> questions = (List<Map<String, Object>>) body.get("questions");
        if (questions == null || questions.isEmpty())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "확인할 문제 목록이 필요합니다."));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "statuses", favoriteService.checkMultipleQuestions(userId, questions)
        ));
    }

    @GetMapping("/questions/all/{userId}")
    public ResponseEntity<Map<String, Object>> getAllQuestions(@PathVariable Long userId) {
        List<FavoriteQuestionResponse> result = favoriteService.getAllQuestions(userId)
                .stream()
                .map(FavoriteQuestionResponse::new)
                .toList();
        return ResponseEntity.ok(Map.of("success", true, "questions", result));
    }

    @GetMapping("/folders/{folderId}/questions/{userId}")
    public ResponseEntity<Map<String, Object>> getByFolder(
            @PathVariable Long folderId,
            @PathVariable Long userId
    ) {
        FavoriteService.FolderWithQuestions raw = favoriteService.getQuestionsByFolder(folderId, userId);
        List<FavoriteQuestionResponse> questions = raw.questions()
                .stream()
                .map(FavoriteQuestionResponse::new)
                .toList();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folder", raw.folder(),
                "questions", questions
        ));
    }
}