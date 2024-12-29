package com.gameplatform.model.dto;

import com.gameplatform.model.entity.Event;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:32
 * @description TODO
 */
@Data
public class EventListItemDTO {
    private Long id;
    private String title;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer currentParticipants;
    private Integer maxParticipants;
    private String status;
    private boolean isRegistered;
}