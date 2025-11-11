package com.example.gqlpcomposer.composer;

import com.example.gqlpcomposer.model.GqlpDocument;

import java.util.List;
import java.util.Map;

/**
 * 文档组合器接口
 */
public interface DocumentComposer {

    /**
     * 组合多个GraphQL+文档为单一文档
     *
     * @param documents 文档映射表 (serviceName -> document)
     * @param entryServiceName 入口服务名称
     * @return 组合后的文档内容
     */
    String compose(Map<String, GqlpDocument> documents, String entryServiceName);

    /**
     * 自动检测入口服务
     *
     * @param documents 文档映射表
     * @return 入口服务名称
     */
    String detectEntryService(Map<String, GqlpDocument> documents);
}
