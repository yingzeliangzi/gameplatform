package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import com.gameplatform.service.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:08
 * @description TODO
 */
@Tag(name = "游戏接口", description = "游戏相关操作接口")
@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class GameController {
    private final GameService gameService;

    @Operation(summary = "搜索游戏", description = "根据条件搜索游戏列表")
    @GetMapping
    public Result<Page<GameDTO>> searchGames(
            @Parameter(description = "搜索条件")
            @Valid GameSearchDTO searchDTO,
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(gameService.searchGames(searchDTO, pageable));
    }

    @Operation(summary = "获取游戏详情", description = "获取指定游戏的详细信息")
    @GetMapping("/{id}")
    public Result<GameDTO> getGame(
            @Parameter(description = "游戏ID", required = true)
            @PathVariable Long id) {
        return Result.success(gameService.getGameById(id));
    }

    @Operation(summary = "获取游戏分类", description = "获取所有游戏分类")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(gameService.getAllCategories());
    }

    @Operation(summary = "评价游戏", description = "为游戏评分")
    @PostMapping("/{id}/rate")
    @RequirePermission("game:rate")
    public Result<Void> rateGame(
            @Parameter(description = "游戏ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "评分", required = true)
            @RequestParam @Min(1) @Max(5) Double rating,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        gameService.rateGame(id, userId, rating);
        return Result.success();
    }

    @Operation(summary = "导出游戏列表", description = "导出用户的游戏列表")
    @GetMapping("/export")
    @RequirePermission("game:export")
    public ResponseEntity<byte[]> exportGames(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        byte[] content = gameService.exportUserGames(userId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"games.csv\"")
                .contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .body(content);
    }

    @Operation(summary = "获取用户游戏列表", description = "获取用户拥有的游戏列表")
    @GetMapping("/my")
    public Result<Page<GameDTO>> getUserGames(
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(gameService.getUserGames(userId, pageable));
    }

    @Operation(summary = "收藏游戏", description = "将游戏添加到收藏")
    @PostMapping("/{id}/collect")
    @RequirePermission("game:collect")
    public Result<Void> addGameToCollection(
            @Parameter(description = "游戏ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        gameService.addGameToUser(id, userId);
        return Result.success();
    }

    @Operation(summary = "取消收藏", description = "将游戏从收藏中移除")
    @DeleteMapping("/{id}/collect")
    @RequirePermission("game:collect")
    public Result<Void> removeGameFromCollection(
            @Parameter(description = "游戏ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        gameService.removeGameFromUser(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取热门游戏", description = "获取热门游戏列表")
    @GetMapping("/hot")
    public Result<List<GameDTO>> getHotGames() {
        return Result.success(gameService.getHotGames());
    }

    @Operation(summary = "获取推荐游戏", description = "获取个性化推荐的游戏列表")
    @GetMapping("/recommended")
    public Result<List<GameDTO>> getRecommendedGames(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = userDetails != null ? Long.parseLong(userDetails.getUsername()) : null;
        return Result.success(gameService.getRecommendedGames(userId));
    }
}