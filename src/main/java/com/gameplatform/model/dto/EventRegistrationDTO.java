package com.gameplatform.model.dto;

import com.gameplatform.model.entity.EventRegistration;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/28 19:33
 * @description TODO
 */
@Data
public class EventRegistrationDTO {
    private Long id;
    private Long eventId;
    private Long userId;
    private String username;
    private String contactInfo;
    private String remark;
    private EventRegistration.RegistrationStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime cancelledAt;
}