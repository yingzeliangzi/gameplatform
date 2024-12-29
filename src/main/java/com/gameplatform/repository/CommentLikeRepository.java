package com.gameplatform.repository;

import com.gameplatform.model.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:18
 * @description TODO
 */
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByCommentIdAndUserId(Long commentId, Long userId);

    boolean existsByCommentIdAndUserId(Long commentId, Long userId);

    @Query("SELECT COUNT(cl) FROM CommentLike cl WHERE cl.comment.id = :commentId")
    long countByCommentId(@Param("commentId") Long commentId);

    void deleteByCommentIdAndUserId(Long commentId, Long userId);
}