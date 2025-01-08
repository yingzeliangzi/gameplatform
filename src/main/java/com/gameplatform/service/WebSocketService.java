package com.gameplatform.service;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:44
 * @description TODO
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;
    private final CacheService cacheService;

    private static final String WS_SESSION_PREFIX = "ws:session:";
    private static final String WS_USER_PREFIX = "ws:user:";
    private static final int MAX_RETRY_ATTEMPTS = 3;

    public void sendNotification(Long userId, NotificationDTO notification) {
        try {
            String destination = "/user/" + userId + "/queue/notifications";
            sendWithRetry(destination, notification);
            log.debug("通知发送成功: userId={}, type={}", userId, notification.getType());
        } catch (Exception e) {
            log.error("发送通知失败: userId={}, error={}", userId, e.getMessage());
            handleSendError(userId, notification);
        }
    }

    public void sendUnreadCount(Long userId, UnreadCountDTO unreadCount) {
        try {
            String destination = "/user/" + userId + "/queue/notifications/unread";
            sendWithRetry(destination, unreadCount);
            log.debug("未读计数发送成功: userId={}", userId);
        } catch (Exception e) {
            log.error("发送未读计数失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    public void broadcastSystemMessage(String message) {
        try {
            messagingTemplate.convertAndSend("/topic/system",
                    Map.of("type", "SYSTEM", "content", message));
            log.debug("系统消息广播成功");
        } catch (Exception e) {
            log.error("系统消息广播失败: {}", e.getMessage());
        }
    }

    public void sendUserStatusUpdate(Long userId, String status) {
        try {
            messagingTemplate.convertAndSend("/topic/user-status",
                    Map.of("userId", userId, "status", status));

            // 缓存用户状态
            cacheService.setCache(WS_USER_PREFIX + userId, status, 1, TimeUnit.HOURS);

            log.debug("用户状态更新发送成功: userId={}, status={}", userId, status);
        } catch (Exception e) {
            log.error("用户状态更新发送失败: userId={}, error={}", userId, e.getMessage());
        }
    }

    private void sendWithRetry(String destination, Object payload) {
        int attempts = 0;
        while (attempts < MAX_RETRY_ATTEMPTS) {
            try {
                messagingTemplate.convertAndSend(destination, payload);
                return;
            } catch (Exception e) {
                attempts++;
                if (attempts == MAX_RETRY_ATTEMPTS) {
                    throw e;
                }
                try {
                    Thread.sleep(1000 * attempts); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试中断", ie);
                }
            }
        }
    }

    private void handleSendError(Long userId, NotificationDTO notification) {
        // 存储失败的消息以便稍后重试
        String cacheKey = WS_USER_PREFIX + userId + ":failed_messages";
        try {
            List<NotificationDTO> failedMessages = cacheService.getCache(cacheKey, List.class)
                    .orElse(new ArrayList<>());
            failedMessages.add(notification);
            cacheService.setCache(cacheKey, failedMessages, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            log.error("存储失败消息失败: {}", e.getMessage());
        }
    }

    // 会话管理
    public void registerSession(String sessionId, Long userId) {
        try {
            cacheService.setCache(WS_SESSION_PREFIX + sessionId, userId, 24, TimeUnit.HOURS);
            cacheService.setCache(WS_USER_PREFIX + userId + ":session", sessionId, 24, TimeUnit.HOURS);
            log.debug("WebSocket会话注册成功: sessionId={}, userId={}", sessionId, userId);
        } catch (Exception e) {
            log.error("WebSocket会话注册失败: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    public void removeSession(String sessionId) {
        try {
            Optional<Long> userId = cacheService.getCache(WS_SESSION_PREFIX + sessionId, Long.class);
            userId.ifPresent(uid -> {
                cacheService.delete(WS_SESSION_PREFIX + sessionId);
                cacheService.delete(WS_USER_PREFIX + uid + ":session");
                sendUserStatusUpdate(uid, "OFFLINE");
            });
            log.debug("WebSocket会话移除成功: sessionId={}", sessionId);
        } catch (Exception e) {
            log.error("WebSocket会话移除失败: sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    // 定时任务：重试发送失败的消息
    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void retryFailedMessages() {
        try {
            Set<String> failedMessageKeys = cacheService.getKeysByPattern(WS_USER_PREFIX + "*:failed_messages");
            for (String key : failedMessageKeys) {
                List<NotificationDTO> failedMessages = cacheService.getCache(key, List.class).orElse(null);
                if (failedMessages != null && !failedMessages.isEmpty()) {
                    Long userId = extractUserIdFromKey(key);
                    retryMessages(userId, failedMessages);
                }
            }
        } catch (Exception e) {
            log.error("重试失败消息时出错: {}", e.getMessage());
        }
    }

    private Long extractUserIdFromKey(String key) {
        String[] parts = key.split(":");
        return Long.parseLong(parts[2]);
    }

    private void retryMessages(Long userId, List<NotificationDTO> messages) {
        List<NotificationDTO> remainingMessages = new ArrayList<>();
        for (NotificationDTO message : messages) {
            try {
                sendNotification(userId, message);
            } catch (Exception e) {
                remainingMessages.add(message);
            }
        }

        String cacheKey = WS_USER_PREFIX + userId + ":failed_messages";
        if (remainingMessages.isEmpty()) {
            cacheService.delete(cacheKey);
        } else {
            cacheService.setCache(cacheKey, remainingMessages, 24, TimeUnit.HOURS);
        }
    }
}