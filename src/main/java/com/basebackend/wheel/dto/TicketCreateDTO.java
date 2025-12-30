package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 工单创建DTO
 *
 * @author wheel-api
 * @since 2025-01-29
 */
@Data
public class TicketCreateDTO {
    /**
     * 工单标题
     */
    private String title;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 问题分类
     */
    private String category;
}
