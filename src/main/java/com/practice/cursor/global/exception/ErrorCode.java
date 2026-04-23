package com.practice.cursor.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT_VALUE(400, "INVALID_INPUT_VALUE", "잘못된 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR", "서버 내부 오류가 발생했습니다."),

    // Todo
    TODO_NOT_FOUND(404, "TODO_NOT_FOUND", "존재하지 않는 할 일입니다. [id=%d]"),

    // Member
    MEMBER_NOT_FOUND(404, "MEMBER_NOT_FOUND", "존재하지 않는 회원입니다."),
    DUPLICATE_LOGIN_ID(409, "DUPLICATE_LOGIN_ID", "이미 사용 중인 로그인 ID입니다."),
    DUPLICATE_NICKNAME(409, "DUPLICATE_NICKNAME", "이미 사용 중인 닉네임입니다."),

    // Authentication
    INVALID_CREDENTIALS(401, "INVALID_CREDENTIALS", "로그인 정보가 올바르지 않습니다."),
    TOKEN_EXPIRED(401, "TOKEN_EXPIRED", "토큰이 만료되었습니다."),
    TOKEN_INVALID(401, "TOKEN_INVALID", "유효하지 않은 토큰입니다."),
    TOKEN_MISSING(401, "TOKEN_MISSING", "토큰이 없습니다."),
    TOKEN_BLACKLISTED(401, "TOKEN_BLACKLISTED", "로그아웃된 토큰입니다."),
    REFRESH_TOKEN_NOT_FOUND(401, "REFRESH_TOKEN_NOT_FOUND", "유효하지 않은 리프레시 토큰입니다.");

    private final int status;
    private final String code;
    private final String message;

    public String formatMessage(Object... args) {
        return String.format(this.message, args);
    }
}

