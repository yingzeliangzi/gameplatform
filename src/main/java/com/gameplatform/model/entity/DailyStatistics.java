package com.gameplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:42
 * @description TODO
 */
@Data
@Entity
@Table(name = "daily_statistics")
public class DailyStatistics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime date;

    private Long newUsers;
    private Long activeUsers;
    private Long newPosts;
    private Long newRegistrations;
    private Long totalGameTime;
    private Long dailyActiveUsers;
    private Long totalTransactions;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String additionalData;

    @Version
    private Long version;
}