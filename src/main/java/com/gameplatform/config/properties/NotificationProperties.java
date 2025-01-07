package com.gameplatform.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/06 1:11
 * @description TODO
 */
@Data
@Component
@ConfigurationProperties(prefix = "notification")
public class NotificationProperties {
    private List<String> emailTypes;
    private String queue;
}