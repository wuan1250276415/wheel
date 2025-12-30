package com.basebackend.wheel.service.impl;

import com.basebackend.common.exception.BusinessException;
import com.basebackend.wheel.dto.FileUploadVO;
import com.basebackend.wheel.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传服务实现（本地存储）
 * 生产环境建议替换为 OSS/COS
 */
@Slf4j
@Service
public class FileUploadServiceImpl implements FileUploadService {

    @Value("${file.upload.path:/data/upload}")
    private String uploadPath;

    @Value("${file.upload.base-url:http://localhost:8080}")
    private String baseUrl;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};

    @Override
    public FileUploadVO uploadFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        // 验证文件大小
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // 验证文件类型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !isAllowedExtension(originalFilename)) {
            throw new BusinessException("不支持的文件类型");
        }

        try {
            // 生成文件路径
            String dateFolder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String targetFolder = folder + File.separator + dateFolder;
            Path targetPath = Paths.get(uploadPath, targetFolder);

            // 创建目录
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
            }

            // 生成唯一文件名
            String extension = getFileExtension(originalFilename);
            String fileName = UUID.randomUUID().toString() + extension;
            Path filePath = targetPath.resolve(fileName);

            // 保存文件
            file.transferTo(filePath.toFile());

            // 构建返回对象
            FileUploadVO vo = new FileUploadVO();
            vo.setUrl(baseUrl + "/upload/" + targetFolder.replace(File.separator, "/") + "/" + fileName);
            vo.setSize(file.getSize());
            vo.setFileName(originalFilename);
            vo.setFileType(file.getContentType());

            log.info("文件上传成功: fileName={}, size={}, url={}", originalFilename, file.getSize(), vo.getUrl());
            return vo;
        } catch (IOException e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            throw new BusinessException("文件上传失败");
        }
    }

    @Override
    public List<FileUploadVO> uploadFiles(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException("文件列表不能为空");
        }

        if (files.size() > 9) {
            throw new BusinessException("最多上传9张图片");
        }

        List<FileUploadVO> results = new ArrayList<>();
        for (MultipartFile file : files) {
            results.add(uploadFile(file, folder));
        }
        return results;
    }

    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // 从URL中提取文件路径
            String filePath = fileUrl.replace(baseUrl + "/upload/", "");
            Path targetPath = Paths.get(uploadPath, filePath);

            if (Files.exists(targetPath)) {
                Files.delete(targetPath);
                log.info("文件删除成功: url={}", fileUrl);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("文件删除失败: url={}, error={}", fileUrl, e.getMessage());
            return false;
        }
    }

    private boolean isAllowedExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        return "";
    }
}
