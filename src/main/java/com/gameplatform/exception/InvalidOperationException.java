package com.gameplatform.exception;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:00
 * @description TODO
 */
public class InvalidOperationException extends BusinessException {
    public InvalidOperationException(String message) {
        super("INVALID_OPERATION", message);
    }
}