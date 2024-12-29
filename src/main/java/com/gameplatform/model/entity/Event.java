package com.gameplatform.model.entity;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 15:10
 * @description TODO
 */
@Data
@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id")
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer currentParticipants = 0;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private Set<EventRegistration> registrations;

    private String location;

    private String coverImage;

    @ElementCollection
    @CollectionTable(name = "event_images")
    private Set<String> images;

    @Column(nullable = false)
    private Boolean isOnline = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status = EventStatus.UPCOMING;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public enum EventType {
        TOURNAMENT,    // 比赛
        MEETUP,       // 线下聚会
        EXHIBITION,   // 展会
        WORKSHOP      // 工作坊
    }

    public enum EventStatus {
        UPCOMING,     // 即将开始
        ONGOING,      // 进行中
        ENDED,        // 已结束
        CANCELLED     // 已取消
    }
}