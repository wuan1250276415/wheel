package com.basebackend.wheel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.basebackend.database.entity.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 转盘记录表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "wheel_api.wheel_spin_record")
public class WheelSpinRecord extends BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableField(value = "user_id")
    @NotNull(message = "用户ID不能为null")
    private Long userId;

    /**
     * 内容ID
     */
    @TableField(value = "content_id")
    @NotNull(message = "内容ID不能为null")
    private Long contentId;

    /**
     * 结果文本
     */
    @TableField(value = "result_text")
    @Size(max = 200, message = "结果文本最大长度要小于 200")
    @NotBlank(message = "结果文本不能为空")
    private String resultText;

    /**
     * 分类ID
     */
    @TableField(value = "category_id")
    @NotNull(message = "分类ID不能为null")
    private Long categoryId;

    /**
     * 旋转时间（毫秒）
     */
    @TableField(value = "spin_duration")
    private Long spinDuration;

    /**
     * 旋转时间
     */
    @TableField(value = "spin_time")
    @NotNull(message = "旋转时间不能为null")
    private LocalDateTime spinTime;

    /**
     * IP地址
     */
    @TableField(value = "ip_address")
    @Size(max = 45, message = "IP地址最大长度要小于 45")
    private String ipAddress;

    /**
     * 设备ID
     */
    @TableField(value = "device_id")
    @Size(max = 100, message = "设备ID最大长度要小于 100")
    private String deviceId;

    /**
     * 客户端类型：0-未知，1-微信小程序，2-H5，3-APP
     */
    @TableField(value = "client_type")
    private int clientType;

    /**
     * 是否异常：0-否，1-是
     */
    @TableField(value = "is_anomaly")
    private int isAnomaly;

}