package com.gameplatform.repository;

import com.gameplatform.model.entity.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:23
 * @description TODO
 */
public interface ReportRepository extends JpaRepository<Report, Long> {
    Page<Report> findByStatus(Report.ReportStatus status, Pageable pageable);
    Page<Report> findByReporterId(Long reporterId, Pageable pageable);
    boolean existsByReporterIdAndTypeAndTargetIdAndStatus(
            Long reporterId,
            Report.ReportType type,
            Long targetId,
            Report.ReportStatus status
    );
    long countByStatus(Report.ReportStatus status);
}