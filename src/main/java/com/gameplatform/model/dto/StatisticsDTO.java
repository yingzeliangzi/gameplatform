package com.gameplatform.model.dto;

import lombok.Data;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:42
 * @description TODO
 */
@Data
public class StatisticsDTO {
    // 用户统计
    private long totalUsers;
    private long activeUsers;

    // 游戏统计
    private long totalGames;
    private long totalPlayTime;

    // 社区统计
    private long totalPosts;
    private long totalComments;

    // 活动统计
    private long totalEvents;
    private long ongoingEvents;
    private long totalRegistrations;

    // 其他统计数据
}
