package com.qureka.domain.auth;

import com.qureka.domain.user.User;
import com.qureka.domain.user.UserRepository;
import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import com.qureka.global.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository         userRepository;
    private final JwtTokenProvider       jwtTokenProvider;

    @Transactional
    public void saveRefreshToken(User user, String token) {
        OffsetDateTime expiresAt = OffsetDateTime.now()
                .plusSeconds(jwtTokenProvider.getRefreshTokenExpiresIn() / 1000);
        refreshTokenRepository.save(
                RefreshToken.builder().user(user).token(token).expiresAt(expiresAt).build());
    }

    @Transactional
    public TokenRefreshResult refreshAccessToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new CustomException(ErrorCode.TOKEN_NOT_PROVIDED, "리프레시 토큰이 제공되지 않았습니다.");

        Claims payload;
        try {
            payload = jwtTokenProvider.validateRefreshToken(refreshToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID, "유효하지 않거나 만료된 리프레시 토큰입니다.");
        }

        refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new CustomException(ErrorCode.TOKEN_INVALID,
                        "유효하지 않거나 만료된 리프레시 토큰입니다."));

        String userid = jwtTokenProvider.getUserid(payload);
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String newAccessToken = jwtTokenProvider.generateAccessToken(
                user.getUserIndex(), user.getUserid(), user.getName());

        return new TokenRefreshResult(newAccessToken, user);
    }

    @Transactional
    public void logout(String refreshToken, Long userId) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenRepository.deleteByToken(refreshToken);
        } else if (userId != null) {
            userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
        }
    }

    public Map<String, Object> verifyAccessToken(String accessToken) {
        if (accessToken == null || accessToken.isBlank())
            throw new CustomException(ErrorCode.TOKEN_NOT_PROVIDED);
        Claims claims;
        try {
            claims = jwtTokenProvider.validateAccessToken(accessToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.TOKEN_INVALID);
        }
        return Map.of(
                "id",     jwtTokenProvider.getUserId(claims),
                "userid", jwtTokenProvider.getUserid(claims),
                "name",   jwtTokenProvider.getName(claims)
        );
    }

    public record TokenRefreshResult(String accessToken, User user) {}
}
