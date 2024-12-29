package com.gameplatform.service;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:44
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class WebSocketService {
    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(Long userId, NotificationDTO notification) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/topic/notifications",
                notification
        );
    }

    public void sendUnreadCount(Long userId, UnreadCountDTO unreadCount) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/topic/notifications/unread",
                unreadCount
        );
    }
}
