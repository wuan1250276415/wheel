package com.basebackend.wheel.controller;

import com.basebackend.common.model.Result;
import com.basebackend.wheel.dto.FileUploadVO;
import com.basebackend.wheel.service.FileUploadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/upload")
@Tag(name = "文件上传管理", description = "图片上传相关接口")
public class FileUploadController {

    @Autowired
    private FileUploadService fileUploadService;

    @Operation(summary = "上传单个图片", description = "上传单个图片到服务器")
    @PostMapping("/image")
    public Result<FileUploadVO> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            FileUploadVO result = fileUploadService.uploadFile(file, "moments");
            log.info("图片上传成功: url={}", result.getUrl());
            return Result.success(result);
        } catch (Exception e) {
            log.error("图片上传失败: error={}", e.getMessage());
            throw e;
        }
    }

    @Operation(summary = "批量上传图片", description = "批量上传图片到服务器（最多9张）")
    @PostMapping("/images")
    public Result<List<FileUploadVO>> uploadImages(@RequestParam("files") List<MultipartFile> files) {
        try {
            List<FileUploadVO> results = fileUploadService.uploadFiles(files, "moments");
            log.info("批量上传图片成功: count={}", results.size());
            return Result.success(results);
        } catch (Exception e) {
            log.error("批量上传图片失败: error={}", e.getMessage());
            throw e;
        }
    }
}
