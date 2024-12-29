package com.gameplatform.service;

import com.gameplatform.model.entity.*;
import com.gameplatform.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:43
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    @Async
    @Transactional
    public void sendEventNotification(User user, Event event, String message) {
        Notification notification = Notification.createEventReminderNotification(
                event.getTitle(),
                message,
                user,
                event
        );
        notificationRepository.save(notification);
    }

    @Async
    @Transactional
    public void sendRegistrationConfirmation(EventRegistration registration) {
        Notification notification = Notification.createEventReminderNotification(
                "报名确认",
                String.format("您已成功报名活动：%s", registration.getEvent().getTitle()),
                registration.getUser(),
                registration.getEvent()
        );
        notificationRepository.save(notification);
    }

    @Async
    @Transactional
    public void sendEventReminder(EventRegistration registration) {
        String message = String.format(
                "您报名的活动 %s 将在24小时内开始",
                registration.getEvent().getTitle()
        );
        Notification notification = Notification.createEventReminderNotification(
                "活动提醒",
                message,
                registration.getUser(),
                registration.getEvent()
        );
        notificationRepository.save(notification);
    }
}