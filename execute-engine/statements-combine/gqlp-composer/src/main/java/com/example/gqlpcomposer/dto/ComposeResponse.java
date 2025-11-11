package com.example.gqlpcomposer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 组合响应数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComposeResponse {

    /**
     * 组合后的GraphQL+文档内容
     */
    private String composedDocument;

    /**
     * 处理状态
     */
    private boolean success;

    /**
     * 错误信息（如果有）
     */
    private String errorMessage;

    /**
     * 处理详情
     */
    private String processingDetails;

    /**
     * 入口服务名称
     */
    private String entryServiceName;

    /**
     * 参与组合的服务数量
     */
    private int serviceCount;
}
