package com.gameplatform.service;

import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:11
 * @description TODO
 */
public interface SettingsService {
    Map<String, Object> getSystemSettings();
    void updateSystemSettings(Map<String, Object> settings);
    String getSettingValue(String key);
    void setSetting(String key, String value);
}
