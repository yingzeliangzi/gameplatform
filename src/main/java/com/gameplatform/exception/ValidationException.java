package com.gameplatform.exception;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:59
 * @description TODO
 */
public class ValidationException extends BusinessException {
    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }
}