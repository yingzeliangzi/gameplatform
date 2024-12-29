package com.gameplatform.service.impl;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.*;
import com.gameplatform.service.NotificationService;
import com.gameplatform.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:43
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final EventRepository eventRepository;
    private final PostRepository postRepository;
    private final WebSocketService webSocketService;

    @Override
    @Transactional
    public void sendNotification(Notification notification) {
        Notification savedNotification = notificationRepository.save(notification);
        // 发送WebSocket消息
        webSocketService.sendNotification(
                notification.getUser().getId(),
                convertToDTO(savedNotification)
        );
    }

    @Override
    @Transactional
    public void sendNotifications(Notification notification, Iterable<Long> userIds) {
        List<Notification> notifications = new ArrayList<>();
        userIds.forEach(userId -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            Notification userNotification = new Notification();
            userNotification.setTitle(notification.getTitle());
            userNotification.setContent(notification.getContent());
            userNotification.setType(notification.getType());
            userNotification.setTargetType(notification.getTargetType());
            userNotification.setTargetId(notification.getTargetId());
            userNotification.setUser(user);
            notifications.add(userNotification);
        });

        List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
        // 发送WebSocket消息
        savedNotifications.forEach(n ->
                webSocketService.sendNotification(
                        n.getUser().getId(),
                        convertToDTO(n)
                )
        );
    }

    @Override
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<NotificationDTO> getNotificationsByType(
            Long userId,
            Notification.NotificationType type,
            Pageable pageable
    ) {
        return notificationRepository
                .findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public UnreadCountDTO getUnreadCount(Long userId) {
        UnreadCountDTO countDTO = new UnreadCountDTO();
        countDTO.setTotal(notificationRepository.countUnreadByUserId(userId));
        countDTO.setSystem(notificationRepository.countUnreadByUserIdAndType(
                userId, Notification.NotificationType.SYSTEM
        ));
        countDTO.setGameDiscount(notificationRepository.countUnreadByUserIdAndType(
                userId, Notification.NotificationType.GAME_DISCOUNT
        ));
        countDTO.setEventReminder(notificationRepository.countUnreadByUserIdAndType(
                userId, Notification.NotificationType.EVENT_REMINDER
        ));
        countDTO.setPostReply(notificationRepository.countUnreadByUserIdAndType(
                userId, Notification.NotificationType.POST_REPLY
        ));
        return countDTO;
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to mark this notification as read");
        }

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        // 更新WebSocket未读消息数
        webSocketService.sendUnreadCount(userId, getUnreadCount(userId));
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
        // 更新WebSocket未读消息数
        webSocketService.sendUnreadCount(userId, getUnreadCount(userId));
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new RuntimeException("Not authorized to delete this notification");
        }

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public void sendGameDiscountNotification(Long gameId, String discountInfo) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));

        // 为所有拥有该游戏的用户发送通知
        List<User> users = userRepository.findByOwnedGamesContaining(game);
        users.forEach(user -> {
            Notification notification = Notification.createGameDiscountNotification(
                    game.getTitle() + " 优惠提醒",
                    discountInfo,
                    user,
                    game
            );
            sendNotification(notification);
        });
    }

    @Override
    @Transactional
    public void sendEventReminder(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        // 为所有报名的用户发送提醒
        List<User> registeredUsers = userRepository.findByRegisteredEventsContaining(event);
        registeredUsers.forEach(user -> {
            Notification notification = Notification.createEventReminderNotification(
                    event.getTitle() + " 即将开始",
                    String.format("您报名的活动「%s」将在24小时后开始，请准时参加！", event.getTitle()),
                    user,
                    event
            );
            sendNotification(notification);
        });
    }

    @Override
    @Transactional
    public void sendPostReplyNotification(Long postId, Long replyUserId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        User replyUser = userRepository.findById(replyUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = Notification.createPostReplyNotification(
                "收到新回复",
                String.format("%s 回复了您的帖子「%s」", replyUser.getNickname(), post.getTitle()),
                post.getAuthor(),
                post
        );
        sendNotification(notification);
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setContent(notification.getContent());
        dto.setType(notification.getType());
        dto.setTargetType(notification.getTargetType());
        dto.setTargetId(notification.getTargetId());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setReadAt(notification.getReadAt());
        return dto;
    }
}
