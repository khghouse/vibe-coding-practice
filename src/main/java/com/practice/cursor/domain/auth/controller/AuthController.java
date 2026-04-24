package com.practice.cursor.domain.auth.controller;

import com.practice.cursor.global.response.ApiResponse;
import com.practice.cursor.domain.auth.dto.request.LoginRequest;
import com.practice.cursor.domain.auth.dto.request.LoginServiceRequest;
import com.practice.cursor.domain.auth.dto.request.ReissueRequest;
import com.practice.cursor.domain.auth.dto.response.TokenResponse;
import com.practice.cursor.domain.auth.service.AuthService;
import com.practice.cursor.global.security.MemberPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API를 제공하는 컨트롤러.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    /**
     * 로그인을 처리한다.
     */
    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = authService.login(LoginServiceRequest.from(request));
        return ApiResponse.ok(response);
    }

    /**
     * 토큰을 재발급한다.
     */
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        TokenResponse response = authService.reissue(request.refreshToken());
        return ApiResponse.ok(response);
    }

    /**
     * 로그아웃을 처리한다.
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request, Authentication authentication) {
        String accessToken = extractTokenFromRequest(request);
        MemberPrincipal memberPrincipal = (MemberPrincipal) authentication.getPrincipal();
        
        authService.logout(accessToken, memberPrincipal.getMemberId());
        return ApiResponse.ok();
    }

    /**
     * 요청에서 Bearer 토큰을 추출한다.
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        
        return null;
    }
}
