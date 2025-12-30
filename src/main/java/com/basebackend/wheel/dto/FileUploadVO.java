package com.basebackend.wheel.dto;

import lombok.Data;

/**
 * 文件上传响应 DTO
 */
@Data
public class FileUploadVO {

    /**
     * 文件URL
     */
    private String url;

    /**
     * 文件大小（字节）
     */
    private Long size;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件类型
     */
    private String fileType;
}
