package com.gameplatform.model.entity;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:32
 * @description TODO
 */

@Data
@Entity
@Table(name = "event_registrations")
public class EventRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegistrationStatus status = RegistrationStatus.REGISTERED;

    private String contactInfo;

    private String remark;

    @CreationTimestamp
    private LocalDateTime registeredAt;

    private LocalDateTime cancelledAt;

    public enum RegistrationStatus {
        REGISTERED,   // 已报名
        CANCELLED,    // 已取消
        ATTENDED,     // 已参加
        ABSENT       // 缺席
    }
}