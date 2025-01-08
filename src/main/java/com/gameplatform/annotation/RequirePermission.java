package com.gameplatform.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:55
 * @description TODO
 */
@Target({ElementType.METHOD, ElementType.TYPE})  // 增加了TYPE，允许在类级别使用
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    /**
     * 所需权限标识
     */
    String value() default "";

    /**
     * 权限逻辑类型
     */
    LogicType logic() default LogicType.AND;

    /**
     * 自定义错误消息
     */
    String message() default "无权限执行此操作";

    /**
     * 是否要求认证
     */
    boolean requireAuthentication() default true;

    /**
     * 权限验证逻辑类型枚举
     */
    enum LogicType {
        AND,    // 必须满足所有权限
        OR      // 满足任一权限即可
    }

    /**
     * 所需角色
     */
    String[] roles() default {};
}