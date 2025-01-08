package com.gameplatform.model.dto;

import com.gameplatform.model.entity.Event;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:32
 * @description TODO
 */
@Data
public class EventDTO {
    private Long id;
    private String title;
    private String description;
    private Long gameId;
    private String gameName;
    private Event.EventType type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private String location;
    private String coverImage;
    private Set<String> images;
    private Boolean isOnline;
    private Event.EventStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isRegistered;
    private String contactInfo;
    private String remark;
}