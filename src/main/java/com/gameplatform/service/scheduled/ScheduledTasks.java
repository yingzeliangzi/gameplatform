package com.gameplatform.service.scheduled;

import com.gameplatform.model.dto.NotificationDTO;
import com.gameplatform.model.entity.Event;
import com.gameplatform.model.entity.Notification;
import com.gameplatform.model.entity.User;
import com.gameplatform.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:42
 * @description TODO
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasks {

    private final UserService userService;
    private final GameService gameService;
    private final StatisticsService statisticsService;
    private final NotificationService notificationService;
    private final CacheService cacheService;

    // 每天凌晨2点执行统计任务
    @Scheduled(cron = "0 0 2 * * ?")
    public void generateDailyStatistics() {
        try {
            log.info("开始生成每日统计数据");
            statisticsService.generateDailyStatistics();
            log.info("每日统计数据生成完成");
        } catch (Exception e) {
            log.error("生成每日统计数据失败", e);
        }
    }

    // 每小时检查并清理过期缓存
    @Scheduled(fixedRate = 3600000)
    public void cleanExpiredCache() {
        try {
            log.info("开始清理过期缓存");
            cacheService.cleanExpiredCache();
            log.info("过期缓存清理完成");
        } catch (Exception e) {
            log.error("清理过期缓存失败", e);
        }
    }

    // 每30分钟更新热门游戏列表
    @Scheduled(fixedRate = 1800000)
    public void updatePopularGames() {
        try {
            log.info("开始更新热门游戏列表");
            gameService.updatePopularGames();
            log.info("热门游戏列表更新完成");
        } catch (Exception e) {
            log.error("更新热门游戏列表失败", e);
        }
    }

    // 每天凌晨1点发送系统通知
    @Scheduled(cron = "0 0 1 * * ?")
    public void sendSystemNotifications() {
        try {
            log.info("开始发送系统通知");
            // 获取需要发送通知的用户
            List<User> users = userService.getActiveUsers();

            // 批量发送通知
            NotificationDTO notification = new NotificationDTO();
            notification.setType(Notification.NotificationType.SYSTEM);
            notification.setTitle("每日登录提醒");
            notification.setContent("记得每天登录签到领取奖励哦！");

            users.forEach(user ->
                    notificationService.sendNotification(user.getId(), notification)
            );

            log.info("系统通知发送完成");
        } catch (Exception e) {
            log.error("发送系统通知失败", e);
        }
    }

    // 每5分钟检查一次事件提醒
    @Scheduled(fixedRate = 300000)
    public void checkEventReminders() {
        try {
            log.info("开始检查事件提醒");
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime oneHourLater = now.plusHours(1);

            // 获取即将开始的事件
            List<Event> upcomingEvents = eventService.getEventsStartingBetween(now, oneHourLater);

            // 发送提醒
            for (Event event : upcomingEvents) {
                notificationService.sendEventReminders(event);
            }

            log.info("事件提醒检查完成");
        } catch (Exception e) {
            log.error("检查事件提醒失败", e);
        }
    }

    // 每天凌晨3点清理临时文件
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupTempFiles() {
        try {
            log.info("开始清理临时文件");
            Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"), "gameplatform");

            if (Files.exists(tempDir)) {
                Files.walk(tempDir)
                        .sorted(Comparator.reverseOrder())
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                log.error("删除临时文件失败: {}", path, e);
                            }
                        });
            }

            log.info("临时文件清理完成");
        } catch (Exception e) {
            log.error("清理临时文件失败", e);
        }
    }
}