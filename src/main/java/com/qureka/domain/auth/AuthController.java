package com.qureka.domain.auth;

import com.qureka.domain.user.User;
import com.qureka.global.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 액세스 토큰 갱신
     *  응답: { success, message, accessToken, user: { id, userid, name } }
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> body) {
        AuthService.TokenRefreshResult result = authService.refreshAccessToken(body.get("refreshToken"));
        User user = result.user();
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "액세스 토큰이 갱신되었습니다.",
                "accessToken", result.accessToken(),
                "user", Map.of(
                        "id",     user.getUserIndex(),
                        "userid", user.getUserid(),
                        "name",   user.getName()
                )
        ));
    }

    /** 로그아웃
     *  응답: { success, message }
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestBody(required = false) Map<String, String> body,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req
    ) {
        String refreshToken = body != null ? body.get("refreshToken") : null;
        Long userId = principal != null ? principal.getId() : null;
        authService.logout(refreshToken, userId);

        log.info("[qureka] 로그아웃 - 사용자: {}, IP: {}",
                principal != null ? principal.getUserid() : "unknown", resolveIp(req));

        return ResponseEntity.ok(Map.of("success", true, "message", "로그아웃 되었습니다."));
    }

    /** 토큰 검증
     *  응답: { success, message, user: { id, userid, name } }
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verify(
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest req
    ) {
        if (principal != null) {
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "유효한 토큰입니다.",
                    "user", Map.of(
                            "id",     principal.getId(),
                            "userid", principal.getUserid(),
                            "name",   principal.getName()
                    )
            ));
        }
        // principal이 null인 경우 토큰 직접 검증
        Map<String, Object> userInfo = authService.verifyAccessToken(extractBearer(req));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "유효한 토큰입니다.",
                "user", userInfo
        ));
    }

    private String resolveIp(HttpServletRequest req) {
        String f = req.getHeader("X-Forwarded-For");
        return StringUtils.hasText(f) ? f.split(",")[0].trim() : req.getRemoteAddr();
    }

    private String extractBearer(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        return StringUtils.hasText(h) && h.startsWith("Bearer ") ? h.substring(7) : null;
    }
}