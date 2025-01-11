package com.gameplatform.common;

import lombok.Data;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:49
 * @description TODO
 */
@Data
public class Result<T> {
    private String code;
    private String message;
    private T data;

    private Result(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 添加新的重载方法
    public static <T> Result<T> error(String code, String message, Object data) {
        return new Result<>(code, message, (T)data);
    }

    // 现有的方法保持不变
    public static <T> Result<T> success() {
        return new Result<>("200", "操作成功", null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>("200", "操作成功", data);
    }

    public static <T> Result<T> success(String message, T data) {
        return new Result<>("200", message, data);
    }

    public static <T> Result<T> error(String code, String message) {
        return new Result<>(code, message, null);
    }

    public static <T> Result<T> error(String message) {
        return new Result<>("500", message, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(String.valueOf(code), message, null);
    }
}