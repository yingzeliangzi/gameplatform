package com.gameplatform.repository;

import com.gameplatform.model.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsByTitle(String title);

    Page<Game> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Game> findByCategoriesIn(Set<String> categories, Pageable pageable);

    @Query("SELECT g FROM Game g WHERE " +
            "(:title IS NULL OR LOWER(g.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:categories IS NULL OR g.categories IN :categories) AND " +
            "(:minRating IS NULL OR g.rating >= :minRating)")
    Page<Game> findBySearchCriteria(
            @Param("title") String title,
            @Param("categories") Set<String> categories,
            @Param("minRating") Double minRating,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM Game g JOIN g.categories c")
    List<String> findAllCategories();

    List<Game> findTop10ByOrderByPopularityDesc();

    @Query("SELECT COUNT(g) FROM Game g WHERE g.rating >= :rating")
    long countByRatingGreaterThanEqual(@Param("rating") Double rating);

    Page<Game> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime date, Pageable pageable);

    @Query("SELECT DISTINCT g FROM Game g " +
            "WHERE g.id NOT IN :excludeIds " +
            "AND EXISTS (SELECT 1 FROM g.categories c WHERE c IN :categories) " +
            "ORDER BY g.rating DESC, g.popularity DESC")
    List<Game> findRecommendedGames(
            @Param("excludeIds") Set<Long> excludeIds,
            @Param("categories") Set<String> categories,
            Pageable pageable
    );

    @Query("SELECT g FROM Game g " +
            "ORDER BY g.popularity DESC, g.rating DESC, g.ratingCount DESC")
    List<Game> findHotGames(Pageable pageable);

    @Query("SELECT DISTINCT g FROM Game g JOIN g.categories c WHERE c = :category")
    Page<Game> findByCategory(@Param("category") String category, Pageable pageable);
}