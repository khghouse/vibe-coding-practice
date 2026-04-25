package com.practice.cursor.domain.auth.service;

import com.practice.cursor.domain.auth.dto.request.RegisterServiceRequest;
import com.practice.cursor.domain.auth.dto.response.RegisterResponse;
import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.security.JwtTokenProvider;
import com.practice.cursor.global.security.MemberAuthenticationProvider;
import com.practice.cursor.global.security.MemberPrincipal;
import com.practice.cursor.global.service.RedisTokenService;
import com.practice.cursor.domain.auth.dto.request.LoginServiceRequest;
import com.practice.cursor.domain.auth.dto.response.TokenResponse;
import com.practice.cursor.domain.member.dto.request.MemberCreateServiceRequest;
import com.practice.cursor.domain.member.entity.Member;
import com.practice.cursor.domain.member.entity.Role;
import com.practice.cursor.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberAuthenticationProvider authenticationProvider;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTokenService tokenRedisService;
    private final MemberService memberService;

    /**
     * 회원을 등록한다.
     */
    @Transactional
    public RegisterResponse register(RegisterServiceRequest request) {
        Member savedMember = memberService.register(
                MemberCreateServiceRequest.of(request.loginId(), request.password(), request.nickname()));
        return RegisterResponse.from(savedMember);
    }

    /**
     * 로그인을 처리한다.
     */
    @Transactional
    public TokenResponse login(LoginServiceRequest request) {
        // 인증 처리
        Authentication authRequest = new UsernamePasswordAuthenticationToken(
                request.loginId(), 
                request.password()
        );
        Authentication authentication = authenticationProvider.authenticate(authRequest);
        
        // 인증된 사용자 정보 추출
        MemberPrincipal memberPrincipal = (MemberPrincipal) authentication.getPrincipal();
        Long memberId = memberPrincipal.getMemberId();
        Role role = memberPrincipal.getRole();
        
        // 토큰 생성
        String accessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String refreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        
        // Refresh Token Redis에 저장
        tokenRedisService.saveRefreshToken(memberId, refreshToken);
        
        return TokenResponse.of(accessToken, refreshToken);
    }

    /**
     * 토큰을 재발급한다. (Refresh Token Rotation 적용)
     */
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        // Refresh Token 유효성 검증
        jwtTokenProvider.validateToken(refreshToken);
        jwtTokenProvider.validateRefreshTokenType(refreshToken);
        
        // memberId 추출
        Long memberId = jwtTokenProvider.extractMemberId(refreshToken);
        
        // Redis에 저장된 Refresh Token과 일치 여부 확인
        tokenRedisService.validateRefreshToken(memberId, refreshToken);
        
        // 기존 Refresh Token 삭제
        tokenRedisService.deleteRefreshToken(memberId);
        
        // Member 조회하여 실제 Role 가져오기
        Member member = memberService.getById(memberId);
        Role role = member.getRole();
        String newAccessToken = jwtTokenProvider.generateAccessToken(memberId, role);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(memberId);
        
        // 새 Refresh Token Redis에 저장
        tokenRedisService.saveRefreshToken(memberId, newRefreshToken);
        
        return TokenResponse.of(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃을 처리한다.
     */
    @Transactional
    public void logout(String accessToken, Long memberId) {
        jwtTokenProvider.validateToken(accessToken);
        jwtTokenProvider.validateAccessTokenType(accessToken);
        validateLogoutOwner(accessToken, memberId);

        // Access Token 블랙리스트에 추가
        long remainingTime = jwtTokenProvider.getTokenRemainingTime(accessToken);
        tokenRedisService.addToBlacklist(accessToken, remainingTime);
        
        // Refresh Token 삭제
        tokenRedisService.deleteRefreshToken(memberId);
    }

    private void validateLogoutOwner(String accessToken, Long memberId) {
        Long tokenMemberId = jwtTokenProvider.extractMemberId(accessToken);
        if (!tokenMemberId.equals(memberId)) {
            throw new CustomException(ErrorCode.TOKEN_OWNER_MISMATCH);
        }
    }
}
