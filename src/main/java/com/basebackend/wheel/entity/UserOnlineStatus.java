package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户在线状态表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "user_online_status")
public class UserOnlineStatus extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 是否在线：0-离线，1-在线
     */
    @TableField(value = "is_online")
    private Integer isOnline;

    /**
     * 最后活跃时间
     */
    @TableField(value = "last_active_time")
    @NotNull(message = "最后活跃时间不能为空")
    private LocalDateTime lastActiveTime;

    /**
     * WebSocket会话ID
     */
    @TableField(value = "session_id")
    private String sessionId;

    /**
     * 设备类型
     */
    @TableField(value = "device_type")
    private String deviceType;
}
