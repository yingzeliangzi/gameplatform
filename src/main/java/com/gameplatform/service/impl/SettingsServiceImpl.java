package com.gameplatform.service.impl;

import com.gameplatform.model.entity.Setting;
import com.gameplatform.model.entity.UserSetting;
import com.gameplatform.repository.SettingRepository;
import com.gameplatform.repository.UserSettingRepository;
import com.gameplatform.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:44
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {

    private final SettingRepository settingRepository;
    private final UserSettingRepository userSettingRepository;

    @Override
    @Cacheable(value = "settings", key = "'system'")
    public Map<String, String> getSystemSettings() {
        List<Setting> settings = settingRepository.findAll();
        return settings.stream()
                .collect(Collectors.toMap(Setting::getKey, Setting::getValue));
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", key = "'system'")
    public void updateSystemSettings(Map<String, String> settings) {
        settings.forEach((key, value) -> {
            Setting setting = settingRepository.findByKey(key)
                    .orElse(new Setting());
            setting.setKey(key);
            setting.setValue(value);
            settingRepository.save(setting);
        });
    }

    @Override
    @Cacheable(value = "settings", key = "'user:' + #userId")
    public Map<String, String> getUserSettings(Long userId) {
        List<UserSetting> settings = userSettingRepository.findByUserId(userId);
        return settings.stream()
                .collect(Collectors.toMap(UserSetting::getKey, UserSetting::getValue));
    }

    @Override
    @Transactional
    @CacheEvict(value = "settings", key = "'user:' + #userId")
    public void updateUserSettings(Long userId, Map<String, String> settings) {
        settings.forEach((key, value) -> {
            UserSetting setting = userSettingRepository.findByUserIdAndKey(userId, key)
                    .orElse(new UserSetting());
            setting.setUserId(userId);
            setting.setKey(key);
            setting.setValue(value);
            userSettingRepository.save(setting);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public String getSettingValue(String key) {
        return settingRepository.findByKey(key)
                .map(Setting::getValue)
                .orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public String getUserSettingValue(Long userId, String key) {
        return userSettingRepository.findByUserIdAndKey(userId, key)
                .map(UserSetting::getValue)
                .orElse(null);
    }

    @Override
    @Transactional
    public void initializeDefaultSettings() {
        Map<String, String> defaults = getDefaultSettings();
        defaults.forEach((key, value) -> {
            if (!settingRepository.existsByKey(key)) {
                Setting setting = new Setting();
                setting.setKey(key);
                setting.setValue(value);
                settingRepository.save(setting);
            }
        });
    }

    @Override
    @Transactional
    public void initializeUserSettings(Long userId) {
        Map<String, String> defaults = getDefaultUserSettings();
        defaults.forEach((key, value) -> {
            if (!userSettingRepository.existsByUserIdAndKey(userId, key)) {
                UserSetting setting = new UserSetting();
                setting.setUserId(userId);
                setting.setKey(key);
                setting.setValue(value);
                userSettingRepository.save(setting);
            }
        });
    }

    private Map<String, String> getDefaultSettings() {
        Map<String, String> defaults = new HashMap<>();
        // 系统设置默认值
        defaults.put("site.name", "游戏社区平台");
        defaults.put("site.description", "一个游戏爱好者的交流平台");
        defaults.put("register.enabled", "true");
        defaults.put("register.verification", "true");
        defaults.put("upload.max-size", "10485760");  // 10MB
        defaults.put("notification.email.enabled", "true");
        defaults.put("maintenance.mode", "false");
        return defaults;
    }

    private Map<String, String> getDefaultUserSettings() {
        Map<String, String> defaults = new HashMap<>();
        // 用户设置默认值
        defaults.put("theme", "light");
        defaults.put("language", "zh_CN");
        defaults.put("notification.email", "true");
        defaults.put("notification.browser", "true");
        defaults.put("privacy.profile", "public");
        defaults.put("privacy.game-library", "friends");
        return defaults;
    }
}