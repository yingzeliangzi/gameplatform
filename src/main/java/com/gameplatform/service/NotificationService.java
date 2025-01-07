package com.gameplatform.service;

import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import com.gameplatform.model.entity.Notification;
import com.gameplatform.model.entity.Post;
import com.gameplatform.model.entity.User;
import com.gameplatform.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:43
 * @description TODO
 */
public interface NotificationService {
    void sendNotification(Long userId, NotificationDTO notificationDTO);

    Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable);

    Page<NotificationDTO> getNotificationsByType(Long userId, Notification.NotificationType type, Pageable pageable);

    void markAsRead(Long notificationId, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long notificationId, Long userId);

    UnreadCountDTO getUnreadCount(Long userId);

    // 添加评论相关通知方法
    void sendCommentNotification(User user, Post post, User commenter);

    void sendReplyNotification(User user, Comment parentComment, User replier);

    void sendCommentReplyNotification(User user, Post post, String commenterName);

    void sendNewCommentNotification(User user, Post post, String commenterName);

    void sendNewPostNotification(User follower, Post post);
}