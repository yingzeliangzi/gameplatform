package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.dto.ReportDTO;
import com.gameplatform.service.PostService;
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

import javax.validation.Valid;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:09
 * @description TODO
 */
@Tag(name = "帖子接口", description = "帖子相关操作接口")
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class PostController {

    private final PostService postService;

    @Operation(summary = "创建帖子", description = "创建新帖子")
    @PostMapping
    @RequirePermission("post:create")
    public Result<PostDTO> createPost(
            @Parameter(description = "帖子信息", required = true)
            @Valid @RequestBody PostDTO postDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(postService.createPost(postDTO, userId));
    }

    @Operation(summary = "搜索帖子", description = "搜索帖子列表，支持按关键词和游戏ID搜索")
    @GetMapping
    public Result<Page<PostDTO>> getPosts(
            @Parameter(description = "游戏ID")
            @RequestParam(required = false) Long gameId,
            @Parameter(description = "搜索关键词")
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable) {
        if (gameId != null) {
            return Result.success(postService.getPostsByGame(gameId, pageable));
        } else {
            return Result.success(postService.searchPosts(keyword, pageable));
        }
    }

    @Operation(summary = "获取帖子详情", description = "获取指定帖子的详细信息")
    @GetMapping("/{id}")
    public Result<PostDTO> getPost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id) {
        return Result.success(postService.getPostById(id));
    }

    @Operation(summary = "添加评论", description = "对帖子添加评论")
    @PostMapping("/{id}/comments")
    @RequirePermission("post:comment")
    public Result<CommentDTO> addComment(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @Parameter(description = "评论信息", required = true)
            @Valid @RequestBody CommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(postService.addComment(id, commentDTO, userId));
    }

    @Operation(summary = "回复评论", description = "回复帖子中的评论")
    @PostMapping("/comments/{commentId}/replies")
    @RequirePermission("post:comment")
    public Result<CommentDTO> replyToComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @Parameter(description = "回复信息", required = true)
            @Valid @RequestBody CommentDTO replyDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(postService.replyToComment(commentId, replyDTO, userId));
    }

    @Operation(summary = "删除评论", description = "删除帖子中的评论")
    @DeleteMapping("/comments/{commentId}")
    @RequirePermission("post:comment:delete")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID", required = true)
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.deleteComment(commentId, userId);
        return Result.success();
    }

    @Operation(summary = "举报内容", description = "举报违规帖子或评论")
    @PostMapping("/report")
    @RequirePermission("post:report")
    public Result<Void> reportContent(
            @Parameter(description = "举报信息", required = true)
            @Valid @RequestBody ReportDTO reportDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.reportContent(reportDTO, userId);
        return Result.success();
    }

    @Operation(summary = "点赞帖子", description = "对帖子进行点赞")
    @PostMapping("/{id}/like")
    @RequirePermission("post:like")
    public Result<Void> likePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.likePost(id, userId);
        return Result.success();
    }

    @Operation(summary = "取消点赞", description = "取消对帖子的点赞")
    @DeleteMapping("/{id}/like")
    @RequirePermission("post:like")
    public Result<Void> unlikePost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.unlikePost(id, userId);
        return Result.success();
    }

    @Operation(summary = "收藏帖子", description = "收藏指定帖子")
    @PostMapping("/{id}/collect")
    @RequirePermission("post:collect")
    public Result<Void> collectPost(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.collectPost(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取热门帖子", description = "获取热门帖子列表")
    @GetMapping("/hot")
    public Result<Page<PostDTO>> getHotPosts(
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(postService.getHotPosts(pageable));
    }
}