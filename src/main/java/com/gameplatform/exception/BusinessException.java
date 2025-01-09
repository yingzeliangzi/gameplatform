package com.gameplatform.exception;

import lombok.Getter;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 1:09
 * @description TODO
 */
@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final String message;
    private final Object[] args;

    public BusinessException(String message) {
        this("BUSINESS_ERROR", message);
    }

    public BusinessException(String code, String message) {
        this(code, message, null);
    }

    public BusinessException(String code, String message, Object[] args) {
        super(message);
        this.code = code;
        this.message = message;
        this.args = args;
    }

    public static class ErrorCode {
        public static final String UNAUTHORIZED = "UNAUTHORIZED";
        public static final String FORBIDDEN = "FORBIDDEN";
        public static final String NOT_FOUND = "NOT_FOUND";
        public static final String ALREADY_EXISTS = "ALREADY_EXISTS";
        public static final String INVALID_PARAMETER = "INVALID_PARAMETER";
        public static final String SYSTEM_ERROR = "SYSTEM_ERROR";
        public static final String OPERATION_FAILED = "OPERATION_FAILED";
        public static final String DATA_ERROR = "DATA_ERROR";
        public static final String VALIDATION_ERROR = "VALIDATION_ERROR";
    }
}
