package com.gameplatform.repository;

import com.gameplatform.model.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:23
 * @description TODO
 */
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByPostId(Long postId, Pageable pageable);

    Page<Comment> findByAuthorId(Long authorId, Pageable pageable);

    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);

    List<Comment> findByParentCommentId(Long parentId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.author.id = :userId")
    long countByAuthorId(@Param("userId") Long userId);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Comment> findByPostIdOrderByCreatedAtDesc(Long postId);

    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL " +
            "ORDER BY c.likeCount DESC, c.createdAt DESC")
    Page<Comment> findTopLevelCommentsByPostId(
            @Param("postId") Long postId,
            Pageable pageable
    );
}