package com.qureka.domain.favorite;

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

    @GetMapping("/folders/{userId}")
    public ResponseEntity<Map<String, Object>> getFolders(@PathVariable Long userId) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folders", favoriteService.getFolders(userId)
        ));
    }

    @PostMapping("/folders")
    public ResponseEntity<Map<String, Object>> createFolder(@RequestBody Map<String, Object> body) {
        Long   userId     = Long.parseLong(String.valueOf(body.get("userId")));
        String folderName = (String) body.get("folderName");
        if (folderName == null || folderName.isBlank())
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "필수 입력값이 누락되었습니다."));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "folder", favoriteService.createFolder(userId, folderName, (String) body.get("description"))
        ));
    }

    @PostMapping("/folders/ensure-default")
    public ResponseEntity<Map<String, Object>> ensureDefault(@RequestBody Map<String, Object> body) {
        Long userId = Long.parseLong(String.valueOf(body.get("userId")));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folder", favoriteService.getOrCreateDefaultFolder(userId)
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
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long resolvedId = userId != null ? userId : (principal != null ? principal.getId() : null);
        if (resolvedId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "사용자 인증이 필요합니다."));
        favoriteService.deleteFolder(folderId, resolvedId);
        return ResponseEntity.ok(Map.of("success", true, "message", "폴더가 삭제되었습니다."));
    }

    @PostMapping("/questions")
    public ResponseEntity<Map<String, Object>> addQuestion(@RequestBody Map<String, Object> body) {
        Long  userId     = Long.parseLong(String.valueOf(body.get("userId")));
        Long  questionId = Long.parseLong(String.valueOf(body.get("questionId")));
        Long  folderId   = body.get("folderId") != null ? Long.parseLong(String.valueOf(body.get("folderId"))) : null;
        Short qIdx       = body.get("questionIndex") != null
                ? Short.parseShort(String.valueOf(body.get("questionIndex"))) : 0;
        FavoriteQuestion fq = favoriteService.addQuestion(userId, folderId, questionId, qIdx);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "favoriteId", fq.getFavoriteId(),
                "message", "즐겨찾기에 추가되었습니다."
        ));
    }

    @DeleteMapping("/questions/{favoriteId}")
    public ResponseEntity<Map<String, Object>> removeQuestion(
            @PathVariable Long favoriteId,
            @RequestParam(required = false) Long userId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        Long resolvedId = userId != null ? userId : (principal != null ? principal.getId() : null);
        if (resolvedId == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "사용자 인증이 필요합니다."));
        favoriteService.removeQuestion(favoriteId, resolvedId);
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
        return ResponseEntity.ok(Map.of(
                "success", true,
                "questions", favoriteService.getAllQuestions(userId)
        ));
    }

    @GetMapping("/folders/{folderId}/questions/{userId}")
    public ResponseEntity<Map<String, Object>> getByFolder(
            @PathVariable Long folderId,
            @PathVariable Long userId
    ) {
        FavoriteService.FolderWithQuestions result = favoriteService.getQuestionsByFolder(folderId, userId);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "folder", result.folder(),
                "questions", result.questions()
        ));
    }
}