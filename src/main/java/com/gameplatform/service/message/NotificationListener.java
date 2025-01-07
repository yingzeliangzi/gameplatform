package com.gameplatform.service.message;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.entity.User;
import com.gameplatform.model.entity.UserSetting;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:37
 * @description TODO
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationListener {

    private final EmailService emailService;
    private final UserRepository userRepository;
    private final NotificationProperties notificationProperties;

    @JmsListener(destination = "${notification.queue}")
    public void handleNotification(NotificationMessage message) {
        try {
            if (shouldSendEmail(message)) {
                sendEmailNotification(message);
            }

            // 处理其他异步任务...

        } catch (Exception e) {
            log.error("处理通知消息失败", e);
        }
    }

    private boolean shouldSendEmail(NotificationMessage message) {
        User user = userRepository.findById(message.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 检查用户的邮件通知设置
        UserSetting emailSetting = user.getSetting("email_notification");
        if (emailSetting == null || !Boolean.parseBoolean(emailSetting.getValue())) {
            return false;
        }

        // 检查通知类型是否需要发送邮件
        return notificationProperties.getEmailTypes().contains(message.getType());
    }

    private void sendEmailNotification(NotificationMessage message) {
        User user = userRepository.findById(message.getUserId()).orElseThrow();

        String emailContent = createEmailContent(message);

        emailService.sendHtmlEmail(
                user.getEmail(),
                message.getTitle(),
                emailContent
        );
    }

    private String createEmailContent(NotificationMessage message) {
        return """
            <div style="padding: 20px; background-color: #f5f7fa;">
                <h2 style="color: #409EFF;">%s</h2>
                <div style="margin: 20px 0;">%s</div>
                <div style="color: #909399; font-size: 12px;">
                    此邮件由系统自动发送，请勿回复
                </div>
            </div>
            """.formatted(message.getTitle(), message.getContent());
    }
}