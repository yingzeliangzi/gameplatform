package com.gameplatform.service;

import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:11
 * @description TODO
 */
public interface GameService {
    GameDTO getGameById(Long id);
    GameDTO createGame(GameDTO gameDTO);
    GameDTO updateGame(Long gameId, GameDTO gameDTO);
    void deleteGame(Long gameId);
    Page<GameDTO> searchGames(GameSearchDTO searchDTO, Pageable pageable);
    List<String> getAllCategories();
    Page<GameDTO> getGamesByCategory(String category, Pageable pageable);
    Page<GameDTO> getUserGames(Long userId, Pageable pageable);
    List<GameDTO> getHotGames();
    List<GameDTO> getRecommendedGames(Long userId);
    List<GameDTO> getNewReleases(int limit);
    void rateGame(Long gameId, Long userId, Double rating);
    void addGameReview(Long gameId, Long userId, String review);
    void updateGameReview(Long gameId, Long userId, String review);
    void deleteGameReview(Long gameId, Long userId);
    Double getAverageRating(Long gameId);
    void addGameToUser(Long gameId, Long userId);
    void removeGameFromUser(Long gameId, Long userId);
    void updateGameProgress(Long gameId, Long userId, Integer progress);
    void updatePlayTime(Long gameId, Long userId, Integer minutes);
    void updatePopularGames();
    long getGamePlayerCount(Long gameId);
    Map<String, Object> getGameStatistics(Long gameId);
    Map<String, Long> getPlayTimeDistribution(Long gameId);
    Map<Integer, Long> getRatingDistribution(Long gameId);
    byte[] exportUserGames(Long userId);
    Map<String, Object> generateGameReport(Long gameId);
    Map<String, Object> generateUserGamingReport(Long userId);
    List<Map<String, Object>> getGameTrends(Long gameId, String timeRange);
}