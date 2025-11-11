package com.example.gqlpcomposer.parser.impl;

import com.example.gqlpcomposer.exception.GqlpParseException;
import com.example.gqlpcomposer.model.*;
import com.example.gqlpcomposer.parser.GqlpParser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * GraphQL+ 解析器实现
 */
@Component
public class GqlpParserImpl implements GqlpParser {

    // 正则表达式模式
    private static final Pattern SERVICE_PATTERN = Pattern.compile(
            "service\\s+(\\w+(?:-\\w+)*)\\s*:\\s*\\{", Pattern.CASE_INSENSITIVE);

    private static final Pattern DATA_QUERY_PATTERN = Pattern.compile(
            "new\\s+(\\w+@\\w+)\\s*=\\s*gql\\s+(query|mutation)\\s*\\{([^}]+)\\}\\s*;",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern ASSIGN_PATTERN = Pattern.compile(
            "assign\\s+([^=]+)\\s*=\\s*([^;]+)\\s*;", Pattern.CASE_INSENSITIVE);

    private static final Pattern CALL_PATTERN = Pattern.compile(
            "call\\s+(\\w+(?:-\\w+)*)\\s*;", Pattern.CASE_INSENSITIVE);

    private static final Pattern PLUGIN_PATTERN = Pattern.compile(
            "new\\s+([^=]+)\\s*=\\s*plugin\\s+([^/]+)/([^(]+)\\(([^)]+)\\)\\s*;",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern RETURN_PATTERN = Pattern.compile(
            "return\\s+([^;]+)\\s*;", Pattern.CASE_INSENSITIVE);

    @Override
    public GqlpDocument parse(String fileName, String content) {
        if (StringUtils.isBlank(content)) {
            throw new GqlpParseException("文档内容为空", fileName, 0);
        }

        try {
            // 提取服务名
            String serviceName = extractServiceName(fileName, content);

            // 创建文档对象
            GqlpDocument document = new GqlpDocument();
            document.setServiceName(serviceName);
            document.setOriginalContent(content);

            // 解析语句
            List<Statement> statements = parseStatements(fileName, content);
            document.setStatements(statements);

            return document;

        } catch (Exception e) {
            if (e instanceof GqlpParseException) {
                throw e;
            }
            throw new GqlpParseException("解析文档失败: " + e.getMessage(), fileName, -1);
        }
    }

    /**
     * 提取服务名
     */
    private String extractServiceName(String fileName, String content) {
        Matcher matcher = SERVICE_PATTERN.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }

        // 如果没有找到服务声明，使用文件名
        return fileName;
    }

    /**
     * 解析语句列表
     */
    private List<Statement> parseStatements(String fileName, String content) {
        List<Statement> statements = new ArrayList<>();
        String[] lines = content.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            int lineNumber = i + 1;

            if (StringUtils.isBlank(line) || line.startsWith("//") ||
                    line.startsWith("service") || line.equals("}")) {
                continue;
            }

            try {
                Statement statement = parseSingleStatement(line, lineNumber);
                if (statement != null) {
                    statements.add(statement);
                }
            } catch (Exception e) {
                throw new GqlpParseException("解析语句失败: " + e.getMessage(), fileName, lineNumber);
            }
        }

        return statements;
    }

    /**
     * 解析单个语句
     */
    private Statement parseSingleStatement(String line, int lineNumber) {
        line = line.trim();

        // 数据查询语句
        Matcher dataQueryMatcher = DATA_QUERY_PATTERN.matcher(line);
        if (dataQueryMatcher.find()) {
            DataQueryStatement stmt = new DataQueryStatement();
            stmt.setRawText(line);
            stmt.setLineNumber(lineNumber);
            stmt.setVariable(dataQueryMatcher.group(1).trim());
            stmt.setQueryType(dataQueryMatcher.group(2).trim());
            stmt.setGqlBody(dataQueryMatcher.group(3).trim());
            return stmt;
        }

        // 赋值语句
        Matcher assignMatcher = ASSIGN_PATTERN.matcher(line);
        if (assignMatcher.find()) {
            AssignStatement stmt = new AssignStatement();
            stmt.setRawText(line);
            stmt.setLineNumber(lineNumber);
            stmt.setLeftVariable(assignMatcher.group(1).trim());
            stmt.setRightVariable(assignMatcher.group(2).trim());
            return stmt;
        }

        // 调用语句
        Matcher callMatcher = CALL_PATTERN.matcher(line);
        if (callMatcher.find()) {
            CallStatement stmt = new CallStatement();
            stmt.setRawText(line);
            stmt.setLineNumber(lineNumber);
            stmt.setServiceName(callMatcher.group(1).trim());
            return stmt;
        }

        // 插件调用语句
        Matcher pluginMatcher = PLUGIN_PATTERN.matcher(line);
        if (pluginMatcher.find()) {
            PluginCallStatement stmt = new PluginCallStatement();
            stmt.setRawText(line);
            stmt.setLineNumber(lineNumber);

            String outputVars = pluginMatcher.group(1).trim();
            stmt.setOutputVariables(parseVariableList(outputVars));

            stmt.setPluginFile(pluginMatcher.group(2).trim());
            stmt.setFunctionName(pluginMatcher.group(3).trim());

            String inputVars = pluginMatcher.group(4).trim();
            stmt.setInputVariables(parseVariableList(inputVars));

            return stmt;
        }

        // 返回语句
        Matcher returnMatcher = RETURN_PATTERN.matcher(line);
        if (returnMatcher.find()) {
            ReturnStatement stmt = new ReturnStatement();
            stmt.setRawText(line);
            stmt.setLineNumber(lineNumber);
            stmt.setReturnVariable(returnMatcher.group(1).trim());
            return stmt;
        }

        // 如果都不匹配，返回null或抛出异常
        if (!line.isEmpty()) {
            throw new GqlpParseException("无法识别的语句格式: " + line);
        }

        return null;
    }

    /**
     * 解析变量列表（逗号分隔）
     */
    private List<String> parseVariableList(String variableString) {
        if (StringUtils.isBlank(variableString)) {
            return new ArrayList<>();
        }

        return Arrays.stream(variableString.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .toList();
    }
}
