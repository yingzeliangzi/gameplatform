package com.gameplatform.controller;

import com.gameplatform.common.Result;
import com.gameplatform.model.dto.LoginRequestDTO;
import com.gameplatform.model.dto.LoginResponseDTO;
import com.gameplatform.model.dto.RegisterRequestDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:22
 * @description TODO
 */
@Tag(name = "认证接口", description = "用户认证相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final UserService userService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    @PostMapping("/login")
    public Result<LoginResponseDTO> login(
            @Parameter(description = "登录信息", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        LoginResponseDTO response = userService.login(loginRequest.getUsername(), loginRequest.getPassword());
        return Result.success(response);
    }

    @Operation(summary = "用户注册", description = "注册新用户")
    @PostMapping("/register")
    public Result<UserDTO> register(
            @Parameter(description = "注册信息", required = true)
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        UserDTO registeredUser = userService.register(registerRequest);
        return Result.success(registeredUser);
    }

    @Operation(summary = "发送验证码", description = "发送邮箱验证码")
    @PostMapping("/verification-code")
    public Result<Void> sendVerificationCode(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") String email) {
        userService.sendVerificationCode(email);
        return Result.success();
    }

    @Operation(summary = "重置密码", description = "通过邮箱重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(
            @Parameter(description = "邮箱地址", required = true)
            @RequestParam @Email(message = "邮箱格式不正确") String email) {
        userService.resetPassword(email);
        return Result.success();
    }

    @Operation(summary = "用户登出", description = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            userService.logout(auth.getName());
        }
        return Result.success();
    }

    @Operation(summary = "获取当前用户信息", description = "获取登录用户的详细信息")
    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return Result.error(401, "未登录");
        }
        UserDTO userInfo = userService.loadUserByUsername(auth.getName());
        return Result.success(userInfo);
    }

    @Operation(summary = "刷新token", description = "刷新用户认证token")
    @PostMapping("/refresh-token")
    public Result<String> refreshToken(
            @Parameter(description = "原token", required = true)
            @RequestParam @NotBlank(message = "token不能为空") String token) {
        String newToken = userService.refreshToken(token);
        return Result.success(newToken);
    }

    @Operation(summary = "验证Token", description = "验证token是否有效")
    @GetMapping("/validate-token")
    public Result<Boolean> validateToken(
            @Parameter(description = "token", required = true)
            @RequestParam @NotBlank(message = "token不能为空") String token) {
        return Result.success(userService.validateToken(token));
    }
}