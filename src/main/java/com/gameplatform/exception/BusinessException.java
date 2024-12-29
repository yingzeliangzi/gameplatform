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

    public BusinessException(String message) {
        this("BIZ_ERROR", message);
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.code = "BIZ_ERROR";
        this.message = message;
    }
}
