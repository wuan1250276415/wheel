package com.basebackend.wheel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 审计字段填充工具类
 * 用于统一设置实体的创建人、更新人等审计字段
 */
@Slf4j
@Component
public class AuditHelper {

    /**
     * 系统操作用户ID（用于无法获取当前用户的场景，如定时任务）
     */
    private static final Long SYSTEM_USER_ID = 0L;

    /**
     * 获取当前操作用户ID
     * 如果无法获取当前用户，返回系统用户ID
     *
     * @return 用户ID
     */
    public static Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof Long) {
                    return (Long) principal;
                } else if (principal instanceof String) {
                    try {
                        return Long.parseLong((String) principal);
                    } catch (NumberFormatException e) {
                        log.debug("用户ID格式错误: {}", principal);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户ID: {}", e.getMessage());
        }
        return SYSTEM_USER_ID;
    }

    /**
     * 静态方法：获取当前用户ID（用于不方便注入的场景）
     *
     * @return 用户ID
     */
    public static Long getOperatorId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                Object principal = authentication.getPrincipal();
                if (principal instanceof Long) {
                    return (Long) principal;
                } else if (principal instanceof String) {
                    try {
                        return Long.parseLong((String) principal);
                    } catch (NumberFormatException e) {
                        log.debug("用户ID格式错误: {}", principal);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户ID: {}", e.getMessage());
        }
        return SYSTEM_USER_ID;
    }

    /**
     * 获取当前用户名
     *
     * @return 用户名
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("无法获取当前用户名: {}", e.getMessage());
        }
        return "system";
    }

    /**
     * 设置创建审计字段
     *
     * @param entity 实体对象
     * @param userId 用户ID
     */
    public static void setCreateAuditFields(Object entity, Long userId) {
        if (entity == null) {
            return;
        }
        try {
            // 通过反射设置审计字段
            java.lang.reflect.Field createdAtField = entity.getClass().getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            if (createdAtField.get(entity) == null) {
                createdAtField.set(entity, LocalDateTime.now());
            }

            java.lang.reflect.Field createByField = entity.getClass().getDeclaredField("createBy");
            createByField.setAccessible(true);
            if (createByField.get(entity) == null) {
                createByField.set(entity, userId);
            }

            // 同时更新更新字段
            setUpdateAuditFields(entity, userId);
        } catch (Exception e) {
            log.debug("设置创建审计字段失败: {}", e.getMessage());
        }
    }

    /**
     * 设置更新审计字段
     *
     * @param entity 实体对象
     * @param userId 用户ID
     */
    public static void setUpdateAuditFields(Object entity, Long userId) {
        if (entity == null) {
            return;
        }
        try {
            java.lang.reflect.Field updatedAtField = entity.getClass().getDeclaredField("updatedAt");
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, LocalDateTime.now());

            java.lang.reflect.Field updateByField = entity.getClass().getDeclaredField("updateBy");
            updateByField.setAccessible(true);
            updateByField.set(entity, userId);
        } catch (Exception e) {
            log.debug("设置更新审计字段失败: {}", e.getMessage());
        }
    }
}
