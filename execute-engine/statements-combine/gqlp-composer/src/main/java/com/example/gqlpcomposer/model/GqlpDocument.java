package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * GraphQL+ 文档模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GqlpDocument {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 语句列表
     */
    private List<Statement> statements = new ArrayList<>();

    /**
     * 原始文档内容
     */
    private String originalContent;

    /**
     * 获取所有服务调用语句
     */
    public List<ServiceCallStatement> getServiceCalls() {
        return statements.stream()
                .filter(stmt -> stmt instanceof ServiceCallStatement)
                .map(stmt -> (ServiceCallStatement) stmt)
                .collect(Collectors.toList());
    }

    /**
     * 获取所有调用语句
     */
    public List<CallStatement> getCallStatements() {
        return statements.stream()
                .filter(stmt -> stmt instanceof CallStatement)
                .map(stmt -> (CallStatement) stmt)
                .collect(Collectors.toList());
    }

    /**
     * 检查是否调用了指定服务
     */
    public boolean callsService(String serviceName) {
        return getCallStatements().stream()
                .anyMatch(call -> serviceName.equals(call.getServiceName()));
    }

    /**
     * 转换为GraphQL+文本
     */
    public String toGqlpText() {
        StringBuilder sb = new StringBuilder();
        sb.append("service ").append(serviceName).append(":{\n");

        for (Statement statement : statements) {
            sb.append("    ").append(statement.toGqlpText()).append("\n");
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * 添加语句
     */
    public void addStatement(Statement statement) {
        if (statements == null) {
            statements = new ArrayList<>();
        }
        statements.add(statement);
    }

    /**
     * 移除指定类型的语句
     */
    public void removeStatements(Statement.StatementType type) {
        statements.removeIf(stmt -> stmt.getType() == type);
    }

    /**
     * 移除返回语句
     */
    public void removeReturnStatements() {
        removeStatements(Statement.StatementType.RETURN);
    }

    /**
     * 获取语句内容（不包含service声明）
     */
    public List<Statement> getStatementsOnly() {
        return new ArrayList<>(statements);
    }
}
