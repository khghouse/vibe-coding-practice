package com.practice.cursor.common.util;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Redis 작업을 위한 유틸리티 클래스.
 */
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 값을 저장한다.
     */
    public void setValue(String key, String value, long ttlMillis) {
        Duration ttl = Duration.ofMillis(ttlMillis);
        stringRedisTemplate.opsForValue().set(key, value, ttl);
    }

    /**
     * 값을 조회한다.
     */
    public String getValue(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 값을 삭제한다.
     */
    public void deleteValue(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * 키 존재 여부를 확인한다.
     */
    public boolean hasKey(String key) {
        Boolean exists = stringRedisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
