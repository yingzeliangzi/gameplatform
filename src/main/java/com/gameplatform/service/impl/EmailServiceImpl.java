package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/06 20:36
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    @Async
    public void sendVerificationEmail(String to, String code) {
        Context context = new Context();
        context.setVariable("verificationCode", code);
        String content = templateEngine.process("verification-email", context);
        sendHtmlEmail(to, "验证码", content);
    }

    @Override
    @Async
    public void sendPasswordResetEmail(String to, String token) {
        Context context = new Context();
        context.setVariable("resetToken", token);
        String content = templateEngine.process("password-reset-email", context);
        sendHtmlEmail(to, "密码重置", content);
    }

    @Override
    @Async
    public void sendWelcomeEmail(String to, String username) {
        Context context = new Context();
        context.setVariable("username", username);
        String content = templateEngine.process("welcome-email", context);
        sendHtmlEmail(to, "欢迎加入", content);
    }

    @Override
    @Async
    public void sendPasswordChangeNotification(String to) {
        Context context = new Context();
        String content = templateEngine.process("password-change-notification", context);
        sendHtmlEmail(to, "密码修改通知", content);
    }

    @Override
    public void sendHtmlEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name());

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new BusinessException("发送邮件失败：" + e.getMessage());
        }
    }
}