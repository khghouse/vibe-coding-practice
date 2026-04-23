package com.practice.cursor.global.response;

import com.practice.cursor.global.exception.ErrorCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ErrorBody {

    private String code;
    private String message;

    public ErrorBody(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public static ErrorBody from(ErrorCode errorCode) {
        return new ErrorBody(errorCode.getCode(), errorCode.getMessage());
    }

    public String code() {
        return code;
    }

    public String message() {
        return message;
    }
}

