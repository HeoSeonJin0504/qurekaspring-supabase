package com.qureka.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qureka.global.common.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IP + 엔드포인트 그룹별 Rate Limiting
 * Bucket4j 8.x 새 API 사용 (Bandwidth.builder())
 *
 * 회원가입/로그인  : 15분당  5회
 * AI 생성         : 30분당 10회
 * 저장            : 10분당 10회
 * 토큰 갱신       : 15분당 30회
 * 일반 API        : 15분당 120회
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = resolveClientIp(request);
        String group    = resolveGroup(request.getRequestURI(), request.getMethod());
        Bucket bucket   = bucketCache.computeIfAbsent(clientIp + ":" + group, k -> buildBucket(group));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Rate Limit 초과 - IP: {}, Path: {}", clientIp, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
            response.getWriter().write(
                    objectMapper.writeValueAsString(
                            ApiResponse.fail("요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요.")));
        }
    }

    private String resolveGroup(String path, String method) {
        if (path.matches(".*/users/(register|login|check-userid)")) return "auth";
        if (path.matches(".*/auth/refresh.*"))                       return "refresh";
        if (path.matches(".*/ai/.*"))                                return "ai";
        if (("POST".equals(method) || "PATCH".equals(method))
                && path.matches(".*(summaries|questions|favorites).*"))  return "save";
        return "general";
    }

    // ✅ Bucket4j 8.x 새 API: Bandwidth.builder() 사용
    private Bucket buildBucket(String group) {
        Bandwidth limit = switch (group) {
            case "auth"    -> Bandwidth.builder()
                    .capacity(5)
                    .refillGreedy(5, Duration.ofMinutes(15))
                    .build();
            case "ai"      -> Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofMinutes(30))
                    .build();
            case "save"    -> Bandwidth.builder()
                    .capacity(10)
                    .refillGreedy(10, Duration.ofMinutes(10))
                    .build();
            case "refresh" -> Bandwidth.builder()
                    .capacity(30)
                    .refillGreedy(30, Duration.ofMinutes(15))
                    .build();
            default        -> Bandwidth.builder()
                    .capacity(120)
                    .refillGreedy(120, Duration.ofMinutes(15))
                    .build();
        };
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) return forwarded.split(",")[0].trim();
        return request.getRemoteAddr();
    }
}