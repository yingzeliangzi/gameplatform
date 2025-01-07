package com.gameplatform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:40
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void cleanExpiredCache() {
        // Redis会自动清理过期的键，这里可以添加额外的清理逻辑
        // 比如清理特定前缀的过期缓存等
    }

    @Override
    public void evictCache(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void evictCacheByPattern(String pattern) {
        var keys = redisTemplate.keys(pattern + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public <T> Optional<T> getCache(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(objectMapper.convertValue(value, type));
    }

    @Override
    public void setCache(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
}