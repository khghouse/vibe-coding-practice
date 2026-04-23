package com.practice.cursor.global.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.practice.cursor.global.exception.ErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final int status;
    private final boolean success;
    private T data;
    private ErrorBody error;

    public ApiResponse(int status, boolean success, T data, ErrorBody error) {
        this.status = status;
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static ApiResponse<Void> ok() {
        return ApiResponse.<Void>builder()
                .status(200)
                .success(true)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .status(200)
                .success(true)
                .data(data)
                .build();
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return ApiResponse.<Void>builder()
                .status(errorCode.getStatus())
                .success(false)
                .error(ErrorBody.from(errorCode))
                .build();
    }

    public static ApiResponse<Void> fail(ErrorCode errorCode, String overrideMessage) {
        ErrorBody body = ErrorBody.from(errorCode);
        if (overrideMessage != null && !overrideMessage.isBlank()) {
            body = new ErrorBody(body.getCode(), overrideMessage);
        }
        return ApiResponse.<Void>builder()
                .status(errorCode.getStatus())
                .success(false)
                .error(body)
                .build();
    }
}
