package com.gameplatform.service;

import com.gameplatform.model.dto.StatisticsDTO;

import java.util.List;
import java.util.Map;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:11
 * @description TODO
 */
public interface StatisticsService {
    StatisticsDTO getOverviewStatistics();
    Map<String, Object> getAdminDashboardStatistics();
    Map<String, Object> getUserStatistics(Long userId);
    Map<String, Object> getGameStatistics(Long gameId);
    Map<String, List<Number>> getTrendStatistics();
}
