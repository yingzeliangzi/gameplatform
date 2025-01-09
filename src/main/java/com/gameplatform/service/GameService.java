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
    // 基本CRUD操作
    GameDTO getGameById(Long id);
    GameDTO createGame(GameDTO gameDTO);
    GameDTO updateGame(Long gameId, GameDTO gameDTO);
    void deleteGame(Long gameId);

    // 查询和搜索
    Page<GameDTO> searchGames(GameSearchDTO searchDTO, Pageable pageable);
    List<String> getAllCategories();
    Page<GameDTO> getGamesByCategory(String category, Pageable pageable);
    Page<GameDTO> getUserGames(Long userId, Pageable pageable);
    List<GameDTO> getHotGames();
    List<GameDTO> getRecommendedGames(Long userId);
    List<GameDTO> getNewReleases(int limit);

    // 评分和反馈
    void rateGame(Long gameId, Long userId, Double rating);
    void addGameReview(Long gameId, Long userId, String review);
    void updateGameReview(Long gameId, Long userId, String review);
    void deleteGameReview(Long gameId, Long userId);
    Double getAverageRating(Long gameId);

    // 用户游戏管理
    void addGameToUser(Long gameId, Long userId);
    void removeGameFromUser(Long gameId, Long userId);
    void updateGameProgress(Long gameId, Long userId, Integer progress);
    void updatePlayTime(Long gameId, Long userId, Integer minutes);

    // 统计相关
    void updatePopularGames();
    long getGamePlayerCount(Long gameId);
    Map<String, Object> getGameStatistics(Long gameId);
    Map<String, Long> getPlayTimeDistribution(Long gameId);
    Map<Integer, Long> getRatingDistribution(Long gameId);

    // 数据导出
    byte[] exportUserGames(Long userId);
    byte[] exportGameStatistics(Long gameId);

    // 游戏收藏
    void collectGame(Long gameId, Long userId);
    void uncollectGame(Long gameId, Long userId);
    boolean isGameCollected(Long gameId, Long userId);
    Page<GameDTO> getUserCollections(Long userId, Pageable pageable);

    // 推荐系统
    List<GameDTO> getPersonalizedRecommendations(Long userId, int limit);
    List<GameDTO> getSimilarGames(Long gameId, int limit);
    List<GameDTO> getTopRatedGames(int limit);
    List<GameDTO> getMostPlayedGames(int limit);

    // 批量操作
    void batchUpdateGames(List<GameDTO> games);
    void batchDeleteGames(List<Long> gameIds);
    void batchAddGamesToUser(List<Long> gameIds, Long userId);

    // 分析和报告
    Map<String, Object> generateGameReport(Long gameId);
    Map<String, Object> generateUserGamingReport(Long userId);
    List<Map<String, Object>> getGameTrends(Long gameId, String timeRange);
}