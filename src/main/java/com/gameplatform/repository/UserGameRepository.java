package com.gameplatform.repository;

import com.gameplatform.model.entity.UserGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 2:00
 * @description TODO
 */
public interface UserGameRepository extends JpaRepository<UserGame, Long> {
    Optional<UserGame> findByGameIdAndUserId(Long gameId, Long userId);

    List<UserGame> findByGameId(Long gameId);

    List<UserGame> findByUserId(Long userId);

    Page<UserGame> findByUserId(Long userId, Pageable pageable);

    Page<UserGame> findByUserIdOrderByLastPlayedAtDesc(Long userId, Pageable pageable);

    boolean existsByGameIdAndUserId(Long gameId, Long userId);

    @Query("SELECT COUNT(ug) FROM UserGame ug WHERE ug.game.id = :gameId")
    long countByGameId(@Param("gameId") Long gameId);

    @Query("SELECT COUNT(ug) FROM UserGame ug WHERE ug.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(ug.playTime) FROM UserGame ug WHERE ug.user.id = :userId")
    Long sumPlayTimeByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(ug.playTime) FROM UserGame ug WHERE ug.game.id = :gameId")
    Double avgPlayTimeByGameId(@Param("gameId") Long gameId);

    @Query("SELECT ug FROM UserGame ug WHERE ug.user.id = :userId " +
            "ORDER BY ug.playTime DESC")
    Page<UserGame> findMostPlayedGames(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT ug FROM UserGame ug WHERE ug.user.id = :userId " +
            "AND ug.userRating IS NOT NULL " +
            "ORDER BY ug.userRating DESC")
    Page<UserGame> findHighestRatedGames(@Param("userId") Long userId, Pageable pageable);

    Long sumPlayTimeByAll();
    Long sumPlayTimeInTimeRange(LocalDateTime start, LocalDateTime end);
    Map<String, Long> getPlayTimeDistribution(Long gameId);
    Map<Integer, Long> getRatingDistribution(Long gameId);
}