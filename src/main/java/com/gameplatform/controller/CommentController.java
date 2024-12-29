package com.gameplatform.controller;

import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:17
 * @description TODO
 */
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDTO> createComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CommentDTO commentDTO) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(commentService.createComment(userId, commentDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> updateComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody String content) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(commentService.updateComment(id, userId, content));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.deleteComment(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentDTO> getComment(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentById(id));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentDTO>> getPostComments(
            @PathVariable Long postId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getPostComments(postId, pageable));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<CommentDTO>> getUserComments(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(commentService.getUserComments(userId, pageable));
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDTO>> getCommentReplies(@PathVariable Long id) {
        return ResponseEntity.ok(commentService.getCommentReplies(id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likeComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.likeComment(id, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<?> unlikeComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.unlikeComment(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/report")
    public ResponseEntity<?> reportComment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestBody String reason) {
        Long userId = Long.parseLong(userDetails.getUsername());
        commentService.reportComment(id, userId, reason);
        return ResponseEntity.ok().build();
    }
}
