package com.gameplatform.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:22
 * @description TODO
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    // 发送验证码
    @PostMapping("/verification-code")
    public ResponseEntity<?> sendVerificationCode(@RequestParam String email) {
        userService.sendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    // 密码重置请求
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email) {
        userService.resetPassword(email);
        return ResponseEntity.ok("新密码已发送到您的邮箱");
    }
}
