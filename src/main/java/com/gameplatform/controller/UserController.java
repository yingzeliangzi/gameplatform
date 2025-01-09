package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.PasswordUpdateDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.model.dto.UserProfileDTO;
import com.gameplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:08
 * @description TODO
 */
@Tag(name = "用户接口", description = "用户相关操作接口")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @GetMapping("/me")
    public Result<UserDTO> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(userService.getCurrentUser(userDetails.getUsername()));
    }

    @Operation(summary = "更新个人资料", description = "更新当前用户的个人资料")
    @PutMapping("/me")
    @RequirePermission("user:update")
    public Result<UserDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "用户资料", required = true)
            @Valid @RequestBody UserProfileDTO userProfileDTO) {
        Long userId = Long.parseLong(userDetails.getUsername());
        UserDTO userDTO = new UserDTO();
        userDTO.setNickname(userProfileDTO.getNickname());
        userDTO.setBio(userProfileDTO.getBio());
        return Result.success(userService.updateUser(userId, userDTO));
    }

    @Operation(summary = "上传头像", description = "上传用户头像")
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission("user:avatar")
    public Result<String> updateAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "头像文件", required = true)
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.parseLong(userDetails.getUsername());
        String avatarUrl = userService.updateAvatar(userId, file);
        return Result.success(avatarUrl);
    }

    @Operation(summary = "更新密码", description = "更新用户密码")
    @PutMapping("/me/password")
    @RequirePermission("user:password")
    public Result<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "密码信息", required = true)
            @Valid @RequestBody PasswordUpdateDTO passwordDTO) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.updatePassword(userId, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "验证邮箱", description = "验证用户邮箱")
    @PostMapping("/me/email/verify")
    @RequirePermission("user:email")
    public Result<Void> verifyEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "验证码", required = true)
            @NotBlank(message = "验证码不能为空") String code) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.verifyEmail(userId, code);
        return Result.success();
    }

    @Operation(summary = "获取用户资料", description = "获取指定用户的公开资料")
    @GetMapping("/{id}")
    public Result<UserDTO> getUserProfile(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id) {
        return Result.success(userService.getUserProfile(id));
    }

    @Operation(summary = "关注用户", description = "关注指定用户")
    @PostMapping("/{id}/follow")
    @RequirePermission("user:follow")
    public Result<Void> followUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.followUser(userId, id);
        return Result.success();
    }

    @Operation(summary = "取消关注", description = "取消关注指定用户")
    @DeleteMapping("/{id}/follow")
    @RequirePermission("user:follow")
    public Result<Void> unfollowUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "目标用户ID", required = true)
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        userService.unfollowUser(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取关注者列表", description = "获取用户的关注者列表")
    @GetMapping("/{id}/followers")
    public Result<Page<UserDTO>> getFollowers(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return Result.success(userService.getUserFollowers(id, pageable));
    }

    @Operation(summary = "获取关注列表", description = "获取用户关注的用户列表")
    @GetMapping("/{id}/following")
    public Result<Page<UserDTO>> getFollowing(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        return Result.success(userService.getUserFollowing(id, pageable));
    }
}