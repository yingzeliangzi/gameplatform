package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/09 15:39
 * @description TODO
 */
@Data
public class PasswordUpdateDTO {
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String newPassword;
}