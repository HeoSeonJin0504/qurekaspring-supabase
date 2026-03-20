package com.qureka.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    @GetMapping("/")
    public ResponseEntity<Map<String, String>> root() {
        return ResponseEntity.ok(Map.of("message", "API 서버에 오신 것을 환영합니다!"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean ok = checkDb();
        return ResponseEntity.ok(Map.of(
                "status",    ok ? "정상" : "경고",
                "timestamp", Instant.now().toString(),
                "database",  ok ? "연결됨" : "연결 안됨"
        ));
    }

    private boolean checkDb() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(2);
        } catch (Exception e) {
            return false;
        }
    }
}
