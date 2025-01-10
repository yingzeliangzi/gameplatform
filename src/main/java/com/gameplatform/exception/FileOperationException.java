package com.gameplatform.exception;/**
 * @author SakurazawaRyoko
 * @date 2024/12/29 15:59
 * @description TODO
 * @version 1.0
 */
public class FileOperationException extends BusinessException {
    public FileOperationException(String message) {
        super("FILE_OPERATION_ERROR", message);
    }

    public FileOperationException(String message, Throwable cause) {
        super("FILE_OPERATION_ERROR", message + ": " + cause.getMessage());
    }
}