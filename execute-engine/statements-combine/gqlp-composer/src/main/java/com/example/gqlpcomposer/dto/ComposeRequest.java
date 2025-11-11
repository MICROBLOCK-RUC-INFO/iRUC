package com.example.gqlpcomposer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 组合请求数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComposeRequest {

    /**
     * 要组合的GraphQL+文件列表
     */
    @NotNull(message = "文件列表不能为null")
    @NotEmpty(message = "文件列表不能为空")
    @Valid
    private List<GqlpFile> files;

    /**
     * 入口服务名称（可选，如果不指定则自动检测）
     */
    private String entryServiceName;
}
