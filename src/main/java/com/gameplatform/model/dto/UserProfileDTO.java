package com.gameplatform.model.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/09 15:38
 * @description TODO
 */
@Data
public class UserProfileDTO {
    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称最长30个字符")
    private String nickname;

    @Size(max = 200, message = "个人简介最长200个字符")
    private String bio;
}