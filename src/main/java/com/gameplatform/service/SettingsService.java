package com.gameplatform.service;

import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:11
 * @description TODO
 */
public interface SettingsService {
    Map<String, String> getSystemSettings();
    void updateSystemSettings(Map<String, String> settings);
    Map<String, String> getUserSettings(Long userId);
    void updateUserSettings(Long userId, Map<String, String> settings);
    String getSettingValue(String key);
    String getUserSettingValue(Long userId, String key);
    void initializeDefaultSettings();
    void initializeUserSettings(Long userId);
}
