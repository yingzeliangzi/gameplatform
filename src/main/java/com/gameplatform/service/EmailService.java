package com.gameplatform.service;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:08
 * @description TODO
 */
public interface EmailService {
    void sendWelcomeEmail(String to, String username);
    void sendVerificationEmail(String to, String code);
    void sendPasswordResetEmail(String to, String newPassword);
    void sendPasswordChangeNotification(String to);
    void sendHtmlEmail(String to, String subject, String content);
}