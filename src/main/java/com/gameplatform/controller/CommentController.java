package com.gameplatform.controller;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.common.Result;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.service.CommentService;
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
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:17
 * @description TODO
 */
@Tag(name = "评论接口", description = "评论相关操作接口")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Validated
@SecurityRequirement(name = "Bearer Authentication")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "创建评论", description = "创建新的评论")
    @PostMapping
    @RequirePermission("comment:create")
    public Result<CommentDTO> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论信息", required = true)
            @Valid @RequestBody CommentDTO commentDTO) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(commentService.createComment(userId, commentDTO));
    }

    @Operation(summary = "更新评论", description = "更新已有评论内容")
    @PutMapping("/{id}")
    @RequirePermission("comment:update")
    public Result<CommentDTO> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id,
            @Parameter(description = "评论内容", required = true) @RequestBody String content) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return Result.success(commentService.updateComment(id, userId, content));
    }

    @Operation(summary = "删除评论", description = "删除指定评论")
    @DeleteMapping("/{id}")
    @RequirePermission("comment:delete")
    public Result<Void> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.deleteComment(id, userId);
        return Result.success();
    }

    @Operation(summary = "获取评论详情", description = "获取指定评论的详细信息")
    @GetMapping("/{id}")
    public Result<CommentDTO> getComment(
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        return Result.success(commentService.getCommentById(id));
    }

    @Operation(summary = "获取帖子评论列表", description = "分页获取指定帖子的评论列表")
    @GetMapping("/post/{postId}")
    public Result<Page<CommentDTO>> getPostComments(
            @Parameter(description = "帖子ID", required = true) @PathVariable Long postId,
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(commentService.getPostComments(postId, pageable));
    }

    @Operation(summary = "获取用户评论列表", description = "分页获取指定用户的评论列表")
    @GetMapping("/user/{userId}")
    public Result<Page<CommentDTO>> getUserComments(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @PageableDefault(size = 10) Pageable pageable) {
        return Result.success(commentService.getUserComments(userId, pageable));
    }

    @Operation(summary = "获取评论回复列表", description = "获取指定评论的回复列表")
    @GetMapping("/{id}/replies")
    public Result<List<CommentDTO>> getCommentReplies(
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        return Result.success(commentService.getCommentReplies(id));
    }

    @Operation(summary = "点赞评论", description = "对指定评论进行点赞")
    @PostMapping("/{id}/like")
    @RequirePermission("comment:like")
    public Result<Void> likeComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.likeComment(id, userId);
        return Result.success();
    }

    @Operation(summary = "取消点赞", description = "取消对指定评论的点赞")
    @DeleteMapping("/{id}/like")
    @RequirePermission("comment:like")
    public Result<Void> unlikeComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.unlikeComment(id, userId);
        return Result.success();
    }

    @Operation(summary = "举报评论", description = "举报违规评论")
    @PostMapping("/{id}/report")
    @RequirePermission("comment:report")
    public Result<Void> reportComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "评论ID", required = true) @PathVariable Long id,
            @Parameter(description = "举报原因", required = true) @RequestBody String reason) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.reportComment(id, userId, reason);
        return Result.success();
    }
}