package com.gameplatform.aspect;

import com.gameplatform.annotation.RequirePermission;
import com.gameplatform.exception.BusinessException;
import com.gameplatform.model.dto.UserDTO;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @author SakurazawaRyoko
 * @version 1.0
 * @date 2025/01/05 13:55
 * @description TODO
 */
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new BusinessException("未登录");
        }

        UserDTO user = (UserDTO) authentication.getPrincipal();
        String requiredPermission = requirePermission.value();

        if (!hasPermission(user, requiredPermission)) {
            throw new BusinessException("无权限执行此操作");
        }
    }

    private boolean hasPermission(UserDTO user, String permission) {
        return user.getRoles().contains("ADMIN") ||
                user.getPermissions().contains(permission);
    }
}