package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.EventDTO;
import com.gameplatform.model.dto.EventListItemDTO;
import com.gameplatform.model.dto.EventRegistrationDTO;
import com.gameplatform.model.entity.Event;
import com.gameplatform.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:09
 * @description TODO
 */
@Tag(name = "活动接口", description = "活动相关操作接口")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class EventController {
    private final EventService eventService;

    @Operation(summary = "创建活动", description = "创建新的活动")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission("event:create")
    public Result<EventDTO> createEvent(
            @Parameter(description = "活动信息", required = true)
            @Valid @RequestBody EventDTO eventDTO) {
        return Result.success(eventService.createEvent(eventDTO));
    }

    @Operation(summary = "更新活动", description = "更新已有活动信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission("event:update")
    public Result<EventDTO> updateEvent(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id,
            @Parameter(description = "活动信息", required = true)
            @Valid @RequestBody EventDTO eventDTO) {
        return Result.success(eventService.updateEvent(id, eventDTO));
    }

    @Operation(summary = "取消活动", description = "取消指定活动")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @RequirePermission("event:cancel")
    public Result<Void> cancelEvent(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id) {
        eventService.cancelEvent(id);
        return Result.success();
    }

    @Operation(summary = "获取活动列表", description = "分页获取活动列表，支持关键字和类型筛选")
    @GetMapping
    public Result<Page<EventListItemDTO>> getEvents(
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "活动类型") @RequestParam(required = false) Event.EventType type,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(eventService.searchEvents(keyword, type, userId, pageable));
    }

    @Operation(summary = "获取即将开始的活动", description = "分页获取即将开始的活动列表")
    @GetMapping("/upcoming")
    public Result<Page<EventListItemDTO>> getUpcomingEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(eventService.getUpcomingEvents(userId, pageable));
    }

    @Operation(summary = "获取进行中的活动", description = "分页获取正在进行的活动列表")
    @GetMapping("/ongoing")
    public Result<Page<EventListItemDTO>> getOngoingEvents(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(eventService.getOngoingEvents(userId, pageable));
    }

    @Operation(summary = "获取活动详情", description = "获取指定活动的详细信息")
    @GetMapping("/{id}")
    public Result<EventDTO> getEvent(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(eventService.getEventById(id, userId));
    }

    @Operation(summary = "活动报名", description = "报名参加指定活动")
    @PostMapping("/{id}/register")
    @RequirePermission("event:register")
    public Result<EventRegistrationDTO> registerForEvent(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id,
            @Parameter(description = "报名信息", required = true)
            @Valid @RequestBody EventRegistrationDTO registrationDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(eventService.registerForEvent(id, userId, registrationDTO));
    }

    @Operation(summary = "取消报名", description = "取消已报名的活动")
    @DeleteMapping("/{id}/register")
    @RequirePermission("event:register")
    public Result<Void> cancelRegistration(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        eventService.cancelRegistration(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取我的报名记录", description = "分页获取当前用户的活动报名记录")
    @GetMapping("/registrations/me")
    public Result<Page<EventRegistrationDTO>> getMyRegistrations(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(eventService.getUserRegistrations(userId, pageable));
    }

    @Operation(summary = "获取活动报名列表", description = "管理员获取活动的报名记录")
    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Page<EventRegistrationDTO>> getEventRegistrations(
            @Parameter(description = "活动ID", required = true) @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(eventService.getEventRegistrations(id, pageable));
    }
}