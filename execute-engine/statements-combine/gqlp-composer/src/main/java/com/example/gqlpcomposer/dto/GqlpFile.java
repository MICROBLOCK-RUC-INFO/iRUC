package com.example.gqlpcomposer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * GraphQL+ 文件数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GqlpFile {

    /**
     * 文件名（不包含扩展名）
     */
    @NotBlank(message = "文件名不能为空")
    private String fileName;

    /**
     * 文件内容
     */
    @NotBlank(message = "文件内容不能为空")
    private String content;

    /**
     * 服务名称（从文件名中提取）
     */
    public String getServiceName() {
        return fileName;
    }
}
