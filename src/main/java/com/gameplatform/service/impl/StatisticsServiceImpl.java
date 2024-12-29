package com.gameplatform.service.impl;

import com.gameplatform.model.dto.StatisticsDTO;
import com.gameplatform.repository.*;
import com.gameplatform.service.StatisticsService;
import com.gameplatform.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    @Cacheable(value = "statistics", key = "'overview'")
    public StatisticsDTO getOverviewStatistics() {
        StatisticsDTO statistics = new StatisticsDTO();

        // 用户统计
        statistics.setTotalUsers(userRepository.count());
        statistics.setActiveUsers(userRepository.countByStatus(User.UserStatus.ACTIVE));

        // 游戏统计
        statistics.setTotalGames(gameRepository.count());

        // 社区统计
        statistics.setTotalPosts(postRepository.count());

        // 活动统计
        statistics.setTotalEvents(eventRepository.count());
        statistics.setOngoingEvents(eventRepository.countByStatus(Event.EventStatus.ONGOING));

        return statistics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastWeek = now.minusWeeks(1);

        // 新增用户统计
        stats.put("newUsers", userRepository.countByCreatedAtBetween(lastWeek, now));

        // 活跃用户统计
        stats.put("activeUsers", userRepository.countByLastLoginTimeAfter(lastWeek));

        // 热门游戏
        stats.put("popularGames", gameRepository.findTop10ByOrderByPopularityDesc());

        // 热门帖子
        stats.put("popularPosts", postRepository.findTop10ByOrderByViewCountDesc());

        // 活动参与统计
        stats.put("eventParticipation", eventRegistrationRepository.countByRegisteredAtBetween(lastWeek, now));

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

        // 拥有该游戏的用户数
        stats.put("ownerCount", userGameRepository.countByGameId(gameId));

        // 平均游戏时长
        stats.put("avgPlayTime", userGameRepository.avgPlayTimeByGameId(gameId));

        // 相关帖子数
        stats.put("postCount", postRepository.countByGameId(gameId));

        // 相关活动数
        stats.put("eventCount", eventRepository.countByGameId(gameId));

        return stats;
    }

    @Scheduled(cron = "0 0 0 * * *")  // 每天零点执行
    @Transactional
    public void generateDailyStatistics() {
        DailyStatistics stats = new DailyStatistics();
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // 新增用户
        stats.setNewUsers(userRepository.countByCreatedAtBetween(
                yesterday.withHour(0),
                yesterday.withHour(23).withMinute(59)
        ));

        // 活跃用户
        stats.setActiveUsers(userRepository.countByLastLoginTimeAfter(yesterday));

        // 新增帖子
        stats.setNewPosts(postRepository.countByCreatedAtBetween(
                yesterday.withHour(0),
                yesterday.withHour(23).withMinute(59)
        ));

        // 新增活动报名
        stats.setNewRegistrations(eventRegistrationRepository.countByRegisteredAtBetween(
                yesterday.withHour(0),
                yesterday.withHour(23).withMinute(59)
        ));

        dailyStatisticsRepository.save(stats);
    }

    @Override
    @Cacheable(value = "statistics", key = "'trends'")
    public Map<String, List<Number>> getTrendStatistics() {
        Map<String, List<Number>> trends = new HashMap<>();

        // 获取最近30天的统计数据
        List<DailyStatistics> dailyStats = dailyStatisticsRepository
                .findTop30ByOrderByDateDesc();

        // 处理统计数据为趋势数据
        trends.put("newUsers", dailyStats.stream()
                .map(DailyStatistics::getNewUsers)
                .collect(Collectors.toList()));

        trends.put("activeUsers", dailyStats.stream()
                .map(DailyStatistics::getActiveUsers)
                .collect(Collectors.toList()));

        trends.put("newPosts", dailyStats.stream()
                .map(DailyStatistics::getNewPosts)
                .collect(Collectors.toList()));

        trends.put("newRegistrations", dailyStats.stream()
                .map(DailyStatistics::getNewRegistrations)
                .collect(Collectors.toList()));

        return trends;
    }

    private StatisticsDTO buildStatisticsDTO(List<Object[]> results) {
        StatisticsDTO dto = new StatisticsDTO();
        // 处理统计结果...
        return dto;
    }
}
