package com.qureka.domain.user;

import com.qureka.domain.auth.AuthService;
import com.qureka.domain.favorite.FavoriteService;
import com.qureka.domain.user.dto.LoginRequest;
import com.qureka.domain.user.dto.RegisterRequest;
import com.qureka.global.common.ValidationUtil;
import com.qureka.global.exception.CustomException;
import com.qureka.global.exception.ErrorCode;
import com.qureka.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository   userRepository;
    private final PasswordEncoder  passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService      authService;
    private final FavoriteService  favoriteService;
    private final ValidationUtil   validationUtil;

    private final Set<String> registrationLock = ConcurrentHashMap.newKeySet();

    @Value("${app.test-userid:}")
    private String testUserId;

    @Transactional(readOnly = true)
    public void checkUseridAvailable(String userid) {
        if (userid == null || userid.isBlank())
            throw new CustomException(ErrorCode.INVALID_INPUT, "아이디가 제공되지 않았습니다.");
        if (userRepository.existsByUserid(userid))
            throw new CustomException(ErrorCode.USERID_ALREADY_EXISTS);
    }

    @Transactional
    public Map<String, String> register(RegisterRequest req) {
        if (!validationUtil.isSafeUserid(req.getUserid()) || !validationUtil.isSafePassword(req.getPassword()))
            throw new CustomException(ErrorCode.INVALID_INPUT_CHARS,
                    "아이디 또는 비밀번호에 허용되지 않는 문자가 있습니다.");
        if (!validationUtil.isSafeSqlInput(req.getName())
                || !validationUtil.isSafeSqlInput(req.getPhone())
                || (req.getEmail() != null && !validationUtil.isSafeSqlInput(req.getEmail())))
            throw new CustomException(ErrorCode.INVALID_INPUT_CHARS,
                    "입력값에 보안 위협이 되는 문자가 포함되어 있습니다.");
        if (!validationUtil.isValidUserid(req.getUserid()))
            throw new CustomException(ErrorCode.INVALID_USERID_FORMAT);
        if (!validationUtil.isValidName(req.getName()))
            throw new CustomException(ErrorCode.INVALID_INPUT, "이름은 2-50자의 한글 또는 영문이어야 합니다.");
        if (!validationUtil.isValidPhone(req.getPhone()))
            throw new CustomException(ErrorCode.INVALID_INPUT, "전화번호 형식이 올바르지 않습니다.");
        if (req.getEmail() != null && !validationUtil.isValidEmail(req.getEmail()))
            throw new CustomException(ErrorCode.INVALID_INPUT, "이메일 형식이 올바르지 않습니다.");

        if (!registrationLock.add(req.getUserid()))
            throw new CustomException(ErrorCode.DUPLICATE_REQUEST);

        try {
            if (userRepository.existsByUserid(req.getUserid()))
                throw new CustomException(ErrorCode.USERID_ALREADY_EXISTS);

            User saved = userRepository.save(User.builder()
                    .userid(req.getUserid())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .name(req.getName()).age(req.getAge())
                    .gender(req.getGender()).phone(req.getPhone()).email(req.getEmail())
                    .build());

            try { favoriteService.getOrCreateDefaultFolder(saved.getUserIndex()); }
            catch (Exception e) { log.error("기본 폴더 생성 실패: {}", e.getMessage()); }

            return Map.of("userid", saved.getUserid(), "name", saved.getName());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("phone")) throw new CustomException(ErrorCode.PHONE_ALREADY_EXISTS);
            if (msg.contains("email")) throw new CustomException(ErrorCode.EMAIL_ALREADY_EXISTS);
            if (msg.contains("userid")) throw new CustomException(ErrorCode.USERID_ALREADY_EXISTS);
            log.error("회원가입 오류: {}", e.getMessage(), e);
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        } finally {
            registrationLock.remove(req.getUserid());
        }
    }

    @Transactional
    public LoginResult login(LoginRequest req, String clientIp) {
        if (!validationUtil.isSafeUserid(req.getUserid()))
            throw new CustomException(ErrorCode.INVALID_INPUT_CHARS,
                    "입력값에 허용되지 않는 문자가 포함되어 있습니다.");
        if (!req.getUserid().equals(testUserId) && !validationUtil.isValidUserid(req.getUserid()))
            throw new CustomException(ErrorCode.INVALID_USERID_FORMAT, "아이디 형식이 올바르지 않습니다.");

        User user = userRepository.findByUserid(req.getUserid()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("[qureka] 로그인 실패 - 아이디: {}, IP: {}", req.getUserid(), clientIp);
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        String accessToken  = jwtTokenProvider.generateAccessToken(
                user.getUserIndex(), user.getUserid(), user.getName());
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                user.getUserIndex(), user.getUserid(), user.getName());

        authService.saveRefreshToken(user, refreshToken);
        log.info("[qureka] 로그인 성공 - 사용자: {}, IP: {}", user.getUserid(), clientIp);

        return new LoginResult(user, accessToken, refreshToken);
    }

    public record LoginResult(User user, String accessToken, String refreshToken) {}
}
