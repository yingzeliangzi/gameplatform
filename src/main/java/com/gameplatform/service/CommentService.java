package com.gameplatform.service;

import com.gameplatform.model.dto.CommentDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:17
 * @description TODO
 */
public interface CommentService {
    CommentDTO createComment(Long userId, CommentDTO commentDTO);
    CommentDTO updateComment(Long commentId, Long userId, String content);
    void deleteComment(Long commentId, Long userId);
    CommentDTO getCommentById(Long commentId);
    Page<CommentDTO> getPostComments(Long postId, Pageable pageable);
    Page<CommentDTO> getUserComments(Long userId, Pageable pageable);
    List<CommentDTO> getCommentReplies(Long commentId);
    void likeComment(Long commentId, Long userId);
    void unlikeComment(Long commentId, Long userId);
    void reportComment(Long commentId, Long userId, String reason);
}
