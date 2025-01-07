package com.gameplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:08
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Async
    public void sendWelcomeEmail(String to, String username) {
        String content = String.format("""
            <div style="padding: 20px;">
                <h2>欢迎加入游戏社区！</h2>
                <p>亲爱的 %s：</p>
                <p>感谢您注册成为我们的会员。我们期待与您一起分享游戏的乐趣！</p>
                <p>如有任何问题，请随时联系我们的客服团队。</p>
            </div>
            """, username);

        sendHtmlEmail(to, "欢迎加入游戏社区", content);
    }

    @Async
    public void sendPasswordChangeNotification(String to) {
        String content = """
            <div style="padding: 20px;">
                <h2>密码修改通知</h2>
                <p>您的账号密码已成功修改。</p>
                <p>如果这不是您本人的操作，请立即联系我们。</p>
            </div>
            """;

        sendHtmlEmail(to, "密码修改通知", content);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("发送邮件失败", e);
        }
    }
}