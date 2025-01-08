package com.gameplatform.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:40
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void cacheUser(UserDTO userDTO) {
        try {
            setCache("user:" + userDTO.getId(), userDTO, 30, TimeUnit.MINUTES);
            log.debug("用户缓存成功: {}", userDTO.getId());
        } catch (Exception e) {
            log.error("缓存用户信息失败: {}", e.getMessage());
        }
    }

    @Override
    public Optional<UserDTO> getCachedUser(Long userId) {
        return getCache("user:" + userId, UserDTO.class);
    }

    @Override
    public void invalidateUser(Long userId) {
        try {
            delete("user:" + userId);
            evictCacheByPattern("userPermissions:" + userId + "*");
            evictCacheByPattern("userRoles:" + userId + "*");
            log.debug("用户缓存已清除: {}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败: {}", e.getMessage());
        }
    }

    @Override
    public Optional<Object> get(String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key));
        } catch (Exception e) {
            log.error("获取缓存失败: {} - {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public <T> Optional<T> getCache(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }

            if (value instanceof String && type != String.class) {
                return Optional.of(objectMapper.readValue((String) value, type));
            }

            return Optional.of(type.cast(value));
        } catch (Exception e) {
            log.error("获取缓存失败: {} - {}", key, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void setCache(String key, Object value, long timeout, TimeUnit unit) {
        try {
            if (value instanceof String) {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
            } else {
                String jsonValue = objectMapper.writeValueAsString(value);
                redisTemplate.opsForValue().set(key, jsonValue, timeout, unit);
            }
            log.debug("设置缓存成功: {}", key);
        } catch (Exception e) {
            log.error("设置缓存失败: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("删除缓存成功: {}", key);
        } catch (Exception e) {
            log.error("删除缓存失败: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public void cleanExpiredCache() {
        try {
            Set<String> keys = redisTemplate.keys("*");
            if (keys != null) {
                for (String key : keys) {
                    Long ttl = redisTemplate.getExpire(key);
                    if (ttl != null && ttl <= 0) {
                        redisTemplate.delete(key);
                        log.debug("清理过期缓存: {}", key);
                    }
                }
            }
        } catch (Exception e) {
            log.error("清理过期缓存失败: {}", e.getMessage());
        }
    }

    @Override
    public void evictCache(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("驱逐缓存成功: {}", key);
        } catch (Exception e) {
            log.error("驱逐缓存失败: {} - {}", key, e.getMessage());
        }
    }

    @Override
    public void evictCacheByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("批量驱逐缓存成功: {}", pattern);
            }
        } catch (Exception e) {
            log.error("批量驱逐缓存失败: {} - {}", pattern, e.getMessage());
        }
    }

    @Override
    public void setWithExpiration(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            log.debug("设置带过期时间的缓存成功: {}", key);
        } catch (Exception e) {
            log.error("设置带过期时间的缓存失败: {} - {}", key, e.getMessage());
        }
    }

    // 定时任务：定期清理过期缓存
    @Scheduled(cron = "0 0 */2 * * ?") // 每2小时执行一次
    public void scheduleCleanExpiredCache() {
        log.info("开始定期清理过期缓存");
        cleanExpiredCache();
        log.info("定期清理过期缓存完成");
    }

    // 私有辅助方法
    private String generateCacheKey(String prefix, String identifier) {
        return String.format("%s:%s", prefix, identifier);
    }
}