# com.example.scriptparser (PEG版执行引擎) 完整说明文档

## 项目概述

com.example.scriptparser 是一个基于PEG（Parsing Expression Grammar）的脚本解析与执行引擎，专门用于处理GraphQL Plus（GQLP）脚本语言。该引擎支持GraphQL查询、数据对齐、插件调用、条件控制、循环控制等功能，并通过PF4J插件框架实现可扩展性。

## 核心架构

### 1. 更新入口机制

#### 1.1 文件处理流程

项目的入口文件处理由 `UpdateController` 负责，支持三种类型的文件：

```java
@PostMapping("/update")
public ResponseEntity<String> update(
        @RequestParam("gqlp") MultipartFile[] gqlpFiles,
        @RequestParam("gqlpk") MultipartFile[] gqlpkFiles,
        @RequestParam(value = "jar", required = false) MultipartFile[] jarFiles
) {
    // 处理逻辑
}
```

**GQLP文件处理**：
- GQLP文件包含脚本的主要逻辑，包括变量定义、GraphQL查询调用、控制流程等
- 文件内容被读取后存储在 `ScriptService` 的内存映射中
- 文件名（去除.gqlp扩展名）作为脚本的唯一标识符

**GQLPK文件处理**：
- GQLPK文件包含实际的GraphQL查询模板
- 支持与GQLP文件合并，使用 `----` 分隔符区分不同部分
- 使用正则表达式解析键值对格式：`[key]:[graphql_query]`

```java
Pattern regex = Pattern.compile("\\[([^\\]]+)\\]:\\[\\s*([\\s\\S]*?)\\s*\\]");
Matcher matcher = regex.matcher(content);
while (matcher.find()) {
    String key = matcher.group(1).trim();
    String value = matcher.group(2).trim();
    if (!key.isEmpty() && !value.isEmpty()) {
        newGqlpkQueries.put(key, value);
    }
}
```

#### 1.2 ScriptService存储机制

`ScriptService` 使用两个ConcurrentHashMap分别存储脚本内容和GraphQL查询：

```java
@Service
public class ScriptService {
    private final Map<String, String> gqlpScripts = new ConcurrentHashMap<>();
    private final Map<String, String> gqlpkQueries = new ConcurrentHashMap<>();
  
    public void updateGqlpScripts(Map<String, String> newScripts) {
        gqlpScripts.putAll(newScripts);
    }
  
    public void updateGqlpkQueries(Map<String, String> newQueries) {
        gqlpkQueries.putAll(newQueries);
    }
}
```

这种设计确保了：
- 线程安全的脚本存储
- 高效的查询性能
- 动态更新能力

### 2. 执行接口详解

#### 2.1 Parser解析机制

项目使用Parboiled框架实现PEG解析器，`GraphQLPlusParser` 是核心解析器类：

```java
@BuildParseTree
public class GraphQLPlusParser extends BaseParser<Object> {
  
    public Rule Script() {
        return Sequence(
                push(new ArrayList<Statement>()),
                Spacing(),
                ZeroOrMore(
                        Statement(),
                        ACTION(addToList())
                ),
                Spacing(),
                EOI
        );
    }
}
```

**解析规则层次结构**：

1. **Script规则**：脚本的根规则，创建Statement列表并逐个解析语句
2. **Statement规则**：识别不同类型的语句（GQL查询、数据对齐、插件调用等）
3. **具体语句规则**：每种语句类型都有对应的解析规则

**以GQL查询语句为例**：

```java
Rule GqlQueryStatement() {
    StringVar varName = new StringVar();
    StringVar queryBody = new StringVar();

    return Sequence(
            String("new"), Spacing(),
            RawIdentifier(), varName.set(match()), Spacing(),
            Ch('='), Spacing(),
            String("gql"), Spacing(), String("query"), Spacing(),
            GraphQLQueryBody(), queryBody.set(match()),
            push(new GqlQueryStatement(varName.get(), queryBody.get()))
    );
}
```

这个规则解析形如 `new trip123 = gql query {getTripByTripId}` 的语句：
- 匹配 "new" 关键字
- 捕获变量名 "trip123"
- 匹配 "gql query" 关键字序列
- 解析GraphQL查询体 "{getTripByTripId}"
- 创建 `GqlQueryStatement` AST节点并推入栈中

#### 2.2 Service执行机制

`ExecutionService` 负责整个脚本的执行流程：

```java
public JsonNode executeScript(String scriptName, Map<String, String> initParams) throws Exception {
    // 1. 获取脚本内容
    String script = scriptService.getGqlpScript(scriptName);
  
    // 2. 解析脚本
    GraphQLPlusParser parser = Parboiled.createParser(GraphQLPlusParser.class);
    ReportingParseRunner<Object> runner = new ReportingParseRunner<>(parser.Script());
    ParsingResult<Object> result = runner.run(script);
  
    // 3. 创建执行上下文
    ExecutionContext context = new ExecutionContext();
    for (Map.Entry<String, String> entry : initParams.entrySet()) {
        context.setVariable(entry.getKey(), JsonNodeUtil.createValueNode(entry.getValue()));
    }
  
    // 4. 执行脚本
    ScriptExecutor executor = new ScriptExecutor();
    return executor.execute(statements, context);
}
```

**执行上下文（ExecutionContext）**：

```java
public class ExecutionContext {
    private final Map<String, Object> variables = new HashMap<>();
  
    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }
  
    public Object getVariable(String name) {
        return variables.get(name);
    }
}
```

执行上下文维护了脚本执行过程中的所有变量状态，支持：
- 变量的动态设置和获取
- 类型安全的变量存储
- 执行状态的完整追踪

#### 2.3 语句执行分发机制

`ScriptExecutor` 使用策略模式分发不同类型的语句：

```java
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
    }
}
```

### 3. 具体语句类型执行详解

#### 3.1 GraphQL查询语句执行

```java
private void executeGqlQuery(GqlQueryStatement stmt, ExecutionContext context) throws Exception {
    // 1. 提取查询键
    String queryKey = extractQueryKey(stmt.getQueryBody());
  
    // 2. 获取查询模板
    String queryTemplate = scriptService.getGqlpkQuery(queryKey);
  
    // 3. 变量替换
    String executableQuery = replaceVariables(queryTemplate, context);
  
    // 4. 发送GraphQL请求
    JsonNode response = sendGraphQLQuery(executableQuery);
  
    // 5. 存储结果
    context.setVariable(stmt.getVariableName(), response.get("data"));
}
```

**变量替换机制**：
```java
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
                replacement = objectMapper.writeValueAsString(value);
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
```

使用正则表达式 `\\$\\{([a-zA-Z0-9_.]+)\\}` 匹配模板中的变量占位符，并从执行上下文中获取相应的值进行替换。

#### 3.2 控制流语句执行

**If语句执行**：
```java
private void executeIf(IfStatement stmt, ExecutionContext context) throws Exception {
    boolean condition = evaluateCondition(stmt.getConditionVariable(), context);
    if (condition) {
        for (Statement blockStmt : stmt.getTrueBlock().getStatements()) {
            executeStatement(blockStmt, context);
        }
    }
}
```

**While语句执行**：
```java
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
```

**条件求值机制**：
```java
private boolean evaluateCondition(String variableName, ExecutionContext context) throws Exception {
    Object conditionValue = context.getVariable(variableName);
  
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
```

条件求值支持：
- 布尔值直接求值
- 整数值：负数报错，0为false，正数为true
- 类型检查和错误处理

### 4. 示例脚本执行流程分析

以给定的示例脚本为例：

```gqlp
new trip123 = gql query {getTripByTripId};
output travel2.trip = trip123;
def train123456 = true;
if(train123456){
    new trainTypeName123_if1 = train0.so/trainapi1(travel2.trip_if1);
    new trainType123_if1 = gql query {getTrainTypeByName_if1};
    output train.trainType_if1 = trainType123_if1;
};
```

**执行流程**：

1. **解析阶段**：
   - `GraphQLPlusParser.Script()` 规则开始解析
   - 识别第一个语句为 `GqlQueryStatement`
   - 创建 AST 节点：`GqlQueryStatement(variableName="trip123", queryBody="{getTripByTripId}")`

2. **执行阶段**：
   - `executeGqlQuery()` 方法被调用
   - 从 `{getTripByTripId}` 提取查询键 `getTripByTripId`
   - 从 `ScriptService` 获取对应的GraphQL查询模板
   - 替换模板中的变量占位符
   - 发送HTTP请求到GraphQL服务器
   - 将响应数据存储到执行上下文的 `trip123` 变量中

3. **数据对齐**：
   - `executeDataAlign()` 执行 `output travel2.trip = trip123`
   - 从上下文获取 `trip123` 的值
   - 设置到 `travel2.trip` 变量中
   - 标记为最终输出变量

4. **条件执行**：
   - `executeDef()` 设置 `train123456 = true`
   - `executeIf()` 求值条件变量 `train123456`
   - 条件为真，执行if块中的语句

### 5. 添加新语句类型的技术细节

#### 5.1 AST节点定义

首先需要在 `com.example.scriptparser.ast` 包中创建新的AST节点类：

```java
public class NewStatementType implements Statement {
    private final String parameter1;
    private final String parameter2;
  
    public NewStatementType(String parameter1, String parameter2) {
        this.parameter1 = parameter1;
        this.parameter2 = parameter2;
    }
  
    // getter方法
    public String getParameter1() { return parameter1; }
    public String getParameter2() { return parameter2; }
  
    @Override
    public String toString() {
        return "NewStatementType{param1='" + parameter1 + "', param2='" + parameter2 + "'}";
    }
}
```

#### 5.2 解析规则添加

在 `GraphQLPlusParser` 中添加新的解析规则：

```java
Rule NewStatementRule() {
    StringVar param1 = new StringVar();
    StringVar param2 = new StringVar();
  
    return Sequence(
            String("newkeyword"), Spacing(),
            RawIdentifier(), param1.set(match()), Spacing(),
            String("with"), Spacing(),
            RawIdentifier(), param2.set(match()),
            push(new NewStatementType(param1.get(), param2.get()))
    );
}
```

在 `Statement()` 规则中添加新的选择项：

```java
Rule Statement() {
    return Sequence(
            FirstOf(
                    GqlQueryStatement(),
                    DataAlignStatement(),
                    PluginCallStatement(),
                    // ... 其他语句类型
                    NewStatementRule()  // 添加新的语句类型
            ),
            OptionalSemicolon()
    );
}
```

#### 5.3 执行逻辑实现

在 `ScriptExecutor` 的 `executeStatement()` 方法中添加分发逻辑：

```java
private void executeStatement(Statement stmt, ExecutionContext context) throws Exception {
    if (stmt instanceof GqlQueryStatement) {
        executeGqlQuery((GqlQueryStatement) stmt, context);
    } else if (stmt instanceof NewStatementType) {
        executeNewStatement((NewStatementType) stmt, context);
    }
    // ... 其他语句类型
}

private void executeNewStatement(NewStatementType stmt, ExecutionContext context) throws Exception {
    // 实现具体的执行逻辑
    String param1 = stmt.getParameter1();
    String param2 = stmt.getParameter2();
  
    // 执行相关操作
    // 更新执行上下文
    context.setVariable("result", "execution result");
}
```

### 6. 插件管理系统详解

#### 6.1 PF4J框架集成

项目使用PF4J框架实现插件管理，配置类如下：

```java
@Configuration
public class PluginManagerConfig {
  
    @Value("${plugin.directory:plugins}")
    private String pluginsDir;
  
    @Bean
    public PluginManager pluginManager() {
        Path pluginsPath = Paths.get(pluginsDir);
        DefaultPluginManager pluginManager = new DefaultPluginManager(pluginsPath);
      
        // 加载插件
        pluginManager.loadPlugins();
        // 启动插件
        pluginManager.startPlugins();
      
        // 日志输出
        pluginManager.getPlugins().forEach(plugin -> {
            System.out.println("Loaded plugin: " + plugin.getDescriptor().getPluginId());
        });
      
        return pluginManager;
    }
}
```

**PF4J工作原理**：
- `DefaultPluginManager` 扫描指定目录下的JAR文件
- 每个JAR文件包含插件描述符和实现类
- 通过Java的ServiceLoader机制发现和加载插件
- 插件生命周期管理：加载→启动→运行→停止→卸载

#### 6.2 插件调用机制

插件调用语句的执行逻辑：

```java
private void executePluginCall(PluginCallStatement stmt, ExecutionContext context) throws Exception {
    // 1. 准备参数
    Map<String, Object> input = new HashMap<>();
    for (String param : stmt.getArguments()) {
        Object value = context.getVariable(param);
        if (value == null) {
            throw new Exception("Variable not found for parameter: " + param);
        }
        input.put(param, value);
    }
  
    // 2. 转换为JsonNode
    JsonNode inputNode = objectMapper.valueToTree(input);
  
    // 3. 查找插件
    List<HandlerService> extensions = pluginManager.getExtensions(HandlerService.class);
    Optional<HandlerService> targetExtensionOpt = extensions.stream()
            .filter(extension -> stmt.getFunctionName().equals(extension.getName()))
            .findFirst();
  
    if (targetExtensionOpt.isEmpty()) {
        throw new Exception("插件 \"" + stmt.getFunctionName() + "\" 未找到或未加载。");
    }
  
    // 4. 调用插件
    HandlerService handlerService = targetExtensionOpt.get();
    JsonNode result = handlerService.handle(inputNode);
  
    // 5. 处理返回结果
    Iterator<String> fieldNames = result.fieldNames();
    if (fieldNames.hasNext()) {
        String key = fieldNames.next();
        JsonNode valueNode = result.get(key);
        context.setVariable(stmt.getVariableName(), valueNode);
    } else {
        throw new Exception("插件返回结果为空。");
    }
}
```

**插件接口定义**：
```java
public interface HandlerService {
    String getName();
    JsonNode handle(JsonNode input);
}
```

#### 6.3 插件热重载机制

`UpdateController` 支持插件的热重载：

```java
private void reloadPlugins() {
    // 1. 停止所有插件
    pluginManager.stopPlugins();
  
    // 2. 卸载所有插件
    pluginManager.unloadPlugins();
  
    // 3. 重新加载插件
    pluginManager.loadPlugins();
  
    // 4. 启动插件
    pluginManager.startPlugins();
  
    // 5. 日志输出
    List<HandlerService> extensions = pluginManager.getExtensions(HandlerService.class);
    for (HandlerService service : extensions) {
        System.out.println("启动插件: " + service.getName());
    }
}
```

这个机制允许在运行时动态更新插件，无需重启整个应用程序。

### 7. 项目部署方法

#### 7.1 GraphQL服务器地址配置

在 `ExecutionService` 中配置GraphQL服务器地址：

```java
private static final String GRAPHQL_ENDPOINT = "http://192.168.0.204:4000/graphql";
```

**生产环境配置**：
建议通过配置文件进行配置：

```properties
# application.properties
graphql.endpoint=http://your-production-server:4000/graphql
```

然后在代码中使用：
```java
@Value("${graphql.endpoint}")
private String graphqlEndpoint;
```

#### 7.2 打包方法

使用Maven进行打包：

```bash
# 清理并编译
mvn clean compile

# 运行测试
mvn test

# 打包成JAR文件
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

生成的JAR文件位于 `target/scriptparser-0.0.1-SNAPSHOT.jar`

#### 7.3 部署运行

**开发环境运行**：
```bash
# 直接运行
java -jar target/scriptparser-0.0.1-SNAPSHOT.jar

# 指定配置文件
java -jar target/scriptparser-0.0.1-SNAPSHOT.jar --spring.config.location=classpath:/application-prod.properties

# 指定插件目录
java -jar target/scriptparser-0.0.1-SNAPSHOT.jar --plugin.directory=/path/to/plugins
```

**生产环境部署**：
```bash
# 创建部署目录
mkdir -p /opt/scriptparser/plugins

# 复制JAR文件
cp target/scriptparser-0.0.1-SNAPSHOT.jar /opt/scriptparser/

# 创建启动脚本
cat > /opt/scriptparser/start.sh << 'EOF'
#!/bin/bash
cd /opt/scriptparser
nohup java -jar scriptparser-0.0.1-SNAPSHOT.jar \
  --server.port=8080 \
  --plugin.directory=./plugins \
  --graphql.endpoint=http://your-graphql-server:4000/graphql \
  > scriptparser.log 2>&1 &
echo $! > scriptparser.pid
EOF

chmod +x /opt/scriptparser/start.sh
```

#### 7.4 示例curl命令

**上传脚本和插件**：
```bash
# 上传GQLP脚本
curl -X POST http://localhost:8080/update \
  -F "gqlp=@example.gqlp" \
  -F "gqlpk=@example.gqlpk" \
  -F "jar=@plugin.jar"

# 只上传脚本
curl -X POST http://localhost:8080/update \
  -F "gqlp=@example.gqlp"

# 只上传插件
curl -X POST http://localhost:8080/update \
  -F "jar=@plugin1.jar" \
  -F "jar=@plugin2.jar"
```

**执行脚本**：
```bash
# 基本执行
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  -d '{
    "scriptname": "example",
    "init": {
      "tripId": "123456",
      "userId": "user001"
    }
  }'

# 复杂参数执行
curl -X POST http://localhost:8080/execute \
  -H "Content-Type: application/json" \
  -d '{
    "scriptname": "travel_query",
    "init": {
      "tripId": "TRIP_12345",
      "startDate": "2024-01-01",
      "endDate": "2024-01-10",
      "passengerCount": "2"
    }
  }'
```

**健康检查**：
```bash
# 检查服务状态
curl -X GET http://localhost:8080/actuator/health

# 检查插件状态
curl -X GET http://localhost:8080/actuator/info
```

### 8. 错误处理和监控

#### 8.1 错误处理机制

项目在多个层面实现了错误处理：

**解析错误处理**：
```java
if (result.hasErrors()) {
    throw new Exception("Parse errors: " + ErrorUtils.printParseErrors(result));
}
```

**执行错误处理**：
```java
try {
    ScriptExecutor executor = new ScriptExecutor();
    return executor.execute(statements, context);
} catch (Exception e) {
    System.err.println("Execution failed: " + e.getMessage());
    throw e;
}
```

**插件调用错误处理**：
```java
if (targetExtensionOpt.isEmpty()) {
    throw new Exception("插件 \"" + stmt.getFunctionName() + "\" 未找到或未加载。");
}
```

#### 8.2 性能监控

项目内置了基本的性能监控：

```java
private long totalGqlQueryDuration = 0;
private int gqlNum = 0;
private int pluginNum = 0;

private void executeGqlQuery(GqlQueryStatement stmt, ExecutionContext context) throws Exception {
    gqlNum++;
    long startTime = System.currentTimeMillis();
  
    // 执行查询逻辑
  
    long endTime = System.currentTimeMillis();
    totalGqlQueryDuration += (endTime - startTime);
}
```

#### 8.3 日志记录

项目使用SLF4J进行日志记录：

```java
System.out.println("总gql查询时间: " + totalGqlQueryDuration + " 毫秒");
System.out.println("启动插件: " + service.getName());
System.err.println("Runtime Error: " + e.getMessage());
```

### 9. 安全考虑

#### 9.1 文件上传安全

```java
String filename = StringUtils.cleanPath(jar.getOriginalFilename());
Path destination = pluginsPath.resolve(filename);
Files.copy(jar.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
```

使用 `StringUtils.cleanPath()` 防止路径遍历攻击。

#### 9.2 脚本执行安全

**循环限制**：
```java
final int maxIterations = 1000;
if (iteration > maxIterations) {
    System.err.println("Warning: While loop exceeded max iterations");
    break;
}
```

**类型检查**：
```java
if (!(sourceValueObj instanceof Integer)) {
    throw new Exception("Variable " + stmt.getSourceVariable() + " is not an integer.");
}
```

### 10. 扩展性设计

#### 10.1 AST节点扩展

通过实现 `Statement` 接口，可以轻松添加新的语句类型：

```java
public interface Statement {
    // 标记接口，用于类型识别
}
```

#### 10.2 插件系统扩展

通过 `HandlerService` 接口，第三方可以开发自定义插件：

```java
public interface HandlerService {
    String getName();
    JsonNode handle(JsonNode input);
}
```

#### 10.3 解析器扩展

PEG解析器支持语法规则的组合和扩展，新的语法结构可以通过添加新的 `Rule` 方法实现。

### 总结

com.example.scriptparser 项目通过精心设计的架构，实现了一个功能完整、可扩展的脚本执行引擎。其核心特性包括：

1. **强大的解析能力**：基于PEG的解析器支持复杂的语法结构
2. **灵活的执行机制**：支持多种语句类型和控制流程
3. **插件化架构**：通过PF4J实现功能的动态扩展
4. **类型安全**：完整的类型检查和错误处理机制
5. **易于部署**：Spring Boot架构，支持快速部署和配置

该项目为GraphQL查询编排和数据处理提供了强大而灵活的解决方案，适用于复杂的业务场景和微服务架构。