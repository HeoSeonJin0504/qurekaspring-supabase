package com.qureka.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    INVALID_INPUT(HttpStatus.BAD_REQUEST,           "입력값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND,                 "리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED,           "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN,                 "접근 권한이 없습니다."),
    DUPLICATE_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "이미 처리 중인 요청입니다. 잠시 후 다시 시도해주세요."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청 횟수를 초과했습니다. 잠시 후 다시 시도해주세요."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND,            "사용자를 찾을 수 없습니다."),
    USERID_ALREADY_EXISTS(HttpStatus.CONFLICT,      "이미 사용 중인 아이디입니다."),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT,       "이미 등록된 전화번호입니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT,       "이미 등록된 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED,    "아이디 또는 비밀번호가 일치하지 않습니다."),
    INVALID_USERID_FORMAT(HttpStatus.BAD_REQUEST,   "아이디 형식이 올바르지 않습니다. (5-20자 영문 소문자/숫자)"),
    INVALID_INPUT_CHARS(HttpStatus.BAD_REQUEST,     "입력값에 허용되지 않는 문자가 포함되어 있습니다."),

    TOKEN_NOT_PROVIDED(HttpStatus.BAD_REQUEST,      "토큰이 제공되지 않았습니다."),
    TOKEN_INVALID(HttpStatus.UNAUTHORIZED,          "유효하지 않거나 만료된 토큰입니다."),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED,          "토큰이 만료되었습니다."),

    SUMMARY_NOT_FOUND(HttpStatus.NOT_FOUND,         "요약을 찾을 수 없습니다."),
    SUMMARY_FORBIDDEN(HttpStatus.FORBIDDEN,         "해당 요약을 수정/삭제할 권한이 없습니다."),

    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND,        "문제를 찾을 수 없습니다."),
    QUESTION_FORBIDDEN(HttpStatus.FORBIDDEN,        "해당 문제를 수정/삭제할 권한이 없습니다."),
    INVALID_QUESTION_TYPE(HttpStatus.BAD_REQUEST,   "유효하지 않은 문제 타입입니다."),

    FOLDER_NOT_FOUND(HttpStatus.NOT_FOUND,          "폴더를 찾을 수 없습니다."),
    FOLDER_ALREADY_EXISTS(HttpStatus.CONFLICT,      "이미 존재하는 폴더 이름입니다."),
    FOLDER_DEFAULT_PROTECTED(HttpStatus.FORBIDDEN,  "기본 폴더는 삭제할 수 없습니다."),
    FAVORITE_ALREADY_EXISTS(HttpStatus.CONFLICT,    "이미 즐겨찾기에 추가된 문제입니다."),

    FILE_NOT_PROVIDED(HttpStatus.BAD_REQUEST,       "파일이 필요합니다. (PDF 또는 PPTX)"),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST,   "PDF 또는 PPTX 파일만 지원합니다."),
    FILE_EXTRACT_FAILED(HttpStatus.UNPROCESSABLE_ENTITY,
            "파일에서 충분한 텍스트를 추출하지 못했습니다."),

    AI_SERVICE_ERROR(HttpStatus.BAD_GATEWAY,        "AI 서비스 오류가 발생했습니다. 잠시 후 다시 시도해주세요."),
    INVALID_SUMMARY_TYPE(HttpStatus.BAD_REQUEST,    "지원하지 않는 요약 타입입니다."),
    SUMMARY_TEXT_TOO_SHORT(HttpStatus.BAD_REQUEST,  "문제 생성을 위한 텍스트가 필요합니다. (최소 30자 이상)");

    private final HttpStatus status;
    private final String     message;

    ErrorCode(HttpStatus status, String message) {
        this.status  = status;
        this.message = message;
    }
}
