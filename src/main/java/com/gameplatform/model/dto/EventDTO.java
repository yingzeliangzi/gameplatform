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

    @NotBlank(message = "活动标题不能为空")
    private String title;

    @NotBlank(message = "活动描述不能为空")
    private String description;

    private Long gameId;
    private String gameName;

    @NotNull(message = "活动类型不能为空")
    private Event.EventType type;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
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
}