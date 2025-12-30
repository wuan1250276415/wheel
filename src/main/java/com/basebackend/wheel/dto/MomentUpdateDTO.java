package com.basebackend.wheel.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 更新动态请求 DTO
 */
@Data
public class MomentUpdateDTO {

    /**
     * 动态文本内容
     */
    @Size(max = 2000, message = "动态内容不能超过2000个字符")
    private String content;

    /**
     * 图片URL列表（最多9张）
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;

    /**
     * 可见性：0-仅情侣可见，1-公开
     */
    private Integer visibility;
}
