package com.practice.cursor.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.cursor.global.service.RedisTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class JwtAuthenticationFilterTest {

    private JwtTokenProvider jwtTokenProvider;
    private RedisTokenService redisTokenService;
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-testing-must-be-at-least-256-bits-long-for-HS256-algorithm";
        long accessTokenExpiration = 1800000L;
        long refreshTokenExpiration = 604800000L;

        jwtTokenProvider = new JwtTokenProvider(secret, accessTokenExpiration, refreshTokenExpiration);
        redisTokenService = mock(RedisTokenService.class);
        objectMapper = new ObjectMapper();
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider, redisTokenService, objectMapper);
    }

    @Test
    @DisplayName("Authorization 헤더가 없으면 다음 필터 체인으로 통과한다")
    void doFilter_missingAuthorizationHeader_passesThroughFilterChain() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        assertThat(response.getStatus()).isEqualTo(200);
        verify(redisTokenService, never()).isBlacklisted(anyString());
    }

    @Test
    @DisplayName("유효하지 않은 토큰이면 TOKEN_INVALID로 응답한다")
    void doFilter_invalidToken_returnsTokenInvalid() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(jsonResponse.get("error").get("code").asText()).isEqualTo("TOKEN_INVALID");
    }

    @Test
    @DisplayName("블랙리스트 토큰이면 TOKEN_BLACKLISTED로 응답한다")
    void doFilter_blacklistedToken_returnsTokenBlacklisted() throws Exception {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, com.practice.cursor.domain.member.entity.Role.USER);
        when(redisTokenService.isBlacklisted(accessToken)).thenReturn(true);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        // then
        JsonNode jsonResponse = objectMapper.readTree(response.getContentAsString());

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(jsonResponse.get("error").get("code").asText()).isEqualTo("TOKEN_BLACKLISTED");
    }
}
