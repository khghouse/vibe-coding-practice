package com.practice.cursor.domain.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.practice.cursor.domain.auth.dto.request.LoginRequest;
import com.practice.cursor.domain.auth.dto.request.LoginServiceRequest;
import com.practice.cursor.domain.auth.dto.request.RegisterRequest;
import com.practice.cursor.domain.auth.dto.request.RegisterServiceRequest;
import com.practice.cursor.domain.auth.dto.request.ReissueRequest;
import com.practice.cursor.domain.auth.dto.response.RegisterResponse;
import com.practice.cursor.domain.auth.dto.response.TokenResponse;
import com.practice.cursor.domain.auth.service.AuthService;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.global.security.MemberPrincipal;
import com.practice.cursor.support.RestDocsSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

class AuthControllerDocsTest extends RestDocsSupport {

    private final AuthService authService = mock(AuthService.class);

    @Override
    protected Object initController() {
        return new AuthController(authService);
    }

    @Test
    @DisplayName("회원가입 API 문서화")
    void register_validRequest_generatesDocument() throws Exception {
        // given
        RegisterRequest request = new RegisterRequest("tester", "password", "테스터");
        RegisterResponse response = new RegisterResponse(1L, "tester", "테스터", Role.USER);

        given(authService.register(any(RegisterServiceRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("loginId").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("로그인 ID (4-20자)"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("비밀번호"),
                                fieldWithPath("nickname").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("닉네임 (2-10자)")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.id").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("회원 ID"),
                                fieldWithPath("data.loginId").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("로그인 ID"),
                                fieldWithPath("data.nickname").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("닉네임"),
                                fieldWithPath("data.role").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("회원 권한")
                        )
                ));
    }

    @Test
    @DisplayName("로그인 API 문서화")
    void login_validRequest_generatesDocument() throws Exception {
        // given
        LoginRequest request = new LoginRequest("tester", "password");
        TokenResponse response = TokenResponse.of("access-token", "refresh-token");

        given(authService.login(any(LoginServiceRequest.class))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("loginId").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("로그인 ID"),
                                fieldWithPath("password").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("비밀번호")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("리프레시 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("토큰 재발급 API 문서화")
    void reissue_validRequest_generatesDocument() throws Exception {
        // given
        ReissueRequest request = new ReissueRequest("refresh-token");
        TokenResponse response = TokenResponse.of("new-access-token", "new-refresh-token");

        given(authService.reissue("refresh-token")).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andDo(document.document(
                        requestFields(
                                fieldWithPath("refreshToken").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("리프레시 토큰")
                        ),
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부"),
                                fieldWithPath("data.accessToken").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("새 액세스 토큰"),
                                fieldWithPath("data.refreshToken").type(JsonFieldType.STRING)
                                        .attributes(key("required").value("true"))
                                        .description("새 리프레시 토큰")
                        )
                ));
    }

    @Test
    @DisplayName("로그아웃 API 문서화")
    void logout_authenticatedRequest_generatesDocument() throws Exception {
        // given
        MemberPrincipal memberPrincipal = MemberPrincipal.authenticated(1L, Role.USER);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                memberPrincipal,
                null,
                memberPrincipal.getAuthorities());

        // when & then
        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer access-token")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(document.document(
                        responseFields(
                                fieldWithPath("status").type(JsonFieldType.NUMBER)
                                        .attributes(key("required").value("true"))
                                        .description("HTTP 상태 코드"),
                                fieldWithPath("success").type(JsonFieldType.BOOLEAN)
                                        .attributes(key("required").value("true"))
                                        .description("성공 여부")
                        )
                ));
    }
}
