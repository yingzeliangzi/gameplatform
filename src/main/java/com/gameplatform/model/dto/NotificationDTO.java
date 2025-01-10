package com.gameplatform.model.dto;

import com.gameplatform.model.entity.Notification;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:42
 * @description TODO
 */
@Data
public class NotificationDTO {
    private Long id;
    private String title;
    private String content;
    private Notification.NotificationType type;
    private String targetType;
    private Long targetId;
    private boolean read;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // 根据通知类型返回目标链接
    public String getTargetUrl() {
        if (this.targetId == null) {
            return null;
        }

        return switch (this.targetType) {
            case "GAME" -> "/games/" + this.targetId;
            case "EVENT" -> "/events/" + this.targetId;
            case "POST" -> "/posts/" + this.targetId;
            default -> null;
        };
    }
}

