package com.example.gqlpcomposer.service;

import com.example.gqlpcomposer.dto.GqlpFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {

    /**
     * 处理上传的文件并转换为GqlpFile列表
     *
     * @param files 上传的文件列表
     * @return GqlpFile列表
     */
    List<GqlpFile> processUploadedFiles(MultipartFile[] files);

    /**
     * 验证文件是否为有效的.gqlp文件
     *
     * @param file 上传的文件
     * @return 是否有效
     */
    boolean isValidGqlpFile(MultipartFile file);
}
