package com.gameplatform.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;


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
    private final ConcurrentHashMap<String, String> verificationCodes = new ConcurrentHashMap<>();

    @Async
    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("发送邮件失败", e);
        }
    }

    public void sendVerificationCode(String email) {
        String code = generateVerificationCode();
        verificationCodes.put(email, code);
        sendSimpleEmail(email, "验证码", "您的验证码是：" + code);
    }

    public void sendPasswordResetEmail(String email, String newPassword) {
        String content = String.format("""
            <div style="padding: 20px;">
                <h2>密码重置</h2>
                <p>您的新密码是：<strong>%s</strong></p>
                <p>请登录后立即修改密码。</p>
            </div>
            """, newPassword);
        sendHtmlEmail(email, "密码重置", content);
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);
        return storedCode != null && storedCode.equals(code);
    }

    private String generateVerificationCode() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}