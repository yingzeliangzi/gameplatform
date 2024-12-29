package com.gameplatform.model.dto;

import lombok.Data;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 14:18
 * @description TODO
 */
@Data
public class LoginResponseDTO {
    private String token;
    private UserDTO user;
}
