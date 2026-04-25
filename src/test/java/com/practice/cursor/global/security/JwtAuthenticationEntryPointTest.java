package com.practice.cursor.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.InsufficientAuthenticationException;

class JwtAuthenticationEntryPointTest {

    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        jwtAuthenticationEntryPoint = new JwtAuthenticationEntryPoint(objectMapper);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 TOKEN_MISSING으로 응답한다")
    void commence_missingAuthorizationHeader_returnsTokenMissing() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Full authentication is required"));

        // then
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(jsonResponse.get("error").get("code").asText()).isEqualTo("TOKEN_MISSING");
        assertThat(jsonResponse.get("error").get("message").asText()).isEqualTo("토큰이 없습니다.");
    }

    @Test
    @DisplayName("Authorization 헤더 형식이 잘못되면 TOKEN_INVALID로 응답한다")
    void commence_invalidAuthorizationHeader_returnsTokenInvalid() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Basic abc123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        jwtAuthenticationEntryPoint.commence(
                request,
                response,
                new InsufficientAuthenticationException("Full authentication is required"));

        // then
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(jsonResponse.get("error").get("code").asText()).isEqualTo("TOKEN_INVALID");
        assertThat(jsonResponse.get("error").get("message").asText()).isEqualTo("유효하지 않은 토큰입니다.");
    }
}
