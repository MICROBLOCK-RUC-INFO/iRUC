package com.example.gqlpcomposer.service.impl;

import com.example.gqlpcomposer.composer.DocumentComposer;
import com.example.gqlpcomposer.dto.*;
import com.example.gqlpcomposer.exception.CircularDependencyException;
import com.example.gqlpcomposer.exception.GqlpParseException;
import com.example.gqlpcomposer.exception.ServiceNotFoundException;
import com.example.gqlpcomposer.model.GqlpDocument;
import com.example.gqlpcomposer.parser.GqlpParser;
import com.example.gqlpcomposer.service.GqlpComposerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * GraphQL+ 组合服务实现
 */
@Service
public class GqlpComposerServiceImpl implements GqlpComposerService {

    private static final Logger logger = LoggerFactory.getLogger(GqlpComposerServiceImpl.class);

    @Autowired
    private GqlpParser gqlpParser;

    @Autowired
    private DocumentComposer documentComposer;

    @Override
    public ComposeResponse compose(ComposeRequest request) {
        logger.info("开始处理组合请求，文件数量: {}", request.getFiles().size());

        try {
            // 1. 解析所有文件
            Map<String, GqlpDocument> documents = parseAllFiles(request.getFiles());

            // 2. 确定入口服务
            String entryServiceName = determineEntryService(request.getEntryServiceName(), documents);

            // 3. 执行组合
            String composedDocument = documentComposer.compose(documents, entryServiceName);

            // 4. 构建成功响应
            ComposeResponse response = ComposeResponse.builder()
                    .success(true)
                    .composedDocument(composedDocument)
                    .entryServiceName(entryServiceName)
                    .serviceCount(documents.size())
                    .processingDetails(String.format("成功组合%d个服务的GraphQL+文档，入口服务: %s",
                            documents.size(), entryServiceName))
                    .build();

            logger.info("组合完成，入口服务: {}, 总服务数: {}", entryServiceName, documents.size());
            return response;

        } catch (GqlpParseException e) {
            logger.error("解析错误: {}", e.getMessage(), e);
            return buildErrorResponse("解析错误: " + e.getMessage());

        } catch (CircularDependencyException e) {
            logger.error("循环依赖错误: {}", e.getMessage(), e);
            return buildErrorResponse("循环依赖错误: " + e.getMessage());

        } catch (ServiceNotFoundException e) {
            logger.error("服务未找到: {}", e.getMessage(), e);
            return buildErrorResponse("服务未找到: " + e.getMessage());

        } catch (Exception e) {
            logger.error("组合过程中发生未知错误", e);
            return buildErrorResponse("组合失败: " + e.getMessage());
        }
    }

    /**
     * 解析所有文件
     */
    private Map<String, GqlpDocument> parseAllFiles(java.util.List<GqlpFile> files) {
        Map<String, GqlpDocument> documents = new HashMap<>();

        for (GqlpFile file : files) {
            logger.debug("解析文件: {}", file.getFileName());

            try {
                GqlpDocument document = gqlpParser.parse(file.getFileName(), file.getContent());
                documents.put(document.getServiceName(), document);

                logger.debug("成功解析服务: {}", document.getServiceName());

            } catch (Exception e) {
                logger.error("解析文件 {} 失败", file.getFileName(), e);
                throw new GqlpParseException(
                        String.format("解析文件 %s 失败: %s", file.getFileName(), e.getMessage()), e);
            }
        }

        return documents;
    }

    /**
     * 确定入口服务
     */
    private String determineEntryService(String requestedEntryService, Map<String, GqlpDocument> documents) {
        if (requestedEntryService != null && !requestedEntryService.trim().isEmpty()) {
            // 验证指定的入口服务是否存在
            if (documents.containsKey(requestedEntryService)) {
                logger.info("使用指定的入口服务: {}", requestedEntryService);
                return requestedEntryService;
            } else {
                throw new ServiceNotFoundException(requestedEntryService);
            }
        } else {
            // 自动检测入口服务
            String detectedEntry = documentComposer.detectEntryService(documents);
            logger.info("自动检测到入口服务: {}", detectedEntry);
            return detectedEntry;
        }
    }

    /**
     * 构建错误响应
     */
    private ComposeResponse buildErrorResponse(String errorMessage) {
        return ComposeResponse.builder()
                .success(false)
                .errorMessage(errorMessage)
                .composedDocument(null)
                .serviceCount(0)
                .build();
    }
}
