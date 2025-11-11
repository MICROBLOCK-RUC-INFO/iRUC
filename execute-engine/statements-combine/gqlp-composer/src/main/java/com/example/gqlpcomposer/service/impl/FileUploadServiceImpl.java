package com.example.gqlpcomposer.service.impl;

import com.example.gqlpcomposer.dto.GqlpFile;
import com.example.gqlpcomposer.service.FileUploadService;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${app.upload.allowed-extensions:.gqlp,.txt}")
    private String[] allowedExtensions;

    @Value("${app.upload.max-files:20}")
    private int maxFiles;

    @Override
    public List<GqlpFile> processUploadedFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            throw new IllegalArgumentException("没有上传任何文件");
        }

        if (files.length > maxFiles) {
            throw new IllegalArgumentException("上传文件数量超过限制，最多允许" + maxFiles + "个文件");
        }

        List<GqlpFile> gqlpFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                logger.warn("跳过空文件: {}", file.getOriginalFilename());
                continue;
            }

            if (!isValidGqlpFile(file)) {
                throw new IllegalArgumentException("无效的文件: " + file.getOriginalFilename());
            }

            try {
                String content = new String(file.getBytes(), StandardCharsets.UTF_8);
                String fileName = getFileNameWithoutExtension(file.getOriginalFilename());

                GqlpFile gqlpFile = new GqlpFile(fileName, content);
                gqlpFiles.add(gqlpFile);

                logger.info("成功处理文件: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());

            } catch (IOException e) {
                logger.error("读取文件内容失败: {}", file.getOriginalFilename(), e);
                throw new RuntimeException("读取文件失败: " + file.getOriginalFilename(), e);
            }
        }

        return gqlpFiles;
    }

    @Override
    public boolean isValidGqlpFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            return false;
        }

        // 检查文件扩展名
        String extension = "." + FilenameUtils.getExtension(originalFilename);
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }

        logger.warn("不支持的文件扩展名: {}, 支持的扩展名: {}", extension, String.join(", ", allowedExtensions));
        return false;
    }

    /**
     * 获取不带扩展名的文件名
     */
    private String getFileNameWithoutExtension(String originalFilename) {
        if (originalFilename == null) {
            return "unknown";
        }
        return FilenameUtils.removeExtension(originalFilename);
    }
}
