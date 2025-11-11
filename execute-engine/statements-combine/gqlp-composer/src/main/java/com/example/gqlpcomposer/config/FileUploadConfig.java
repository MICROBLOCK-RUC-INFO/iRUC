package com.example.gqlpcomposer.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;

import jakarta.servlet.MultipartConfigElement;

/**
 * 文件上传配置类
 */
@Configuration
public class FileUploadConfig {

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // 设置文件大小限制
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        // 设置请求大小限制
        factory.setMaxRequestSize(DataSize.ofMegabytes(50));
        // 设置文件写入磁盘的阈值
        factory.setFileSizeThreshold(DataSize.ofKilobytes(2));
        // 设置上传文件的临时目录
        factory.setLocation(System.getProperty("java.io.tmpdir"));

        return factory.createMultipartConfig();
    }
}
