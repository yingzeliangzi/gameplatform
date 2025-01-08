package com.gameplatform.aspect;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:55
 * @description TODO
 */
@Aspect
@Component
@RequiredArgsConstructor
public class PermissionAspect {

    @Before("@annotation(requirePermission) || @within(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        // 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 检查是否需要认证
        if (requirePermission.requireAuthentication()) {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new BusinessException("未登录或登录已过期");
            }
        }

        // 获取用户信息
        UserDTO user = (UserDTO) authentication.getPrincipal();
        if (user == null) {
            throw new BusinessException("无法获取用户信息");
        }

        // 获取目标方法的权限注解
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        RequirePermission methodPermission = AnnotationUtils.findAnnotation(method, RequirePermission.class);
        RequirePermission classPermission = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RequirePermission.class);

        // 合并类级别和方法级别的权限
        String[] permissions = mergePermissions(methodPermission, classPermission);
        String[] roles = mergeRoles(methodPermission, classPermission);

        // 验证角色
        if (roles.length > 0 && !hasAnyRole(user.getRoles(), roles)) {
            throw new BusinessException(requirePermission.message());
        }

        // 验证权限
        if (permissions.length > 0) {
            boolean hasPermission = false;
            if (requirePermission.logic() == RequirePermission.LogicType.AND) {
                hasPermission = hasAllPermissions(user, permissions);
            } else {
                hasPermission = hasAnyPermission(user, permissions);
            }

            if (!hasPermission) {
                throw new BusinessException(requirePermission.message());
            }
        }
    }

    private String[] mergePermissions(RequirePermission methodPermission, RequirePermission classPermission) {
        String methodValue = methodPermission != null ? methodPermission.value() : "";
        String classValue = classPermission != null ? classPermission.value() : "";

        return Arrays.stream(new String[]{methodValue, classValue})
                .filter(v -> !v.isEmpty())
                .distinct()
                .toArray(String[]::new);
    }

    private String[] mergeRoles(RequirePermission methodPermission, RequirePermission classPermission) {
        String[] methodRoles = methodPermission != null ? methodPermission.roles() : new String[0];
        String[] classRoles = classPermission != null ? classPermission.roles() : new String[0];

        return Stream.concat(
                        Arrays.stream(methodRoles),
                        Arrays.stream(classRoles)
                )
                .distinct()
                .toArray(String[]::new);
    }

    private boolean hasAnyRole(Set<String> userRoles, String[] requiredRoles) {
        return Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
    }

    private boolean hasAllPermissions(UserDTO user, String[] permissions) {
        return Arrays.stream(permissions)
                .allMatch(permission -> hasPermission(user, permission));
    }

    private boolean hasAnyPermission(UserDTO user, String[] permissions) {
        return Arrays.stream(permissions)
                .anyMatch(permission -> hasPermission(user, permission));
    }

    private boolean hasPermission(UserDTO user, String permission) {
        // 管理员默认拥有所有权限
        if (user.getRoles().contains("ADMIN")) {
            return true;
        }
        // 验证具体权限
        return user.getPermissions() != null && user.getPermissions().contains(permission);
    }
}