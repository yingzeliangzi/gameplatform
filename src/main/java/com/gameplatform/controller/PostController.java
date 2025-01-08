package com.gameplatform.controller;import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.dto.ReportDTO;
import com.gameplatform.model.entity.Report;
import com.gameplatform.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:09
 * @description TODO
 */
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @Valid @RequestBody PostDTO postDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(postService.createPost(postDTO, userId));
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getPosts(
            @RequestParam(required = false) Long gameId,
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        if (gameId != null) {
            return ResponseEntity.ok(postService.getPostsByGame(gameId, pageable));
        } else if (keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(postService.searchPosts(keyword, pageable));
        }
        return ResponseEntity.ok(postService.searchPosts("", pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentDTO commentDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(postService.addComment(id, commentDTO, userId));
    }

    @PostMapping("/comments/{commentId}/replies")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDTO replyDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ResponseEntity.ok(postService.replyToComment(commentId, replyDTO, userId));
    }

    @PostMapping("/report")
    public ResponseEntity<?> reportContent(@Valid @RequestBody ReportDTO reportDTO,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        Report report = new Report();
        report.setType(Report.ReportType.valueOf(reportDTO.getType()));
        report.setTargetId(reportDTO.getTargetId());
        report.setReason(reportDTO.getReason());
        report.setDescription(reportDTO.getDescription());
        report.setStatus(Report.ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());

        reportRepository.save(report);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        postService.likePost(id, userId);
        return ResponseEntity.ok().build();
    }
}
