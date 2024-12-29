package com.gameplatform.repository;import com.gameplatform.model.entity.Game;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
    @Query("SELECT DISTINCT g FROM Game g WHERE g.rating >= :minRating")
    Page<Game> findByMinimumRating(@Param("minRating") Double minRating, Pageable pageable);
    @Query("SELECT DISTINCT g.categories FROM Game g")
    List<String> findAllCategories();
}
