package com.gameplatform.model.dto;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;


/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:04
 * @description TODO
 */
@Data
public class UserDTO {
    private Long id;

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度必须在2-20之间")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    private String avatar;

    private String bio;

    private Set<String> roles;

    private boolean isVerified;

    private String phone;

    private String status;

    private LocalDateTime lastLoginTime;

    private LocalDateTime createdAt;

    // 额外的DTO字段，不映射到实体
    private String token;
    private Integer followerCount;
    private Integer followingCount;
    private Integer gameCount;
    private Integer postCount;
    private boolean isFollowing;
    private boolean isBlocked;
    private Set<String> permissions;
}
