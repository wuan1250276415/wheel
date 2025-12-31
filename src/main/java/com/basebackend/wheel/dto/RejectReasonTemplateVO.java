package com.basebackend.wheel.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 拒绝原因模板VO
 *
 * @author wheel-api
 * @since 2025-02-01
 */
@Data
public class RejectReasonTemplateVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    private Integer id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 违规类型代码
     */
    private Integer violationType;

    /**
     * 违规类型名称
     */
    private String violationTypeName;
}
