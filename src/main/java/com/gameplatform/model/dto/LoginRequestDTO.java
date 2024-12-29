package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:18
 * @description TODO
 */
@Data
public class LoginRequestDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
