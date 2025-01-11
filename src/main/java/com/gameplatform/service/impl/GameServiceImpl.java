package com.gameplatform.service.impl;

import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.GameDTO;
import com.gameplatform.model.dto.GameSearchDTO;
import com.gameplatform.model.entity.Game;
import com.gameplatform.model.entity.UserGame;
import com.gameplatform.model.entity.User;
import com.gameplatform.repository.GameRepository;
import com.gameplatform.repository.PostRepository;
import com.gameplatform.repository.UserGameRepository;
import com.gameplatform.repository.UserRepository;
import com.gameplatform.service.GameService;
import com.gameplatform.service.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:13
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final UserGameRepository userGameRepository;
    private final CacheService cacheService;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public GameDTO createGame(GameDTO gameDTO) {
        // 检查重复
        if (gameRepository.existsByTitle(gameDTO.getTitle())) {
            throw new BusinessException("游戏名称已存在");
        }

        Game game = new Game();
        game.setTitle(gameDTO.getTitle());
        game.setDescription(gameDTO.getDescription());
        game.setPrice(gameDTO.getPrice());
        game.setCoverImage(gameDTO.getCoverImage());
        game.setCategories(gameDTO.getCategories());
        game.setScreenshots(gameDTO.getScreenshots());
        game.setRating(0.0);
        game.setRatingCount(0);
        game.setPopularity(0);
        game.setCreatedAt(LocalDateTime.now());

        Game savedGame = gameRepository.save(game);
        return convertToDTO(savedGame);
    }

    @Override
    @Transactional
    public GameDTO updateGame(Long gameId, GameDTO gameDTO) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        // 更新基本信息
        if (gameDTO.getTitle() != null) {
            game.setTitle(gameDTO.getTitle());
        }
        if (gameDTO.getDescription() != null) {
            game.setDescription(gameDTO.getDescription());
        }
        if (gameDTO.getPrice() != null) {
            game.setPrice(gameDTO.getPrice());
        }
        if (gameDTO.getCoverImage() != null) {
            game.setCoverImage(gameDTO.getCoverImage());
        }
        if (gameDTO.getCategories() != null) {
            game.setCategories(gameDTO.getCategories());
        }
        if (gameDTO.getScreenshots() != null) {
            game.setScreenshots(gameDTO.getScreenshots());
        }

        Game updatedGame = gameRepository.save(game);

        // 清除缓存
        cacheService.evictCache("game:" + gameId);

        return convertToDTO(updatedGame);
    }

    @Override
    @Transactional
    public void deleteGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        // 检查是否有关联数据
        if (!userGameRepository.findByGameId(gameId).isEmpty()) {
            throw new BusinessException("该游戏已有用户购买，无法删除");
        }

        // 删除相关的评论和评分
        userGameRepository.deleteByGameId(gameId);

        // 删除游戏
        gameRepository.delete(game);

        // 清除缓存
        cacheService.evictCache("game:" + gameId);
        cacheService.evictCache("game:list");
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDTO> getGamesByCategory(String category, Pageable pageable) {
        // 获取指定分类的游戏列表
        Page<Game> games = gameRepository.findByCategory(category, pageable);

        return games.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameDTO> getHotGames() {
        // 获取热门游戏，基于评分、玩家数量和最近活跃度
        List<Game> hotGames = gameRepository.findHotGames(PageRequest.of(0, 10));

        return hotGames.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameDTO> getRecommendedGames(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取用户已有的游戏
        Set<Long> userGameIds = user.getGames().stream()
                .map(ug -> ug.getGame().getId())
                .collect(Collectors.toSet());

        // 获取用户游戏的类别
        Set<String> userCategories = user.getGames().stream()
                .flatMap(ug -> ug.getGame().getCategories().stream())
                .collect(Collectors.toSet());

        // 根据用户喜好获取推荐游戏
        List<Game> recommendedGames = gameRepository.findRecommendedGames(
                userGameIds,
                userCategories,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "rating"))
        );

        return recommendedGames.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GameDTO> getNewReleases(int limit) {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);

        // 获取最近添加的游戏
        List<Game> newGames = gameRepository.findByCreatedAtAfterOrderByCreatedAtDesc(
                oneMonthAgo,
                PageRequest.of(0, limit)
        ).getContent();

        return newGames.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void addGameReview(Long gameId, Long userId, String review) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未拥有该游戏"));

        // 检查是否已经有评价
        if (userGame.getUserReview() != null) {
            throw new BusinessException("已经评价过该游戏，请使用更新功能");
        }

        // 添加评价
        userGame.setUserReview(review);

        // 如果还没有评分，设置一个默认评分
        if (userGame.getUserRating() == null) {
            userGame.setUserRating(5.0); // 默认5分
        }

        userGameRepository.save(userGame);

        // 更新游戏总评分
        updateGameRating(gameId);

        // 清除缓存
        cacheService.evictCache("game:review:" + gameId + ":" + userId);
    }

    @Override
    @Transactional
    public void updateGameReview(Long gameId, Long userId, String review) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未找到游戏评价"));

        // 更新评价内容
        userGame.setUserReview(review);
        userGameRepository.save(userGame);

        // 清除缓存
        cacheService.evictCache("game:review:" + gameId + ":" + userId);
    }

    @Override
    @Transactional
    public void deleteGameReview(Long gameId, Long userId) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未找到游戏评价"));

        // 清除评分和评价
        userGame.setUserRating(null);
        userGame.setUserReview(null);
        userGameRepository.save(userGame);

        // 更新游戏总评分
        updateGameRating(gameId);

        // 清除缓存
        cacheService.evictCache("game:rating:" + gameId);
    }

    private void updateGameRating(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        // 计算新的平均评分
        Double avgRating = getAverageRating(gameId);
        game.setRating(avgRating);

        // 更新评分数量
        long ratingCount = userGameRepository.findByGameId(gameId).stream()
                .filter(ug -> ug.getUserRating() != null)
                .count();
        game.setRatingCount((int) ratingCount);

        gameRepository.save(game);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getAverageRating(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        List<UserGame> userGames = userGameRepository.findByGameId(gameId);

        return userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .average()
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void updateGameProgress(Long gameId, Long userId, Integer progress) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未拥有该游戏"));

        userGame.setPlayTime(progress);
        userGame.setLastPlayedAt(LocalDateTime.now());
        userGameRepository.save(userGame);
    }

    @Override
    @Transactional(readOnly = true)
    public long getGamePlayerCount(Long gameId) {
        return userGameRepository.countByGameId(gameId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGameStatistics(Long gameId) {
        Map<String, Object> statistics = new HashMap<>();

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        // 游戏基本信息
        statistics.put("rating", game.getRating());
        statistics.put("ratingCount", game.getRatingCount());

        // 玩家统计
        long playerCount = userGameRepository.countByGameId(gameId);
        statistics.put("playerCount", playerCount);

        // 游戏时长分布
        statistics.put("playTimeDistribution", getPlayTimeDistribution(gameId));

        // 评分分布
        statistics.put("ratingDistribution", getRatingDistribution(gameId));

        return statistics;
    }

    @Override
    public Map<Integer, Long> getRatingDistribution(Long gameId) {
        List<UserGame> userGames = userGameRepository.findByGameId(gameId);
        return userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .collect(Collectors.groupingBy(
                        ug -> ug.getUserRating().intValue(),
                        Collectors.counting()
                ));
    }

    public Map<String, Long> getPlayTimeDistribution(Long gameId) {
        List<UserGame> userGames = userGameRepository.findByGameId(gameId);
        Map<String, Long> distribution = new HashMap<>();

        distribution.put("0-1h", userGames.stream()
                .filter(ug -> ug.getPlayTime() <= 60).count());
        distribution.put("1-5h", userGames.stream()
                .filter(ug -> ug.getPlayTime() > 60 && ug.getPlayTime() <= 300).count());
        distribution.put("5-10h", userGames.stream()
                .filter(ug -> ug.getPlayTime() > 300 && ug.getPlayTime() <= 600).count());
        distribution.put(">10h", userGames.stream()
                .filter(ug -> ug.getPlayTime() > 600).count());

        return distribution;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateUserGamingReport(Long userId) {
        Map<String, Object> report = new HashMap<>();
        List<UserGame> userGames = userGameRepository.findByUserId(userId);

        // 基础统计
        report.put("totalGames", userGames.size());
        report.put("totalPlayTime", userGames.stream()
                .mapToInt(UserGame::getPlayTime)
                .sum());

        // 游戏评分统计
        DoubleSummaryStatistics ratingStats = userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .summaryStatistics();
        report.put("averageRating", ratingStats.getAverage());
        report.put("totalRated", ratingStats.getCount());

        // 最常玩的游戏
        List<Map<String, Object>> mostPlayed = userGames.stream()
                .sorted(Comparator.comparing(UserGame::getPlayTime).reversed())
                .limit(5)
                .map(ug -> {
                    Map<String, Object> gameStats = new HashMap<>();
                    gameStats.put("gameId", ug.getGame().getId());
                    gameStats.put("title", ug.getGame().getTitle());
                    gameStats.put("playTime", ug.getPlayTime());
                    gameStats.put("lastPlayed", ug.getLastPlayedAt());
                    return gameStats;
                })
                .collect(Collectors.toList());
        report.put("mostPlayed", mostPlayed);

        // 最近游玩记录
        List<Map<String, Object>> recentActivity = userGames.stream()
                .filter(ug -> ug.getLastPlayedAt() != null)
                .sorted(Comparator.comparing(UserGame::getLastPlayedAt).reversed())
                .limit(10)
                .map(ug -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("gameId", ug.getGame().getId());
                    activity.put("title", ug.getGame().getTitle());
                    activity.put("playTime", ug.getPlayTime());
                    activity.put("lastPlayed", ug.getLastPlayedAt());
                    return activity;
                })
                .collect(Collectors.toList());
        report.put("recentActivity", recentActivity);

        // 分类统计
        Map<String, Long> categoryStats = userGames.stream()
                .flatMap(ug -> ug.getGame().getCategories().stream())
                .collect(Collectors.groupingBy(
                        category -> category,
                        Collectors.counting()
                ));
        report.put("categoryDistribution", categoryStats);

        return report;
    }

    @Override
    @Transactional
    public void updatePlayTime(Long gameId, Long userId, Integer minutes) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未拥有该游戏"));

        userGame.setPlayTime(userGame.getPlayTime() + minutes);
        userGame.setLastPlayedAt(LocalDateTime.now());
        userGameRepository.save(userGame);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> generateGameReport(Long gameId) {
        Map<String, Object> report = new HashMap<>();
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        // 基础信息
        report.put("title", game.getTitle());
        report.put("rating", game.getRating());
        report.put("ratingCount", game.getRatingCount());
        report.put("popularity", game.getPopularity());

        // 玩家统计
        long playerCount = userGameRepository.countByGameId(gameId);
        report.put("totalPlayers", playerCount);

        // 游玩时间分布
        report.put("playTimeDistribution", getPlayTimeDistribution(gameId));

        // 评分分布
        report.put("ratingDistribution", getRatingDistribution(gameId));

        return report;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getGameTrends(Long gameId, String timeRange) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start;

        // 根据时间范围确定起始时间
        switch (timeRange.toLowerCase()) {
            case "week":
                start = now.minusWeeks(1);
                break;
            case "month":
                start = now.minusMonths(1);
                break;
            case "year":
                start = now.minusYears(1);
                break;
            default:
                throw new BusinessException("不支持的时间范围");
        }

        List<Map<String, Object>> trends = new ArrayList<>();
        List<UserGame> userGames = userGameRepository.findByGameId(gameId);

        // 处理统计数据
        userGames.stream()
                .filter(ug -> ug.getLastPlayedAt().isAfter(start))
                .forEach(ug -> {
                    Map<String, Object> trend = new HashMap<>();
                    trend.put("date", ug.getLastPlayedAt());
                    trend.put("playTime", ug.getPlayTime());
                    trend.put("userCount", 1);
                    trends.add(trend);
                });

        return trends;
    }

    @Override
    @Transactional(readOnly = true)
    public GameDTO getGameById(Long id) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new BusinessException("游戏不存在"));
        return convertToDTO(game);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDTO> searchGames(GameSearchDTO searchDTO, Pageable pageable) {
        return gameRepository.findBySearchCriteria(
                searchDTO.getTitle(),
                searchDTO.getCategories(),
                searchDTO.getMinRating(),
                pageable
        ).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAllCategories() {
        return gameRepository.findAllCategories();
    }

    @Override
    @Transactional
    public void rateGame(Long gameId, Long userId, Double rating) {
        if (rating < 0 || rating > 5) {
            throw new BusinessException("评分必须在0-5之间");
        }

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("您还没有这款游戏"));

        userGame.setUserRating(rating);
        userGameRepository.save(userGame);

        // 更新游戏总评分
        updateGameRating(game);

        // 清除缓存
        cacheService.evictCache("game:" + gameId);
    }

    @Override
    @Scheduled(cron = "0 0 */1 * * ?") // 每小时执行一次
    @Transactional
    public void updatePopularGames() {
        List<Game> games = gameRepository.findAll();
        for (Game game : games) {
            // 更新游戏人气值
            long userCount = userGameRepository.countByGameId(game.getId());
            long postCount = postRepository.countByGameId(game.getId());
            double avgRating = game.getRating();

            // 计算综合人气值：用户数 * (1 + 平均评分/5) * (1 + 帖子数/100)
            int popularity = (int) (userCount * (1 + avgRating/5) * (1 + postCount/100.0));

            game.setPopularity(popularity);
        }
        gameRepository.saveAll(games);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportUserGames(Long userId) {
        List<UserGame> userGames = userGameRepository.findByUserId(userId);
        if (userGames.isEmpty()) {
            throw new BusinessException("没有找到游戏记录");
        }

        StringBuilder csv = new StringBuilder();
        csv.append("游戏名称,购买时间,游戏时长(小时),最后游玩时间,评分\n");

        for (UserGame ug : userGames) {
            csv.append(String.format("%s,%s,%d,%s,%.1f\n",
                    ug.getGame().getTitle(),
                    formatDateTime(ug.getPurchasedAt()),
                    ug.getPlayTime() / 60, // 转换为小时
                    formatDateTime(ug.getLastPlayedAt()),
                    ug.getUserRating() != null ? ug.getUserRating() : 0.0
            ));
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GameDTO> getUserGames(Long userId, Pageable pageable) {
        return userGameRepository.findByUserId(userId, pageable)
                .map(userGame -> convertToDTO(userGame.getGame()));
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private void updateGameRating(Game game) {
        List<UserGame> userGames = userGameRepository.findByGameId(game.getId());
        double avgRating = userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .average()
                .orElse(0.0);

        game.setRating(avgRating);
        game.setRatingCount((int) userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .count());

        gameRepository.save(game);
    }

    private GameDTO convertToDTO(Game game) {
        GameDTO dto = new GameDTO();
        BeanUtils.copyProperties(game, dto);
        return dto;
    }

    @Override
    @Transactional
    public void addGameToUser(Long gameId, Long userId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new BusinessException("游戏不存在"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (userGameRepository.existsByGameIdAndUserId(gameId, userId)) {
            throw new BusinessException("已拥有该游戏");
        }

        UserGame userGame = new UserGame();
        userGame.setGame(game);
        userGame.setUser(user);
        userGame.setPurchasedAt(LocalDateTime.now());
        userGame.setPlayTime(0);
        userGame.setLastPlayedAt(LocalDateTime.now());
        userGameRepository.save(userGame);
    }

    @Override
    @Transactional
    public void removeGameFromUser(Long gameId, Long userId) {
        UserGame userGame = userGameRepository.findByGameIdAndUserId(gameId, userId)
                .orElseThrow(() -> new BusinessException("未拥有该游戏"));
        userGameRepository.delete(userGame);
    }
}