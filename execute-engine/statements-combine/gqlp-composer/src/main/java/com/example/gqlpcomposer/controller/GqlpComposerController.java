package com.example.gqlpcomposer.controller;

import com.example.gqlpcomposer.dto.*;
import com.example.gqlpcomposer.service.FileUploadService;
import com.example.gqlpcomposer.service.GqlpComposerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * GraphQL+ 组合控制器
 *
 * 提供RESTful API接口用于组合GraphQL+文档
 * 支持JSON请求体和文件上传两种方式
 */
@RestController
@RequestMapping("/v1/gqlp")
@Validated
@CrossOrigin(origins = "*", maxAge = 3600)
public class GqlpComposerController {

    private static final Logger logger = LoggerFactory.getLogger(GqlpComposerController.class);

    @Autowired
    private GqlpComposerService gqlpComposerService;

    @Autowired
    private FileUploadService fileUploadService;

    /**
     * 组合GraphQL+文档（JSON方式）
     *
     * @param request 包含多个.gqlp文件内容的组合请求
     * @return 组合后的单一GraphQL+文档
     */
    @PostMapping("/compose")
    public ResponseEntity<ComposeResponse> compose(@Valid @RequestBody ComposeRequest request) {
        logger.info("接收到JSON组合请求，文件数量: {}", request.getFiles().size());

        try {
            ComposeResponse response = gqlpComposerService.compose(request);

            if (response.isSuccess()) {
                logger.info("组合成功完成");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("组合失败: {}", response.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (Exception e) {
            logger.error("处理组合请求时发生错误", e);
            return buildErrorResponseEntity("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 组合GraphQL+文档（文件上传方式）
     *
     * @param files 上传的.gqlp文件数组
     * @param entryServiceName 可选的入口服务名称
     * @return 组合后的单一GraphQL+文档
     */
    @PostMapping(value = "/compose/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComposeResponse> composeWithUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "entryServiceName", required = false) String entryServiceName) {

        logger.info("接收到文件上传组合请求，文件数量: {}", files != null ? files.length : 0);

        try {
            // 1. 处理上传的文件
            List<GqlpFile> gqlpFiles = fileUploadService.processUploadedFiles(files);

            // 2. 构建组合请求
            ComposeRequest request = new ComposeRequest(gqlpFiles, entryServiceName);

            // 3. 执行组合
            ComposeResponse response = gqlpComposerService.compose(request);

            if (response.isSuccess()) {
                logger.info("文件上传组合成功完成");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("文件上传组合失败: {}", response.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            logger.error("文件上传参数错误: {}", e.getMessage());
            return buildErrorResponseEntity("参数错误: " + e.getMessage());

        } catch (Exception e) {
            logger.error("处理文件上传组合请求时发生错误", e);
            return buildErrorResponseEntity("服务器内部错误: " + e.getMessage());
        }
    }

    /**
     * 批量上传并组合GraphQL+文档（支持ZIP文件）
     *
     * @param zipFile 包含多个.gqlp文件的ZIP压缩包
     * @param entryServiceName 可选的入口服务名称
     * @return 组合后的单一GraphQL+文档
     */
    @PostMapping(value = "/compose/upload-zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComposeResponse> composeWithZipUpload(
            @RequestParam("zipFile") MultipartFile zipFile,
            @RequestParam(value = "entryServiceName", required = false) String entryServiceName) {

        logger.info("接收到ZIP文件上传组合请求，文件: {}", zipFile.getOriginalFilename());

        // 这个功能可以后续扩展实现
        return buildErrorResponseEntity("ZIP文件上传功能暂未实现，请使用单独文件上传方式");
    }

    /**
     * 获取支持的文件格式信息
     */
    @GetMapping("/upload/info")
    public ResponseEntity<Object> getUploadInfo() {
        return ResponseEntity.ok(new Object() {
            public final String[] supportedExtensions = {".gqlp", ".txt"};
            public final String maxFileSize = "10MB";
            public final String maxRequestSize = "50MB";
            public final int maxFiles = 20;
            public final String[] uploadEndpoints = {
                    "/v1/gqlp/compose/upload",
                    "/v1/gqlp/compose/upload-zip"
            };
        });
    }

    /**
     * 健康检查接口
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("GraphQL+ Composer Service is running");
    }

    /**
     * 获取服务信息
     */
    @GetMapping("/info")
    public ResponseEntity<Object> info() {
        return ResponseEntity.ok(new Object() {
            public final String service = "GraphQL+ Composer";
            public final String version = "1.0.0";
            public final String description = "基于iRUC论文实现的GraphQL+文档组合服务";
            public final String[] supportedOperations = {
                    "compose", "compose/upload", "compose/upload-zip",
                    "upload/info", "health", "info"
            };
            public final String[] inputMethods = {
                    "JSON请求体",
                    "文件上传",
                    "ZIP压缩包上传"
            };
        });
    }

    /**
     * 构建错误响应实体
     */
    private ResponseEntity<ComposeResponse> buildErrorResponseEntity(String errorMessage) {
        ComposeResponse errorResponse = ComposeResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 异常处理
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ComposeResponse> handleException(Exception e) {
        logger.error("未处理的异常", e);
        return buildErrorResponseEntity("处理请求时发生错误: " + e.getMessage());
    }
}
