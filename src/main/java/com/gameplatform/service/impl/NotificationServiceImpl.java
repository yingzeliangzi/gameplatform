package com.gameplatform.service.impl;

import com.gameplatform.config.properties.NotificationProperties;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.model.message.NotificationMessage;
import com.gameplatform.repository.NotificationRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.EventService;
import com.gameplatform.service.NotificationService;
import com.gameplatform.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
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
// 路径：F:\yingzeliangzi\Desktop\gameplatform\src\main\java\com\gameplatform\service\impl\NotificationServiceImpl.java

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final WebSocketService webSocketService;
    private final JmsTemplate jmsTemplate;
    private final EventService eventService;
    private final NotificationProperties notificationProperties;

    @Value("${notification.queue}")
    private String notificationQueue;

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
        try {
            webSocketService.sendNotification(userId, convertToDTO(savedNotification));
        } catch (Exception e) {
            log.error("WebSocket发送通知失败: {}", e.getMessage());
        }

        // 发送消息队列
        try {
            NotificationMessage message = new NotificationMessage(userId, notificationDTO);
            jmsTemplate.convertAndSend(notificationQueue, message);
        } catch (Exception e) {
            log.error("消息队列发送通知失败: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getUserNotifications(Long userId, Pageable pageable) {
        validateUser(userId);
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationDTO> getNotificationsByType(Long userId,
                                                        Notification.NotificationType type, Pageable pageable) {
        validateUser(userId);
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
        validateUser(userId);
        notificationRepository.markAllAsRead(userId);
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
        validateUser(userId);
        UnreadCountDTO dto = new UnreadCountDTO();
        dto.setTotal(notificationRepository.countUnreadByUserId(userId));
        dto.setSystem(countUnreadByType(userId, Notification.NotificationType.SYSTEM));
        dto.setGameDiscount(countUnreadByType(userId, Notification.NotificationType.GAME_DISCOUNT));
        dto.setEventReminder(countUnreadByType(userId, Notification.NotificationType.EVENT_REMINDER));
        dto.setPostReply(countUnreadByType(userId, Notification.NotificationType.POST_REPLY));
        return dto;
    }

    @Override
    @Transactional
    public void sendCommentReplyNotification(User user, Post post, String commenterName) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("评论回复");
        notification.setContent(String.format("%s 评论了您的帖子「%s」", commenterName, post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    @Override
    @Transactional
    public void sendEventReminder(EventRegistration registration) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("活动提醒");
        notification.setContent(String.format("您报名的活动「%s」即将开始",
                registration.getEvent().getTitle()));
        notification.setType(Notification.NotificationType.EVENT_REMINDER);
        notification.setTargetType("EVENT");
        notification.setTargetId(registration.getEvent().getId());
        sendNotification(registration.getUser().getId(), notification);
    }

    @Override
    @Transactional
    public void sendRegistrationConfirmation(EventRegistration registration) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("报名确认");
        notification.setContent(String.format("您已成功报名活动「%s」，请准时参加",
                registration.getEvent().getTitle()));
        notification.setType(Notification.NotificationType.EVENT_REMINDER);
        notification.setTargetType("EVENT");
        notification.setTargetId(registration.getEvent().getId());
        sendNotification(registration.getUser().getId(), notification);
    }

    @Override
    @Transactional
    public void sendNewCommentNotification(User user, Post post, String commenterName) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("新评论通知");
        notification.setContent(String.format("%s 评论了您的帖子「%s」",
                commenterName, post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    @Override
    @Transactional
    public void sendNewPostNotification(User follower, Post post) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("关注动态");
        notification.setContent(String.format("您关注的用户 %s 发布了新帖子「%s」",
                post.getAuthor().getNickname(), post.getTitle()));
        notification.setType(Notification.NotificationType.SYSTEM);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(follower.getId(), notification);
    }

    @Override
    @Transactional
    public void sendCommentNotification(User user, Post post, User commenter) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("评论通知");
        notification.setContent(String.format("%s 评论了您的帖子「%s」",
                commenter.getNickname(), post.getTitle()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(post.getId());
        sendNotification(user.getId(), notification);
    }

    @Override
    @Transactional
    public void sendReplyNotification(User user, Comment parentComment, User replier) {
        NotificationDTO notification = new NotificationDTO();
        notification.setTitle("回复通知");
        notification.setContent(String.format("%s 回复了您的评论", replier.getNickname()));
        notification.setType(Notification.NotificationType.POST_REPLY);
        notification.setTargetType("POST");
        notification.setTargetId(parentComment.getPost().getId());
        sendNotification(user.getId(), notification);
    }

    // 私有辅助方法
    private long countUnreadByType(Long userId, Notification.NotificationType type) {
        return notificationRepository.countUnreadByUserIdAndType(userId, type);
    }

    private void updateUnreadCount(Long userId) {
        UnreadCountDTO unreadCount = getUnreadCount(userId);
        try {
            webSocketService.sendUnreadCount(userId, unreadCount);
        } catch (Exception e) {
            log.error("发送未读消息计数失败: {}", e.getMessage());
        }
    }

    private Notification findByIdAndUserId(Long notificationId, Long userId) {
        return notificationRepository.findById(notificationId)
                .filter(n -> n.getUser().getId().equals(userId))
                .orElseThrow(() -> new BusinessException("通知不存在或无权访问"));
    }

    private void validateUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new BusinessException("用户不存在");
        }
    }

    private NotificationDTO convertToDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        BeanUtils.copyProperties(notification, dto);
        dto.setType(notification.getType());

        // 根据不同类型设置额外信息
        switch (notification.getType()) {
            case EVENT_REMINDER -> {
                Event event = eventService.getEventById(notification.getTargetId(), null);
                dto.setTargetUrl("/events/" + event.getId());
            }
            case POST_REPLY -> dto.setTargetUrl("/posts/" + notification.getTargetId());
            case GAME_DISCOUNT -> dto.setTargetUrl("/games/" + notification.getTargetId());
        }

        return dto;
    }

    // 定时任务：清理过期通知
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点执行
    @Transactional
    public void cleanExpiredNotifications() {
        try {
            LocalDateTime threshold = LocalDateTime.now().minusMonths(3); // 3个月前的通知
            int deletedCount = notificationRepository.deleteByCreatedAtBefore(threshold);
            log.info("清理过期通知完成，共删除 {} 条通知", deletedCount);
        } catch (Exception e) {
            log.error("清理过期通知失败: {}", e.getMessage());
        }
    }
}