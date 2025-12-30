package com.basebackend.wheel.service;

import com.basebackend.wheel.dto.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 上传单个文件
     *
     * @param file 文件
     * @param folder 文件夹路径
     * @return 文件URL
     */
    FileUploadVO uploadFile(MultipartFile file, String folder);

    /**
     * 批量上传文件
     *
     * @param files 文件列表
     * @param folder 文件夹路径
     * @return 文件URL列表
     */
    List<FileUploadVO> uploadFiles(List<MultipartFile> files, String folder);

    /**
     * 删除文件
     *
     * @param fileUrl 文件URL
     * @return 是否成功
     */
    boolean deleteFile(String fileUrl);
}
