package com.practice.cursor.global.util;

import com.practice.cursor.domain.member.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 토큰 생성 및 검증 유틸리티.
 */
@Slf4j
@Component
public class JwtUtil {

    private static final String MEMBER_ID_CLAIM = "memberId";
    private static final String ROLE_CLAIM = "role";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    /**
     * Access Token을 생성한다.
     */
    public String generateAccessToken(Long memberId, Role role) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(accessTokenExpiration);

        return Jwts.builder()
                .claim(MEMBER_ID_CLAIM, memberId)
                .claim(ROLE_CLAIM, role.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Refresh Token을 생성한다.
     */
    public String generateRefreshToken(Long memberId) {
        Instant now = Instant.now();
        Instant expiration = now.plusMillis(refreshTokenExpiration);

        return Jwts.builder()
                .claim(MEMBER_ID_CLAIM, memberId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 회원 ID를 추출한다.
     */
    public Long extractMemberId(String token) {
        Claims claims = extractClaims(token);
        return claims.get(MEMBER_ID_CLAIM, Long.class);
    }

    /**
     * 토큰에서 권한을 추출한다.
     */
    public Role extractRole(String token) {
        Claims claims = extractClaims(token);
        String roleName = claims.get(ROLE_CLAIM, String.class);
        return Role.valueOf(roleName);
    }

    /**
     * 토큰의 유효성을 검증한다.
     * 
     * @throws ExpiredJwtException 토큰이 만료된 경우
     * @throws SecurityException 서명이 유효하지 않은 경우
     * @throws MalformedJwtException 토큰 형식이 잘못된 경우
     * @throws UnsupportedJwtException 지원하지 않는 토큰인 경우
     * @throws IllegalArgumentException 토큰이 null이거나 빈 문자열인 경우
     */
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("토큰이 만료되었습니다: {}", e.getMessage());
            throw e;
        } catch (SecurityException | MalformedJwtException e) {
            log.debug("유효하지 않은 토큰입니다: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.debug("지원하지 않는 토큰입니다: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.debug("토큰이 비어있습니다: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 토큰의 남은 만료시간을 밀리초로 계산한다.
     * 로그아웃 시 블랙리스트 TTL 설정에 사용된다.
     */
    public long getTokenRemainingTime(String token) {
        Claims claims = extractClaims(token);
        Date expiration = claims.getExpiration();
        long currentTime = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        
        return Math.max(0, expirationTime - currentTime);
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
