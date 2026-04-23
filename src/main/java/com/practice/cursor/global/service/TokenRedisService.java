package com.practice.cursor.global.service;

import com.practice.cursor.global.exception.CustomException;
import com.practice.cursor.global.exception.ErrorCode;
import com.practice.cursor.global.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 토큰 관련 Redis 작업을 담당하는 서비스.
 */
@Service
@RequiredArgsConstructor
public class TokenRedisService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final String LOGOUT_VALUE = "logout";

    private final RedisUtil redisUtil;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    /**
     * Refresh Token을 Redis에 저장한다.
     */
    public void saveRefreshToken(Long memberId, String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisUtil.setValue(key, refreshToken, refreshTokenExpiration);
    }

    /**
     * 회원 ID로 저장된 Refresh Token을 조회한다.
     */
    public String getRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        return redisUtil.getValue(key);
    }

    /**
     * 회원 ID로 저장된 Refresh Token을 삭제한다.
     */
    public void deleteRefreshToken(Long memberId) {
        String key = REFRESH_TOKEN_PREFIX + memberId;
        redisUtil.deleteValue(key);
    }

    /**
     * 요청된 Refresh Token이 Redis에 저장된 값과 일치하는지 검증한다.
     * 
     * @throws CustomException 토큰이 일치하지 않거나 존재하지 않는 경우
     */
    public void validateRefreshToken(Long memberId, String requestToken) {
        String storedToken = getRefreshToken(memberId);
        
        if (storedToken == null || !storedToken.equals(requestToken)) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }
    }

    /**
     * Access Token을 블랙리스트에 추가한다.
     */
    public void addToBlacklist(String accessToken, long remainingTimeMillis) {
        String key = BLACKLIST_PREFIX + accessToken;
        redisUtil.setValue(key, LOGOUT_VALUE, remainingTimeMillis);
    }

    /**
     * Access Token이 블랙리스트에 존재하는지 확인한다.
     */
    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return redisUtil.hasKey(key);
    }
}
