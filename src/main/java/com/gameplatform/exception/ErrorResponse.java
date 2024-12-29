package com.gameplatform.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:13
 * @description TODO
 */
@Data
@AllArgsConstructor
public class ErrorResponse {
    private String code;
    private String message;
}
