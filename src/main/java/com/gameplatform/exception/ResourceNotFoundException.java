package com.gameplatform.exception;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 15:58
 * @description TODO
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
