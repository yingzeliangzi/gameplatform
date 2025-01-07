package com.gameplatform.service.impl;

import com.gameplatform.model.dto.StatisticsDTO;
import com.gameplatform.model.entity.*;
import com.gameplatform.repository.*;
import com.gameplatform.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:41
 * @description TODO
 */
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PostRepository postRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserGameRepository userGameRepository;
    private final DailyStatisticsRepository dailyStatisticsRepository;

    @Override
    @Cacheable(value = "statistics", key = "'overview'")
    public StatisticsDTO getOverviewStatistics() {
        StatisticsDTO statistics = new StatisticsDTO();

        // 设置用户统计
        statistics.setTotalUsers(userRepository.count());
        statistics.setActiveUsers(userRepository.countByStatus(User.UserStatus.ACTIVE));

        // 设置游戏统计
        statistics.setTotalGames(gameRepository.count());
        Long totalPlayTime = userGameRepository.sumPlayTimeByAll();
        statistics.setTotalPlayTime(totalPlayTime != null ? totalPlayTime : 0L);

        // 设置社区统计
        statistics.setTotalPosts(postRepository.count());
        statistics.setTotalComments(postRepository.countTotalComments());

        // 设置活动统计
        statistics.setTotalEvents(eventRepository.count());
        statistics.setOngoingEvents(eventRepository.countByStatus(Event.EventStatus.ONGOING));
        statistics.setTotalRegistrations(eventRegistrationRepository.count());

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minus(7, ChronoUnit.DAYS);

        // 近期新增用户统计
        stats.put("newUsers", userRepository.countByCreatedAtBetween(lastWeek, now));
        stats.put("activeUsers", userRepository.countByLastLoginTimeAfter(lastWeek));

        // 热门游戏
        stats.put("popularGames", gameRepository.findTop10ByOrderByPopularityDesc());

        // 热门帖子
        stats.put("popularPosts", postRepository.findTop10ByOrderByViewCountDesc());

        // 活动参与统计
        stats.put("eventParticipation", eventRegistrationRepository.countByRegisteredAtBetween(lastWeek, now));

        // 最近30天的统计趋势
        stats.put("trends", getTrendStatistics());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getUserStatistics(Long userId) {
        Map<String, Object> stats = new HashMap<>();

        // 游戏统计
        stats.put("gameCount", userGameRepository.countByUserId(userId));
        stats.put("totalPlayTime", userGameRepository.sumPlayTimeByUserId(userId));

        // 社区统计
        stats.put("postCount", postRepository.countByAuthorId(userId));
        stats.put("receivedLikes", postRepository.countLikesByAuthorId(userId));

        // 活动统计
        stats.put("eventParticipation", eventRegistrationRepository.countByUserId(userId));

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getGameStatistics(Long gameId) {
        Map<String, Object> stats = new HashMap<>();

        // 基础统计
        stats.put("ownerCount", userGameRepository.countByGameId(gameId));
        stats.put("avgPlayTime", userGameRepository.avgPlayTimeByGameId(gameId));
        stats.put("postCount", postRepository.countByGameId(gameId));
        stats.put("eventCount", eventRepository.countByGameId(gameId));

        // 玩家数据分布
        Map<String, Long> playTimeDistribution = userGameRepository.getPlayTimeDistribution(gameId);
        stats.put("playTimeDistribution", playTimeDistribution);

        // 评分分布
        Map<Integer, Long> ratingDistribution = userGameRepository.getRatingDistribution(gameId);
        stats.put("ratingDistribution", ratingDistribution);

        return stats;
    }

    @Override
    public Map<String, List<Number>> getTrendStatistics() {
        Map<String, List<Number>> trends = new HashMap<>();

        List<DailyStatistics> dailyStats = dailyStatisticsRepository.findTop30ByOrderByDateDesc();

        // 处理统计数据
        trends.put("newUsers", extractTrendData(dailyStats, DailyStatistics::getNewUsers));
        trends.put("activeUsers", extractTrendData(dailyStats, DailyStatistics::getActiveUsers));
        trends.put("newPosts", extractTrendData(dailyStats, DailyStatistics::getNewPosts));
        trends.put("newRegistrations", extractTrendData(dailyStats, DailyStatistics::getNewRegistrations));

        return trends;
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨执行
    @Transactional
    public void generateDailyStatistics() {
        LocalDateTime yesterday = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        LocalDateTime startOfDay = yesterday.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);

        DailyStatistics stats = new DailyStatistics();
        stats.setDate(startOfDay);

        // 收集统计数据
        stats.setNewUsers(userRepository.countByCreatedAtBetween(startOfDay, endOfDay));
        stats.setActiveUsers(userRepository.countByLastLoginTimeAfter(startOfDay));
        stats.setNewPosts(postRepository.countByCreatedAtBetween(startOfDay, endOfDay));
        stats.setNewRegistrations(eventRegistrationRepository.countByRegisteredAtBetween(startOfDay, endOfDay));
        stats.setTotalGameTime(userGameRepository.sumPlayTimeInTimeRange(startOfDay, endOfDay));
        stats.setDailyActiveUsers(userRepository.countDailyActiveUsers(startOfDay, endOfDay));

        dailyStatisticsRepository.save(stats);
    }

    private <T extends Number> List<T> extractTrendData(List<DailyStatistics> stats,
                                                        java.util.function.Function<DailyStatistics, T> extractor) {
        return stats.stream()
                .map(extractor)
                .collect(Collectors.toList());
    }
}