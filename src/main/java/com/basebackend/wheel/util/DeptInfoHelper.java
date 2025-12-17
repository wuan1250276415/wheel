package com.basebackend.wheel.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 部门信息获取工具类（简化版）
 */
@Slf4j
@Component
public class DeptInfoHelper {

    /**
     * 获取部门名称
     * 简化版直接返回空字符串，后续可根据需要扩展
     *
     * @param deptId 部门ID
     * @return 部门名称
     */
    public String getDeptName(Long deptId) {
        if (deptId == null) {
            return "";
        }
        // TODO: 实现部门信息获取逻辑
        // 可以从数据库、缓存或其他服务获取
        log.debug("获取部门名称: deptId={}", deptId);
        return "部门" + deptId;
    }
}
