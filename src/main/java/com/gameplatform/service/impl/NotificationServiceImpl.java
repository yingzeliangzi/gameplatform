package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import com.gameplatform.model.entity.Comment;
import com.gameplatform.model.entity.Notification;
import com.gameplatform.model.entity.Post;
import com.gameplatform.model.entity.User;
import com.gameplatform.model.message.NotificationMessage;
import com.gameplatform.repository.NotificationRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.NotificationService;
import com.gameplatform.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:43
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final JmsTemplate jmsTemplate;
    private final String notificationQueue = "notification.queue";

    @Override
    @Transactional
    public void sendNotification(Long userId, NotificationDTO notificationDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(notificationDTO.getTitle());
        notification.setContent(notificationDTO.getContent());
        notification.setType(Notification.NotificationType.valueOf(notificationDTO.getType().name()));
        notification.setTargetType(notificationDTO.getTargetType());
        notification.setTargetId(notificationDTO.getTargetId());
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        Notification savedNotification = notificationRepository.save(notification);

        // 发送WebSocket消息
        webSocketService.sendNotification(userId, convertToDTO(savedNotification));

        // 发送消息队列
        NotificationMessage message = new NotificationMessage(userId, notificationDTO);
        jmsTemplate.convertAndSend(notificationQueue, message);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByType(Long userId, Notification.NotificationType type, Pageable pageable) {
        return notificationRepository.findByUserIdAndTypeOrderByCreatedAtDesc(userId, type, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = findByIdAndUserId(notificationId, userId);
        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);

            // 更新未读消息计数
            updateUnreadCount(userId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);

        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        notificationRepository.saveAll(unreadNotifications);
        updateUnreadCount(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        Notification notification = findByIdAndUserId(notificationId, userId);
        notificationRepository.delete(notification);
        updateUnreadCount(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UnreadCountDTO getUnreadCount(Long userId) {
        UnreadCountDTO dto = new UnreadCountDTO();
        dto.setTotal(notificationRepository.countUnreadByUserId(userId));
        dto.setSystem(countUnreadByType(userId, Notification.NotificationType.SYSTEM));
        dto.setGameDiscount(countUnreadByType(userId, Notification.NotificationType.GAME_DISCOUNT));
        dto.setEventReminder(countUnreadByType(userId, Notification.NotificationType.EVENT_REMINDER));
        dto.setPostReply(countUnreadByType(userId, Notification.NotificationType.POST_REPLY));
        return dto;
    }

    private long countUnreadByType(Long userId, Notification.NotificationType type) {
        return notificationRepository.countUnreadByUserIdAndType(userId, type);
    }

    private void updateUnreadCount(Long userId) {
        UnreadCountDTO unreadCount = getUnreadCount(userId);
        webSocketService.sendUnreadCount(userId, unreadCount);
    }

    private Notification findByIdAndUserId(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("通知不存在或无权访问"));
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        dto.setType(notification.getType());
        return dto;
    }

    public void sendCommentNotification(User user, Post post, User commenter) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("新评论通知");
        notification.setContent(String.format("%s 评论了您的帖子 「%s」", commenter.getNickname(), post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    public void sendReplyNotification(User user, Comment parentComment, User replier) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("回复通知");
        notification.setContent(String.format("%s 回复了您的评论", replier.getNickname()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(parentComment.getPost().getId());
        sendNotification(user.getId(), notification);
    }

    public void sendCommentReplyNotification(User user, Post post, String commenterName) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("评论回复通知");
        notification.setContent(String.format("%s 回复了您在帖子「%s」中的评论", commenterName, post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    public void sendNewCommentNotification(User user, Post post, String commenterName) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("新评论通知");
        notification.setContent(String.format("%s 评论了您的帖子「%s」", commenterName, post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    public void sendNewPostNotification(User follower, Post post) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("新帖子通知");
        notification.setContent(String.format("您关注的用户 %s 发布了新帖子「%s」", post.getAuthor().getNickname(), post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(follower.getId(), notification);
    }
}