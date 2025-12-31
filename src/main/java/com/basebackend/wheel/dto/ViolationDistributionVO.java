package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 违规类型分布VO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class ViolationDistributionVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 违规类型代码
     */
    private Integer violationType;

    /**
     * 违规类型名称
     */
    private String violationTypeName;

    /**
     * 数量
     */
    private Long count;

    /**
     * 占比（百分比）
     */
    private Double percentage;
}
