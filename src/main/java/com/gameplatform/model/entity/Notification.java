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

    // 创建系统通知的静态方法
    public static Notification createSystemNotification(String title, String content, User user) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setTargetType("SYSTEM");
        return notification;
    }

    // 创建游戏折扣通知的静态方法
    public static Notification createGameDiscountNotification(String title, String content, User user, Game game) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.GAME_DISCOUNT);
        notification.setTargetType("GAME");
        notification.setTargetId(game.getId());
        return notification;
    }

    // 创建活动提醒通知的静态方法
    public static Notification createEventReminderNotification(String title, String content, User user, Event event) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.EVENT_REMINDER);
        notification.setTargetType("EVENT");
        notification.setTargetId(event.getId());
        return notification;
    }

    // 创建帖子回复通知的静态方法
    public static Notification createPostReplyNotification(String title, String content, User user, Post post) {
        Notification notification = new Notification();
        notification.setTitle(title);
        notification.setContent(content);
        notification.setUser(user);
        notification.setType(NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        return notification;
    }
}
