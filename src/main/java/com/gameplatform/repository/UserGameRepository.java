package com.gameplatform.repository;

import com.gameplatform.model.entity.UserGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
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
    boolean existsByGameIdAndUserId(Long gameId, Long userId);
    @Query("SELECT COUNT(ug) FROM UserGame ug WHERE ug.game.id = :gameId")
    long countByGameId(@Param("gameId") Long gameId);
    long countByUserId(@Param("userId") Long userId);

    @Query("SELECT SUM(ug.playTime) FROM UserGame ug WHERE ug.user.id = :userId")
    Long sumPlayTimeByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(ug.playTime) FROM UserGame ug WHERE ug.game.id = :gameId")
    Double avgPlayTimeByGameId(@Param("gameId") Long gameId);



}
