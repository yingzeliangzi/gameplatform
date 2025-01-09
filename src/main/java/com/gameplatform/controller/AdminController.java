package com.gameplatform.controller;

import com.gameplatform.common.Result;
import com.gameplatform.model.dto.StatisticsDTO;
import com.gameplatform.model.dto.UserDTO;
import com.gameplatform.service.StatisticsService;
import com.gameplatform.service.SettingsService;
import com.gameplatform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:09
 * @description TODO
 */
@Tag(name = "管理员接口", description = "管理员相关操作接口")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
@Validated
public class AdminController {

    private final UserService userService;
    private final StatisticsService statisticsService;
    private final SettingsService settingsService;

    @Operation(summary = "获取用户列表", description = "分页获取用户列表，支持关键字搜索")
    @GetMapping("/users")
    public Result<Page<UserDTO>> getUsers(
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "分页参数") Pageable pageable) {
        return Result.success(userService.searchUsers(keyword, pageable));
    }

    @Operation(summary = "禁用用户", description = "禁用指定用户")
    @PostMapping("/users/{id}/disable")
    public Result<Void> disableUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.disableUser(id);
        return Result.success();
    }

    @Operation(summary = "启用用户", description = "启用指定用户")
    @PostMapping("/users/{id}/enable")
    public Result<Void> enableUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.enableUser(id);
        return Result.success();
    }

    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "更新用户角色", description = "更新指定用户的角色列表")
    @PostMapping("/users/{id}/roles")
    public Result<Void> updateUserRoles(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Parameter(description = "角色列表") @RequestBody @Valid Set<String> roles) {
        userService.updateUserRoles(id, roles);
        return Result.success();
    }

    @Operation(summary = "获取统计信息", description = "获取系统统计信息")
    @GetMapping("/statistics")
    public Result<StatisticsDTO> getStatistics() {
        return Result.success(statisticsService.getOverviewStatistics());
    }

    @Operation(summary = "获取系统设置", description = "获取所有系统设置")
    @GetMapping("/settings")
    public Result<Map<String, Object>> getSettings() {
        return Result.success(settingsService.getSystemSettings());
    }

    @Operation(summary = "更新系统设置", description = "更新系统设置")
    @PutMapping("/settings")
    public Result<Void> updateSettings(
            @Parameter(description = "设置信息") @RequestBody @Valid Map<String, Object> settings) {
        settingsService.updateSystemSettings(settings);
        return Result.success();
    }

    @Operation(summary = "获取管理面板统计", description = "获取管理面板的详细统计信息")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        return Result.success(statisticsService.getAdminDashboardStatistics());
    }
}