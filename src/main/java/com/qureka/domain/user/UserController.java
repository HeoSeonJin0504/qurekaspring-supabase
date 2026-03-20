package com.qureka.domain.user;

import com.qureka.domain.user.dto.LoginRequest;
import com.qureka.domain.user.dto.RegisterRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /** 아이디 중복 확인
     *  응답: { success, message }
     */
    @PostMapping("/check-userid")
    public ResponseEntity<Map<String, Object>> checkUserid(@RequestBody Map<String, String> body) {
        String userid = body.get("userid");
        if (userid == null || userid.isBlank())
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "아이디가 제공되지 않았습니다."));

        userService.checkUseridAvailable(userid);
        return ResponseEntity.ok(Map.of("success", true, "message", "사용 가능한 아이디입니다."));
    }

    /** 회원가입
     *  응답: { success, message, user: { userid, name } }
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        Map<String, String> userInfo = userService.register(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("success", true, "message", "회원가입이 완료되었습니다.", "user", userInfo));
    }

    /** 로그인
     *  응답: { success, message, user: { id, userid, name, email }, tokens: { accessToken, refreshToken }, rememberMe }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequest req, HttpServletRequest httpReq
    ) {
        String clientIp = resolveIp(httpReq);
        UserService.LoginResult result = userService.login(req, clientIp);
        User user = result.user();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "로그인 성공",
                "user", Map.of(
                        "id",     user.getUserIndex(),
                        "userid", user.getUserid(),
                        "name",   user.getName(),
                        "email",  user.getEmail() != null ? user.getEmail() : ""
                ),
                "tokens", Map.of(
                        "accessToken",  result.accessToken(),
                        "refreshToken", result.refreshToken()
                ),
                "rememberMe", req.isRememberMe()
        ));
    }

    private String resolveIp(HttpServletRequest req) {
        String f = req.getHeader("X-Forwarded-For");
        return f != null && !f.isBlank() ? f.split(",")[0].trim() : req.getRemoteAddr();
    }
}