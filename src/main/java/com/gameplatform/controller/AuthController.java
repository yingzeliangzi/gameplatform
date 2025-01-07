package com.gameplatform.controller;

import com.gameplatform.common.Result;
import com.gameplatform.model.dto.LoginRequestDTO;
import com.gameplatform.model.dto.LoginResponseDTO;
import com.gameplatform.model.dto.RegisterRequestDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:22
 * @description TODO
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/login")
    public Result<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDTO userDTO = userService.login(loginRequest.getUsername(), loginRequest.getPassword());

            LoginResponseDTO response = new LoginResponseDTO();
            response.setToken(userDTO.getToken());
            response.setUser(userDTO);

            return Result.success(response);
        } catch (BadCredentialsException e) {
            return Result.error(401, "用户名或密码错误");
        }
    }

    @PostMapping("/register")
    public Result<UserDTO> register(@Valid @RequestBody RegisterRequestDTO registerRequest) {
        UserDTO registeredUser = userService.register(registerRequest);
        return Result.success(registeredUser);
    }

    @PostMapping("/verification-code")
    public Result<Void> sendVerificationCode(@RequestParam String email) {
        userService.sendVerificationCode(email);
        return Result.success("验证码已发送");
    }

    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestParam String email) {
        userService.resetPassword(email);
        return Result.success("新密码已发送到您的邮箱");
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout(SecurityContextHolder.getContext().getAuthentication().getName());
        return Result.success("登出成功");
    }

    @GetMapping("/info")
    public Result<UserDTO> getUserInfo() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        UserDTO userInfo = userService.loadUserByUsername(username);
        return Result.success(userInfo);
    }
}