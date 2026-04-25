package com.practice.cursor.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.domain.auth.dto.request.LoginRequest;
import com.practice.cursor.domain.auth.dto.request.LoginServiceRequest;
import com.practice.cursor.domain.auth.dto.request.ReissueRequest;
import com.practice.cursor.domain.auth.dto.response.TokenResponse;
import com.practice.cursor.global.exception.GlobalExceptionHandler;
import com.practice.cursor.global.security.MemberPrincipal;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.support.ControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

@Import(GlobalExceptionHandler.class)
class AuthControllerTest extends ControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인 요청이 유효하면 토큰 응답을 반환한다")
    void login_validRequest_returnsTokenResponse() throws Exception {
        // given
        TokenResponse response = TokenResponse.of("access-token", "refresh-token");
        when(authService.login(any(LoginServiceRequest.class))).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(new LoginRequest("tester", "password"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginServiceRequest.class));
    }

    @Test
    @DisplayName("로그인 요청이 유효하지 않으면 400을 반환한다")
    void login_invalidRequest_returnsBadRequest() throws Exception {
        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(new LoginRequest("", ""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.message").isString());

        verify(authService, never()).login(any());
    }

    @Test
    @DisplayName("재발급 요청이 유효하면 토큰 응답을 반환한다")
    void reissue_validRequest_returnsTokenResponse() throws Exception {
        // given
        TokenResponse response = TokenResponse.of("new-access-token", "new-refresh-token");
        when(authService.reissue("refresh-token")).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(csrf())
                        .content(objectMapper.writeValueAsString(new ReissueRequest("refresh-token"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.data.refreshToken").value("new-refresh-token"));

        verify(authService).reissue("refresh-token");
    }

    @Test
    @DisplayName("로그아웃 요청이 인증되면 성공 응답을 반환한다")
    void logout_authenticatedRequest_returnsOk() throws Exception {
        // given
        MemberPrincipal memberPrincipal = MemberPrincipal.authenticated(1L, Role.USER);
        Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
                memberPrincipal,
                null,
                memberPrincipal.getAuthorities());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                        .with(csrf())
                        .with(authentication(authenticationToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(authService).logout(eq("access-token"), eq(1L));
    }
}
