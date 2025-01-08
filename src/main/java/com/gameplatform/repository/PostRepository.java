package com.gameplatform.repository;
import com.gameplatform.model.entity.Post;
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
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT COUNT(p) FROM Post p WHERE p.author.id = :userId")
    long countByAuthorId(@Param("userId") Long userId);

    @Query("SELECT COUNT(pl) FROM PostLike pl WHERE pl.post.author.id = :authorId")
    long countLikesByAuthorId(@Param("authorId") Long authorId);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.game.id = :gameId")
    long countByGameId(@Param("gameId") Long gameId);

    List<Post> findTop10ByOrderByViewCountDesc();

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :start AND :end")
    long countByCreatedAtBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    long countTotalComments();

    Page<Post> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);
}