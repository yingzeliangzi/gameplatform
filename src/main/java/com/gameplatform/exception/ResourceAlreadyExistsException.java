package com.gameplatform.exception;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2024/12/29 16:00
 * @description TODO
 */
public class ResourceAlreadyExistsException extends BusinessException {
    public ResourceAlreadyExistsException(String message) {
        super("RESOURCE_ALREADY_EXISTS", message);
    }
}