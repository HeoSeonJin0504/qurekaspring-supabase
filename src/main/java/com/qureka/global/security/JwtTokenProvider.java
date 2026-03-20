package com.qureka.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.access-token-secret}")
    private String accessTokenSecret;

    @Value("${jwt.refresh-token-secret}")
    private String refreshTokenSecret;

    @Value("${jwt.access-token-expires-in}")
    private long accessTokenExpiresIn;

    @Value("${jwt.refresh-token-expires-in}")
    private long refreshTokenExpiresIn;

    private SecretKey accessKey;
    private SecretKey refreshKey;

    @PostConstruct
    public void init() {
        this.accessKey  = Keys.hmacShaKeyFor(accessTokenSecret.getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(refreshTokenSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId, String userid, String name) {
        return buildToken(accessKey, userId, userid, name, accessTokenExpiresIn);
    }

    public String generateRefreshToken(Long userId, String userid, String name) {
        return buildToken(refreshKey, userId, userid, name, refreshTokenExpiresIn);
    }

    private String buildToken(SecretKey key, Long userId, String userid, String name, long expiresIn) {
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put("id",     userId);
        claims.put("userid", userid);
        claims.put("name",   name);
        return Jwts.builder()
                .claims(claims).subject(String.valueOf(userId))
                .issuedAt(now).expiration(new Date(now.getTime() + expiresIn))
                .signWith(key).compact();
    }

    public Claims validateAccessToken(String token)  { return parseClaims(token, accessKey); }
    public Claims validateRefreshToken(String token) { return parseClaims(token, refreshKey); }

    private Claims parseClaims(String token, SecretKey key) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    public Long getUserId(Claims claims) {
        Object id = claims.get("id");
        if (id instanceof Integer) return ((Integer) id).longValue();
        if (id instanceof Long)    return (Long) id;
        return Long.parseLong(String.valueOf(id));
    }

    public String getUserid(Claims claims) { return claims.get("userid", String.class); }
    public String getName(Claims claims)   { return claims.get("name",   String.class); }
    public long getRefreshTokenExpiresIn() { return refreshTokenExpiresIn; }
}
