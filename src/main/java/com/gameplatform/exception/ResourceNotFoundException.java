package com.gameplatform.exception;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:58
 * @description TODO
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.NOT_FOUND, message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(ErrorCode.NOT_FOUND,
                String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue, Object[] args) {
        super(ErrorCode.NOT_FOUND,
                String.format("%s not found with %s : '%s'", resourceName, fieldName, fieldValue),
                args);
    }
}