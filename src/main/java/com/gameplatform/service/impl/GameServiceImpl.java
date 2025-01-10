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
import org.springframework.data.domain.Pageable;
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
    @Transactional(readOnly = true)
    public Map<String, Object> generateUserGamingReport(Long userId) {
        Map<String, Object> report = new HashMap<>();

        List<UserGame> userGames = userGameRepository.findByUserId(userId);

        report.put("totalGames", userGames.size());
        report.put("totalPlayTime", userGames.stream()
                .mapToInt(UserGame::getPlayTime)
                .sum());
        report.put("averageRating", userGames.stream()
                .filter(ug -> ug.getUserRating() != null)
                .mapToDouble(UserGame::getUserRating)
                .average()
                .orElse(0.0));

        // 添加最近游戏记录
        report.put("recentGames", userGames.stream()
                .sorted(Comparator.comparing(UserGame::getLastPlayedAt).reversed())
                .limit(5)
                .map(ug -> convertToDTO(ug.getGame()))
                .collect(Collectors.toList()));

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