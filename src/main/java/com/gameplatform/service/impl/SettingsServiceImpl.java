package com.gameplatform.service.impl;

import com.gameplatform.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:44
 * @description TODO
 */

@Service
@RequiredArgsConstructor
class SettingsServiceImpl implements SettingsService {
    private final RedisTemplate<String, String> redisTemplate;
    private static final String SETTINGS_PREFIX = "system:settings:";

    @Override
    public Map<String, Object> getSystemSettings() {
        Set<String> keys = redisTemplate.keys(SETTINGS_PREFIX + "*");
        Map<String, Object> settings = new HashMap<>();
        if (keys != null) {
            keys.forEach(key -> {
                String shortKey = key.substring(SETTINGS_PREFIX.length());
                settings.put(shortKey, redisTemplate.opsForValue().get(key));
            });
        }
        return settings;
    }

    @Override
    public void updateSystemSettings(Map<String, Object> settings) {
        settings.forEach((key, value) ->
                redisTemplate.opsForValue().set(SETTINGS_PREFIX + key, value.toString())
        );
    }

    @Override
    public String getSettingValue(String key) {
        return redisTemplate.opsForValue().get(SETTINGS_PREFIX + key);
    }

    @Override
    public void setSetting(String key, String value) {
        redisTemplate.opsForValue().set(SETTINGS_PREFIX + key, value);
    }
}
