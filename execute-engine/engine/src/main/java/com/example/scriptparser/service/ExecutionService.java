package com.example.scriptparser.service;

import com.example.api.HandlerService;
import com.example.scriptparser.ast.*;
import com.example.scriptparser.interpreter.Interpreter;
import com.example.scriptparser.model.ExecutionContext;
import com.example.scriptparser.parser.GraphQLPlusParser;
import com.example.scriptparser.util.JsonNodeUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.parboiled.errors.ErrorUtils;
import org.pf4j.PluginManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ExecutionService {

    @Autowired
    private ScriptService scriptService;

    @Autowired
    private PluginManager pluginManager;

    @Autowired
    private RestTemplate restTemplate;

    private static final String GRAPHQL_ENDPOINT = "http://192.168.0.204:4000/graphql";
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9_.]+)\\}");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonNode executeScript(String scriptName, Map<String, String> initParams) throws Exception {
        String script = scriptService.getGqlpScript(scriptName);
        if (script == null) {
            throw new Exception("Script " + scriptName + " not found.");
        }

        // 使用parboiled解析器
        GraphQLPlusParser parser = Parboiled.createParser(GraphQLPlusParser.class);
        ReportingParseRunner<Object> runner = new ReportingParseRunner<>(parser.Script());
        ParsingResult<Object> result = runner.run(script);

        if (result.hasErrors()) {
            throw new Exception("Parse errors: " + ErrorUtils.printParseErrors(result));
        }

        Object parseResultValue = result.resultValue;
        if (!(parseResultValue instanceof List)) {
            throw new Exception("Parsing failed: expected List<Statement>");
        }

        @SuppressWarnings("unchecked")
        List<Statement> statements = (List<Statement>) parseResultValue;

        // 创建执行上下文并设置初始参数
        ExecutionContext context = new ExecutionContext();
        for (Map.Entry<String, String> entry : initParams.entrySet()) {
            context.setVariable(entry.getKey(), JsonNodeUtil.createValueNode(entry.getValue()));
        }

        // 执行脚本
        ScriptExecutor executor = new ScriptExecutor();
        return executor.execute(statements, context);
    }

    private class ScriptExecutor {
        private String finalOutputKey = null;
        private long totalGqlQueryDuration = 0;
        private int gqlNum = 0;
        private int pluginNum = 0;

        public JsonNode execute(List<Statement> statements, ExecutionContext context) throws Exception {
            for (Statement stmt : statements) {
                executeStatement(stmt, context);
            }

            System.out.println("总gql查询时间: " + totalGqlQueryDuration + " 毫秒");

            if (finalOutputKey == null || context.getVariable(finalOutputKey) == null) {
                throw new Exception("Final output variable not set.");
            }

            Object finalOutput = context.getVariable(finalOutputKey);
            if (finalOutput instanceof JsonNode) {
                return (JsonNode) finalOutput;
            } else {
                return objectMapper.valueToTree(finalOutput);
            }
        }

        private void executeStatement(Statement stmt, ExecutionContext context) throws Exception {
            if (stmt instanceof GqlQueryStatement) {
                executeGqlQuery((GqlQueryStatement) stmt, context);
            } else if (stmt instanceof DataAlignStatement) {
                executeDataAlign((DataAlignStatement) stmt, context);
            } else if (stmt instanceof PluginCallStatement) {
                executePluginCall((PluginCallStatement) stmt, context);
            } else if (stmt instanceof DefStatement) {
                executeDef((DefStatement) stmt, context);
            } else if (stmt instanceof SetStatement) {
                executeSet((SetStatement) stmt, context);
            } else if (stmt instanceof IntDefStatement) {
                executeIntDef((IntDefStatement) stmt, context);
            } else if (stmt instanceof IntCalStatement) {
                executeIntCal((IntCalStatement) stmt, context);
            } else if (stmt instanceof IfStatement) {
                executeIf((IfStatement) stmt, context);
            } else if (stmt instanceof WhileStatement) {
                executeWhile((WhileStatement) stmt, context);
            } else if (stmt instanceof Block) {
                Block block = (Block) stmt;
                for (Statement blockStmt : block.getStatements()) {
                    executeStatement(blockStmt, context);
                }
            } else {
                throw new Exception("Unknown statement type: " + stmt.getClass().getName());
            }
        }

        private void executeGqlQuery(GqlQueryStatement stmt, ExecutionContext context) throws Exception {
            gqlNum++;
            long startTime = System.currentTimeMillis();

            String queryKey = extractQueryKey(stmt.getQueryBody());
            String queryTemplate = scriptService.getGqlpkQuery(queryKey);
            if (queryTemplate == null) {
                throw new Exception("GraphQL Key " + queryKey + " not found.");
            }

            // 替换变量
            String executableQuery = replaceVariables(queryTemplate, context);

            // 发送请求到 Apollo Server
            JsonNode response = sendGraphQLQuery(executableQuery);

            context.setVariable(stmt.getVariableName(), response.get("data"));
            long endTime = System.currentTimeMillis();
            totalGqlQueryDuration += (endTime - startTime);
        }

        private void executeDataAlign(DataAlignStatement stmt, ExecutionContext context) throws Exception {
            Object sourceValue = context.getVariable(stmt.getSourceVariable());
            if (sourceValue == null) {
                throw new Exception("Source variable " + stmt.getSourceVariable() + " not found.");
            }
            context.setVariable(stmt.getOutputVariable(), sourceValue);
            finalOutputKey = stmt.getOutputVariable();
        }

        private void executePluginCall(PluginCallStatement stmt, ExecutionContext context) throws Exception {
            pluginNum++;
            long startTime = System.currentTimeMillis();

            // 准备参数
            Map<String, Object> input = new HashMap<>();
            for (String param : stmt.getArguments()) {
                Object value = context.getVariable(param);
                if (value == null) {
                    throw new Exception("Variable not found for parameter: " + param);
                }
                input.put(param, value);
            }

            JsonNode inputNode = objectMapper.valueToTree(input);

            // 查找并调用插件
            List<HandlerService> extensions = pluginManager.getExtensions(HandlerService.class);
            Optional<HandlerService> targetExtensionOpt = extensions.stream()
                    .filter(extension -> stmt.getFunctionName().equals(extension.getName()))
                    .findFirst();

            if (targetExtensionOpt.isEmpty()) {
                throw new Exception("插件 \"" + stmt.getFunctionName() + "\" 未找到或未加载。");
            }

            HandlerService handlerService = targetExtensionOpt.get();
            JsonNode result = handlerService.handle(inputNode);

            // 插件返回一个kv对，取第一个
            Iterator<String> fieldNames = result.fieldNames();
            if (fieldNames.hasNext()) {
                String key = fieldNames.next();
                JsonNode valueNode = result.get(key);
                context.setVariable(stmt.getVariableName(), valueNode);
            } else {
                throw new Exception("插件返回结果为空。");
            }

            long endTime = System.currentTimeMillis();
        }

        private void executeDef(DefStatement stmt, ExecutionContext context) {
            Object value = stmt.getValue().getValue();
            JsonNode jsonValue = JsonNodeUtil.createValueNode(value);
            context.setVariable(stmt.getVariableName(), jsonValue);
        }

        private void executeSet(SetStatement stmt, ExecutionContext context) {
            Object value = stmt.getValue().getValue();
            JsonNode jsonValue = JsonNodeUtil.createValueNode(value);
            context.setVariable(stmt.getVariableName(), jsonValue);
        }

        private void executeIntDef(IntDefStatement stmt, ExecutionContext context) {
            context.setVariable(stmt.getVariableName(), stmt.getValue());
        }

        private void executeIntCal(IntCalStatement stmt, ExecutionContext context) throws Exception {
            Object sourceValueObj = context.getVariable(stmt.getSourceVariable());
            if (!(sourceValueObj instanceof Integer)) {
                throw new Exception("Variable " + stmt.getSourceVariable() + " is not an integer.");
            }

            int sourceValue = (Integer) sourceValueObj;
            int result;
            switch (stmt.getOperator()) {
                case "+":
                    result = sourceValue + stmt.getOperand();
                    break;
                case "-":
                    result = sourceValue - stmt.getOperand();
                    break;
                default:
                    throw new Exception("Unsupported operator: " + stmt.getOperator());
            }

            context.setVariable(stmt.getTargetVariable(), result);
        }

        private void executeIf(IfStatement stmt, ExecutionContext context) throws Exception {
            boolean condition = evaluateCondition(stmt.getConditionVariable(), context);
            if (condition) {
                for (Statement blockStmt : stmt.getTrueBlock().getStatements()) {
                    executeStatement(blockStmt, context);
                }
            }
        }

        private void executeWhile(WhileStatement stmt, ExecutionContext context) throws Exception {
            int maxIterations = 1000;
            int iteration = 0;

            while (evaluateCondition(stmt.getConditionVariable(), context)) {
                iteration++;
                if (iteration > maxIterations) {
                    throw new Exception("While loop exceeded max iterations");
                }

                for (Statement blockStmt : stmt.getLoopBlock().getStatements()) {
                    executeStatement(blockStmt, context);
                }
            }
        }

        private boolean evaluateCondition(String variableName, ExecutionContext context) throws Exception {
            Object conditionValue = context.getVariable(variableName);
            if (conditionValue == null) {
                throw new Exception("Condition variable " + variableName + " not found.");
            }

            if (conditionValue instanceof JsonNode) {
                JsonNode jsonNode = (JsonNode) conditionValue;
                if (jsonNode.isBoolean()) {
                    return jsonNode.asBoolean();
                }
            } else if (conditionValue instanceof Integer) {
                Integer intValue = (Integer) conditionValue;
                if (intValue < 0) {
                    throw new Exception("Integer condition cannot be negative: " + intValue);
                }
                return intValue > 0;
            }

            throw new Exception("Invalid condition type for variable: " + variableName);
        }

        private String extractQueryKey(String queryBody) {
            // 从 {getTripByTripId} 中提取 getTripByTripId
            String trimmed = queryBody.trim();
            if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
                return trimmed.substring(1, trimmed.length() - 1).trim();
            }
            return trimmed;
        }
    }

    private String replaceVariables(String template, ExecutionContext context) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = context.getVariable(key);
            String replacement;

            if (value != null) {
                if (value instanceof String) {
                    replacement = "\"" + escapeQuotes((String) value) + "\"";
                } else {
                    try {
                        replacement = objectMapper.writeValueAsString(value);
                    } catch (JsonProcessingException e) {
                        replacement = value.toString();
                    }
                }
            } else {
                replacement = matcher.group(0);
            }

            replacement = Matcher.quoteReplacement(replacement);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String escapeQuotes(String input) {
        return input.replace("\"", "\\\"");
    }

    private JsonNode sendGraphQLQuery(String gqlRequest) throws IOException {
        ObjectNode queryObject = objectMapper.createObjectNode();
        queryObject.put("query", gqlRequest);

        String requestBody = objectMapper.writeValueAsString(queryObject);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(
                    GRAPHQL_ENDPOINT,
                    requestEntity,
                    String.class
            );

            if (responseEntity.getStatusCode() != HttpStatus.OK) {
                throw new IOException("GraphQL query failed with status code: " + responseEntity.getStatusCode());
            }

            return objectMapper.readTree(responseEntity.getBody());

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            throw new IOException("GraphQL query failed with error: " + e.getStatusCode() + " " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new IOException("An error occurred while sending the GraphQL query", e);
        }
    }
}
