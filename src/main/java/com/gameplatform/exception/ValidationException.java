package com.gameplatform.exception;

import lombok.Getter;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:59
 * @description TODO
 */
@Getter
public class ValidationException extends BusinessException {
    private final Object validationResult;

    public ValidationException(String message) {
        this(message, null);
    }

    public ValidationException(String message, Object validationResult) {
        super(BusinessException.ErrorCode.VALIDATION_ERROR, message);
        this.validationResult = validationResult;
    }

    public ValidationException(String message, Object validationResult, Object[] args) {
        super(BusinessException.ErrorCode.VALIDATION_ERROR, message, args);
        this.validationResult = validationResult;
    }
}