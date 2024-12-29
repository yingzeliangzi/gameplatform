package com.gameplatform.repository;

import com.gameplatform.model.entity.DailyStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:43
 * @description TODO
 */
public interface DailyStatisticsRepository extends JpaRepository<DailyStatistics, Long> {
    List<DailyStatistics> findTop30ByOrderByDateDesc();
}