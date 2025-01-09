package com.gameplatform.model.message;

import com.gameplatform.model.dto.NotificationDTO;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/06 1:23
 * @description TODO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {
    private Long userId;
    private String type;
    private String title;
    private String content;

    public NotificationMessage(Long userId, NotificationDTO notificationDTO) {
        this.userId = userId;
        this.type = notificationDTO.getType().toString();
        this.title = notificationDTO.getTitle();
        this.content = notificationDTO.getContent();
    }
}