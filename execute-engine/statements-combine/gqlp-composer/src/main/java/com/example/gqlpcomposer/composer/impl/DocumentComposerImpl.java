package com.example.gqlpcomposer.composer.impl;

import com.example.gqlpcomposer.composer.DocumentComposer;
import com.example.gqlpcomposer.exception.CircularDependencyException;
import com.example.gqlpcomposer.exception.ServiceNotFoundException;
import com.example.gqlpcomposer.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档组合器实现
 * 根据论文中的Algorithm 1实现GraphQL+组合逻辑
 */
@Component
public class DocumentComposerImpl implements DocumentComposer {

    private static final Logger logger = LoggerFactory.getLogger(DocumentComposerImpl.class);

    @Override
    public String compose(Map<String, GqlpDocument> documents, String entryServiceName) {
        if (documents == null || documents.isEmpty()) {
            throw new IllegalArgumentException("文档映射表不能为空");
        }

        // 如果未指定入口服务，自动检测
        if (StringUtils.isBlank(entryServiceName)) {
            entryServiceName = detectEntryService(documents);
        }

        // 验证入口服务存在
        if (!documents.containsKey(entryServiceName)) {
            throw new ServiceNotFoundException(entryServiceName);
        }

        logger.info("开始组合文档，入口服务: {}", entryServiceName);

        // 执行组合算法
        List<Statement> composedStatements = composeStatements(documents, entryServiceName);

        // 生成最终文档
        return generateComposedDocument(composedStatements);
    }

    @Override
    public String detectEntryService(Map<String, GqlpDocument> documents) {
        // 统计每个服务被调用的次数
        Map<String, Integer> callCounts = new HashMap<>();

        // 初始化所有服务的调用计数为0
        for (String serviceName : documents.keySet()) {
            callCounts.put(serviceName, 0);
        }

        // 统计调用关系
        for (GqlpDocument document : documents.values()) {
            List<CallStatement> calls = document.getCallStatements();
            for (CallStatement call : calls) {
                String calledService = call.getServiceName();
                callCounts.put(calledService, callCounts.getOrDefault(calledService, 0) + 1);
            }
        }

        // 找到未被调用的服务作为入口服务
        List<String> entryServices = callCounts.entrySet().stream()
                .filter(entry -> entry.getValue() == 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (entryServices.isEmpty()) {
            throw new CircularDependencyException("检测到循环依赖，无法确定入口服务",
                    new ArrayList<>(documents.keySet()));
        }

        if (entryServices.size() > 1) {
            logger.warn("检测到多个可能的入口服务: {}，选择第一个: {}", entryServices, entryServices.get(0));
        }

        return entryServices.get(0);
    }

    /**
     * 执行组合算法（基于论文Algorithm 1）
     */
    private List<Statement> composeStatements(Map<String, GqlpDocument> documents, String entryServiceName) {
        List<Statement> result = new ArrayList<>();
        Set<String> processedServices = new HashSet<>();
        List<String> processingStack = new ArrayList<>();

        // 获取入口文档的语句
        GqlpDocument entryDocument = documents.get(entryServiceName);
        List<Statement> entryStatements = new ArrayList<>(entryDocument.getStatementsOnly());

        // 递归处理语句
        processStatementsRecursively(entryStatements, documents, result, processedServices, processingStack);

        return result;
    }

    /**
     * 递归处理语句列表
     */
    private void processStatementsRecursively(List<Statement> statements,
                                              Map<String, GqlpDocument> documents,
                                              List<Statement> result,
                                              Set<String> processedServices,
                                              List<String> processingStack) {

        for (Statement statement : statements) {
            if (statement instanceof CallStatement) {
                CallStatement callStmt = (CallStatement) statement;
                String calledService = callStmt.getServiceName();

                // 检查循环依赖
                if (processingStack.contains(calledService)) {
                    List<String> cycle = new ArrayList<>(processingStack);
                    cycle.add(calledService);
                    throw new CircularDependencyException("检测到循环依赖", cycle);
                }

                // 检查服务是否存在
                if (!documents.containsKey(calledService)) {
                    throw new ServiceNotFoundException(calledService);
                }

                // 如果服务还未处理过，则处理它
                if (!processedServices.contains(calledService)) {
                    processingStack.add(calledService);

                    GqlpDocument calledDocument = documents.get(calledService);
                    List<Statement> calledStatements = new ArrayList<>(calledDocument.getStatementsOnly());

                    // 移除被调用服务的返回语句
                    calledStatements.removeIf(stmt -> stmt instanceof ReturnStatement);

                    // 递归处理被调用服务的语句
                    processStatementsRecursively(calledStatements, documents, result, processedServices, processingStack);

                    processedServices.add(calledService);
                    processingStack.remove(calledService);
                }

                // 不添加call语句到结果中（这是关键的组合逻辑）
                logger.debug("跳过call语句: {}", statement.toGqlpText());

            } else {
                // 添加非call语句到结果中
                result.add(statement);
                logger.debug("添加语句: {}", statement.toGqlpText());
            }
        }
    }

    /**
     * 生成组合后的文档
     */
    private String generateComposedDocument(List<Statement> statements) {
        StringBuilder sb = new StringBuilder();
        sb.append("// 组合后的GraphQL+文档\n");
        sb.append("// 由iRUC系统自动生成\n\n");

        for (Statement statement : statements) {
            // 添加适当的缩进和格式
            String statementText = statement.toGqlpText();
            if (statement instanceof DataQueryStatement) {
                sb.append("//数据查询\n");
            } else if (statement instanceof AssignStatement) {
                sb.append("//赋值操作\n");
            } else if (statement instanceof PluginCallStatement) {
                sb.append("//插件调用\n");
            } else if (statement instanceof ReturnStatement) {
                sb.append("//返回语句\n");
            }

            sb.append(statementText).append("\n");
        }

        return sb.toString();
    }
}
