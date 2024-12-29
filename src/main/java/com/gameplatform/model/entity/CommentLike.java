package com.gameplatform.model.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:18
 * @description TODO
 */
@Data
@Entity
@Table(name = "comment_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
