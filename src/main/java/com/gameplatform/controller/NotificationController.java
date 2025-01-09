package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.dto.UnreadCountDTO;
import com.gameplatform.model.entity.Notification;
import com.gameplatform.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:44
 * @description TODO
 */
@Tag(name = "通知接口", description = "通知相关操作接口")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "获取通知列表", description = "分页获取通知列表，支持按类型过滤")
    @GetMapping
    @RequirePermission("notification:view")
    public Result<Page<NotificationDTO>> getNotifications(
            @Parameter(description = "通知类型")
            @RequestParam(required = false) Notification.NotificationType type,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        if (type != null) {
            return Result.success(notificationService.getNotificationsByType(userId, type, pageable));
        }
        return Result.success(notificationService.getUserNotifications(userId, pageable));
    }

    @Operation(summary = "获取未读通知数量", description = "获取未读通知数量统计")
    @GetMapping("/unread")
    @RequirePermission("notification:view")
    public Result<UnreadCountDTO> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记通知为已读", description = "将指定通知标记为已读")
    @PatchMapping("/{id}/read")
    @RequirePermission("notification:update")
    public Result<Void> markAsRead(
            @Parameter(description = "通知ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.markAsRead(id, userId);
        return Result.success();
    }

    @Operation(summary = "标记全部已读", description = "将所有通知标记为已读")
    @PatchMapping("/read-all")
    @RequirePermission("notification:update")
    public Result<Void> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "删除通知", description = "删除指定通知")
    @DeleteMapping("/{id}")
    @RequirePermission("notification:delete")
    public Result<Void> deleteNotification(
            @Parameter(description = "通知ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.deleteNotification(id, userId);
        return Result.success();
    }

    @Operation(summary = "更新通知设置", description = "更新用户的通知设置")
    @PutMapping("/settings")
    @RequirePermission("notification:settings")
    public Result<Void> updateNotificationSettings(
            @Parameter(description = "通知设置", required = true)
            @RequestBody @Validated NotificationSettingsDTO settings,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        notificationService.updateNotificationSettings(userId, settings);
        return Result.success();
    }

    @Operation(summary = "获取通知设置", description = "获取用户的通知设置")
    @GetMapping("/settings")
    @RequirePermission("notification:settings")
    public Result<NotificationSettingsDTO> getNotificationSettings(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(notificationService.getNotificationSettings(userId));
    }
}