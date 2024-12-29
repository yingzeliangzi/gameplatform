package com.gameplatform.service;
import com.gameplatform.model.dto.CommentDTO;
import com.gameplatform.model.dto.PostDTO;
import com.gameplatform.model.dto.ReportDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */

public interface PostService {
    PostDTO createPost(PostDTO postDTO, Long userId);

    PostDTO updatePost(Long postId, PostDTO postDTO, Long userId);

    void deletePost(Long postId, Long userId);

    PostDTO getPostById(Long postId);

    Page<PostDTO> getPostsByGame(Long gameId, Pageable pageable);

    Page<PostDTO> searchPosts(String keyword, Pageable pageable);

    CommentDTO addComment(Long postId, CommentDTO commentDTO, Long userId);

    CommentDTO replyToComment(Long commentId, CommentDTO replyDTO, Long userId);

    void deleteComment(Long commentId, Long userId);

    void reportContent(ReportDTO reportDTO, Long userId);

    void likePost(Long postId, Long userId);

    void unlikePost(Long postId, Long userId);
}
