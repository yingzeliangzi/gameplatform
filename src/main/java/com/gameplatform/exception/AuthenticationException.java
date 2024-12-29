package com.gameplatform.exception;

import lombok.Getter;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:59
 * @description TODO
 */
@Getter
public class AuthenticationException extends BusinessException {
    public AuthenticationException(String message) {
        super("AUTHENTICATION_ERROR", message);
    }
}
