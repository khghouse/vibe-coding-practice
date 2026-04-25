package com.practice.cursor.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.practice.cursor.domain.auth.dto.request.LoginServiceRequest;
import com.practice.cursor.domain.auth.dto.response.TokenResponse;
import com.practice.cursor.domain.member.entity.Member;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.domain.member.repository.MemberRepository;
import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.security.JwtTokenProvider;
import com.practice.cursor.global.service.RedisTokenService;
import com.practice.cursor.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class AuthServiceTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private RedisTokenService redisTokenService;

    @Test
    @DisplayName("유효한 로그인 요청이면 액세스 토큰과 리프레시 토큰을 반환한다")
    void login_validCredentials_returnsTokenResponse() {
        // given
        Member member = createMember("tester", "password", "테스터");
        LoginServiceRequest request = new LoginServiceRequest("tester", "password");

        // when
        TokenResponse response = authService.login(request);

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(jwtTokenProvider.extractMemberId(response.accessToken())).isEqualTo(member.getId());
        assertThat(jwtTokenProvider.extractRole(response.accessToken())).isEqualTo(Role.USER);
        assertThat(jwtTokenProvider.extractTokenType(response.accessToken()))
                .isEqualTo(JwtTokenProvider.TokenType.ACCESS);
        assertThat(jwtTokenProvider.extractTokenType(response.refreshToken()))
                .isEqualTo(JwtTokenProvider.TokenType.REFRESH);
        verify(redisTokenService).saveRefreshToken(eq(member.getId()), eq(response.refreshToken()));
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다")
    void login_invalidPassword_throwsCustomException() {
        // given
        createMember("tester", "password", "테스터");
        LoginServiceRequest request = new LoginServiceRequest("tester", "wrong-password");

        // when & then
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.INVALID_CREDENTIALS.getMessage());

        verify(redisTokenService, never()).saveRefreshToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이면 토큰을 재발급한다")
    void reissue_validRefreshToken_returnsRotatedTokens() {
        // given
        Member member = createMember("tester", "password", "테스터");
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId());

        // when
        TokenResponse response = authService.reissue(refreshToken);

        // then
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(jwtTokenProvider.extractTokenType(response.accessToken()))
                .isEqualTo(JwtTokenProvider.TokenType.ACCESS);
        assertThat(jwtTokenProvider.extractTokenType(response.refreshToken()))
                .isEqualTo(JwtTokenProvider.TokenType.REFRESH);
        verify(redisTokenService).validateRefreshToken(member.getId(), refreshToken);
        verify(redisTokenService).deleteRefreshToken(member.getId());
        verify(redisTokenService).saveRefreshToken(eq(member.getId()), eq(response.refreshToken()));
    }

    @Test
    @DisplayName("액세스 토큰으로 재발급을 요청하면 실패한다")
    void reissue_accessToken_throwsCustomException() {
        // given
        Member member = createMember("tester", "password", "테스터");
        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getRole());

        // when & then
        assertThatThrownBy(() -> authService.reissue(accessToken))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.TOKEN_TYPE_INVALID.getMessage());

        verify(redisTokenService, never()).validateRefreshToken(anyLong(), anyString());
    }

    @Test
    @DisplayName("유효한 액세스 토큰이면 로그아웃 처리한다")
    void logout_accessToken_blacklistsTokenAndDeletesRefreshToken() {
        // given
        String accessToken = jwtTokenProvider.generateAccessToken(1L, Role.USER);

        // when
        authService.logout(accessToken, 1L);

        // then
        verify(redisTokenService).addToBlacklist(eq(accessToken), anyLong());
        verify(redisTokenService).deleteRefreshToken(1L);
    }

    private Member createMember(String loginId, String rawPassword, String nickname) {
        Member member = Member.create(loginId, passwordEncoder.encode(rawPassword), nickname);
        return memberRepository.save(member);
    }
}
