package com.gameplatform.repository;

import com.gameplatform.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    // 基础查询方法
    Page<Post> findByGameId(Long gameId, Pageable pageable);
    Page<Post> findByAuthorId(Long authorId, Pageable pageable);
    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    // 计数统计
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId")
    long countByAuthorId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author.id = :authorId")
    long countLikesByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.game.id = :gameId")
    long countByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    long countCommentsByPostId(@Param("postId") Long postId);

    @Query("SELECT COUNT(c) FROM Comment c")
    long countTotalComments();

    // 热门和推荐查询
    List<Post> findTop10ByOrderByViewCountDesc();

    @Query("SELECT p FROM Post p ORDER BY p.likeCount DESC, p.viewCount DESC, p.createdAt DESC")
    Page<Post> findHotPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.game.id = :gameId ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsByGameId(@Param("gameId") Long gameId, Pageable pageable);

    // 状态更新
    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.id = :postId")
    void incrementViewCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.status = :status WHERE p.id = :postId")
    void updateStatus(@Param("postId") Long postId, @Param("status") Post.PostStatus status);

    // 综合查询
    @Query("SELECT p FROM Post p WHERE " +
            "(:gameId IS NULL OR p.game.id = :gameId) AND " +
            "(:authorId IS NULL OR p.author.id = :authorId) AND " +
            "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "p.status = com.gameplatform.model.entity.Post.PostStatus.NORMAL " +
            "ORDER BY p.createdAt DESC")
    Page<Post> searchPosts(
            @Param("gameId") Long gameId,
            @Param("authorId") Long authorId,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 用户相关查询
    @Query("SELECT p FROM Post p WHERE p.author.id IN " +
            "(SELECT f.id FROM User u JOIN u.following f WHERE u.id = :userId)")
    Page<Post> findPostsByFollowedUsers(@Param("userId") Long userId, Pageable pageable);

    // 批量操作
    @Modifying
    @Query("UPDATE Post p SET p.status = :status WHERE p.id IN :postIds")
    void batchUpdateStatus(@Param("postIds") List<Long> postIds, @Param("status") Post.PostStatus status);

    // 获取用户的收藏帖子
    @Query("SELECT p FROM Post p JOIN PostLike pl ON p.id = pl.post.id WHERE pl.user.id = :userId")
    Page<Post> findUserLikedPosts(@Param("userId") Long userId, Pageable pageable);

    // 特定时间范围的帖子统计
    @Query("SELECT COUNT(p) FROM Post p WHERE " +
            "p.createdAt >= :startTime AND p.createdAt < :endTime AND " +
            "(:gameId IS NULL OR p.game.id = :gameId)")
    long countPostsInTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("gameId") Long gameId
    );
}