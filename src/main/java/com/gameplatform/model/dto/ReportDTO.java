package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 13:36
 * @description TODO
 */
@Data
public class ReportDTO {
    private Long id;

    @NotNull(message = "举报类型不能为空")
    private String type;

    @NotNull(message = "举报目标不能为空")
    private Long targetId;

    @NotBlank(message = "举报理由不能为空")
    private String reason;

    private String description;

    private UserDTO reporter;
    private UserDTO handler;
    private String status;
    private String handleResult;
    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

}