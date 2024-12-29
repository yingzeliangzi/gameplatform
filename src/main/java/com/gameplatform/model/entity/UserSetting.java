package com.gameplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:45
 * @description TODO
 */
@Data
@Entity
@Table(name = "user_settings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "key"}))
public class UserSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String key;

    @Column(columnDefinition = "TEXT")
    private String value;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
