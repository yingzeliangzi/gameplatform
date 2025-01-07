package com.gameplatform.service;

import com.gameplatform.model.dto.UserDTO;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:53
 * @description TODO
 */
public interface CacheService {
    void cacheUser(UserDTO userDTO);
    Optional<UserDTO> getCachedUser(Long userId);
    void invalidateUser(Long userId);
    void setWithExpiration(String key, Object value, long timeout, TimeUnit unit);
    Optional<Object> get(String key);
    void delete(String key);
    void cleanExpiredCache();
    void evictCache(String key);
    void evictCacheByPattern(String pattern);
    <T> Optional<T> getCache(String key, Class<T> type);
    void setCache(String key, Object value, long timeout, TimeUnit unit);
}