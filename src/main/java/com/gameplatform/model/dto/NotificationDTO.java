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
    private boolean isRead;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;

    // 根据通知类型返回目标链接
    public String getTargetUrl() {
        if (targetId == null) return null;

        switch (targetType) {
            case "GAME":
                return "/games/" + targetId;
            case "EVENT":
                return "/events/" + targetId;
            case "POST":
                return "/community/posts/" + targetId;
            default:
                return null;
        }
    }
}

