package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/09 15:36
 * @description TODO
 */
@Data
public class NotificationSettingsDTO {
    @NotNull
    private Boolean systemNotification;

    @NotNull
    private Boolean gameNotification;

    @NotNull
    private Boolean eventNotification;

    @NotNull
    private Boolean emailNotification;

    private String pushTimeStart;
    private String pushTimeEnd;
    private Boolean soundEnabled;
    private Boolean vibrationEnabled;
}