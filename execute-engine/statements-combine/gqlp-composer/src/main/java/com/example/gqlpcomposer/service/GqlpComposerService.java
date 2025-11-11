package com.example.gqlpcomposer.service;

import com.example.gqlpcomposer.dto.ComposeRequest;
import com.example.gqlpcomposer.dto.ComposeResponse;

/**
 * GraphQL+ 组合服务接口
 */
public interface GqlpComposerService {

    /**
     * 组合多个GraphQL+文件为单一文档
     *
     * @param request 组合请求
     * @return 组合响应
     */
    ComposeResponse compose(ComposeRequest request);
}
