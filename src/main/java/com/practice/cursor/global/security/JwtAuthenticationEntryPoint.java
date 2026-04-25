package com.practice.cursor.global.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 인증 실패 시 401 응답을 처리하는 EntryPoint.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        log.debug("인증 실패: {}", authException.getMessage());

        ErrorCode errorCode = resolveErrorCode(request);

        response.setStatus(errorCode.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<Void> errorResponse = ApiResponse.fail(errorCode);
        String jsonResponse = objectMapper.writeValueAsString(errorResponse);

        response.getWriter().write(jsonResponse);
    }

    /**
     * EntryPoint는 인증이 시작되지 못한 경우만 처리한다.
     * 헤더가 없으면 TOKEN_MISSING, Authorization 헤더 형식이 잘못되면 TOKEN_INVALID를 반환한다.
     */
    private ErrorCode resolveErrorCode(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (!StringUtils.hasText(authorizationHeader)) {
            return ErrorCode.TOKEN_MISSING;
        }

        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            return ErrorCode.TOKEN_INVALID;
        }

        return ErrorCode.TOKEN_MISSING;
    }
}
