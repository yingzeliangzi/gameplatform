package com.gameplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:41
 * @description TODO
 */
@Data
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String targetType;

    private Long targetId;

    @Column(nullable = false)
    private boolean isRead = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime readAt;

    public enum NotificationType {
        SYSTEM,          // 系统通知
        GAME_DISCOUNT,   // 游戏折扣
        EVENT_REMINDER,  // 活动提醒
        POST_REPLY,      // 帖子回复
        POST_LIKE,       // 帖子点赞
        EVENT_REGISTER,  // 活动报名
        EVENT_CANCEL     // 活动取消
    }

    // 新增的静态工厂方法
    public static Notification createEventNotification(String title, String content, User user, Event event) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.EVENT_REMINDER);
        notification.setTargetType("EVENT");
        notification.setTargetId(event.getId());
        return notification;
    }

    public static Notification createPostNotification(String title, String content, User user, Post post) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        return notification;
    }

    public static Notification createGameNotification(String title, String content, User user, Game game) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.GAME_DISCOUNT);
        notification.setTargetType("GAME");
        notification.setTargetId(game.getId());
        return notification;
    }

    // 用于判断通知是否过期
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.createdAt.plusMonths(3));
    }

    // 用于标记通知为已读
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    // 用于获取目标URL
    public String getTargetUrl() {
        if (this.targetId == null) return null;

        return switch (this.targetType) {
            case "GAME" -> "/games/" + this.targetId;
            case "EVENT" -> "/events/" + this.targetId;
            case "POST" -> "/posts/" + this.targetId;
            default -> null;
        };
    }
}