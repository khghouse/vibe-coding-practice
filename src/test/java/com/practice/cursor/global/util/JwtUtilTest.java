package com.practice.cursor.global.util;

import com.practice.cursor.domain.member.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JwtUtil 단위 테스트.
 */
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        String secret = "test-secret-key-for-testing-must-be-at-least-256-bits-long-for-HS256-algorithm";
        long accessTokenExpiration = 1800000L;
        long refreshTokenExpiration = 604800000L;

        jwtUtil = new JwtUtil(secret, accessTokenExpiration, refreshTokenExpiration);
    }

    @Test
    @DisplayName("Access Token 생성 및 검증이 정상 동작한다")
    void generateAccessToken_validMemberIdAndRole_returnsValidToken() {
        // given
        Long memberId = 1L;
        Role role = Role.USER;

        // when
        String accessToken = jwtUtil.generateAccessToken(memberId, role);

        // then
        assertThat(jwtUtil.validateToken(accessToken)).isTrue();
        assertThat(jwtUtil.extractMemberId(accessToken)).isEqualTo(memberId);
        assertThat(jwtUtil.extractRole(accessToken)).isEqualTo(role);
    }

    @Test
    @DisplayName("Refresh Token 생성 및 검증이 정상 동작한다")
    void generateRefreshToken_validMemberId_returnsValidToken() {
        // given
        Long memberId = 1L;

        // when
        String refreshToken = jwtUtil.generateRefreshToken(memberId);

        // then
        assertThat(jwtUtil.validateToken(refreshToken)).isTrue();
        assertThat(jwtUtil.extractMemberId(refreshToken)).isEqualTo(memberId);
    }

}
