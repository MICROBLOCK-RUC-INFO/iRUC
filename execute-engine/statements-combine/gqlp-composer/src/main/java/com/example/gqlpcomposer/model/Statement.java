package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * GraphQL+ 语句基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Statement {

    /**
     * 语句类型
     */
    public enum StatementType {
        DATA_QUERY,     // 数据查询语句
        ASSIGN,         // 赋值语句
        SERVICE_CALL,   // 服务调用语句
        PLUGIN_CALL,    // 插件调用语句
        RETURN          // 返回语句
    }

    /**
     * 语句原始文本
     */
    private String rawText;

    /**
     * 行号
     */
    private int lineNumber;

    /**
     * 获取语句类型
     */
    public abstract StatementType getType();

    /**
     * 转换为GraphQL+文本
     */
    public abstract String toGqlpText();
}
