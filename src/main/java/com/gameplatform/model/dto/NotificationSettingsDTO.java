package com.gameplatform.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/09 15:36
 * @description TODO
 */
@Data
@Schema(description = "通知设置")
public class NotificationSettingsDTO {
    @NotNull
    @Schema(description = "是否接收系统通知")
    private Boolean systemNotification;

    @NotNull
    @Schema(description = "是否接收游戏通知")
    private Boolean gameNotification;

    @NotNull
    @Schema(description = "是否接收活动通知")
    private Boolean eventNotification;

    @NotNull
    @Schema(description = "是否接收邮件通知")
    private Boolean emailNotification;

    @Schema(description = "推送时间范围开始")
    private String pushTimeStart;

    @Schema(description = "推送时间范围结束")
    private String pushTimeEnd;
}