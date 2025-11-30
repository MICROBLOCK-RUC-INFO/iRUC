# Travel2 Service Project

这是一个基于 Spring Boot 3.3.5 开发的微服务项目 `travel2`。该服务主要负责处理列车行程（Trip）相关的查询，并与食品服务（Food Service）进行交互。

## 📋 环境准备 (Prerequisites)

在部署 `pom.xml` 和启动项目之前，请确保您的开发环境满足以下要求：

*   **Java Development Kit (JDK):** 版本 **17** (由 `pom.xml` 中的 `<java.version>17</java.version>` 指定)。
*   **Apache Maven:** 3.6 或更高版本。
*   **Database:** PostgreSQL (项目依赖 `postgresql` 驱动)。

---

## 📦 部署 POM.xml 与构建项目 (Build & Dependencies)

`pom.xml` 是 Maven 项目的核心配置文件。部署该文件实际上是指**解析依赖、下载库文件并构建可执行程序**的过程。

### 1. 理解 POM 结构
该项目的 `pom.xml` 主要包含以下关键部分：
*   **Parent:** 继承自 `spring-boot-starter-parent` (3.3.5)，统一管理版本。
*   **Dependencies:**
    *   `spring-boot-starter-data-jpa`: 用于操作数据库。
    *   `spring-boot-starter-web`: 提供 REST API。
    *   `postgresql`: PostgreSQL 数据库驱动。
    *   `lombok`: 简化代码（需要 IDE 安装 Lombok 插件）。
    *   `jakarta.persistence` & `validation`: 数据持久化与校验标准 API。

### 2. 初始化依赖 (Deploy Dependencies)
在项目根目录下（即 `pom.xml` 所在目录），打开终端执行以下命令以“部署”并下载所有依赖：

```bash
# 下载所有依赖包到本地 Maven 仓库
mvn clean dependency:resolve
```

### 3. 构建项目 (Build)
将 `pom.xml` 定义的项目打包成可执行的 JAR 文件：

```bash
# 清理旧文件并打包
mvn clean package -DskipTests
```

*构建成功后，您将在 `target/` 目录下看到 `travel2-0.0.1-SNAPSHOT.jar` 文件。*

---

## ⚙️ 项目配置 (Configuration)

**推荐配置 (`src/main/resources/application.yml`):**

```yaml
server:
  port: 8080 # 服务端口

spring:
  application:
    name: travel2-service
  datasource:
    # 请修改为您的 PostgreSQL 数据库地址、用户名和密码
    url: jdbc:postgresql://localhost:5432/your_database_name
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver
  jpa:
    hibernate:
      ddl-auto: update # 自动更新表结构
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
```

> **注意：** 请确保您的 PostgreSQL 数据库中已经创建了对应的数据库（例如 `your_database_name`），JPA 会自动创建名为 `trip2` 的表。

---

## 🚀 运行项目 (Running)

### 方式一：通过 Maven 运行（开发模式）
```bash
mvn spring-boot:run
```

### 方式二：通过 JAR 包运行（部署模式）
在执行完构建命令后：
```bash
java -jar target/travel2-0.0.1-SNAPSHOT.jar
```

---

## 🔌 接口说明 (API Usage)

项目启动后，可以通过 HTTP 请求访问以下接口：

### 查询行程并预订食物

*   **URL:** `GET /travel/retrieve_food/{tripId}`
*   **示例:** `http://localhost:8080/travel/retrieve_food/G-1234`
*   **描述:** 根据 Trip ID 查询行程信息，并尝试发送请求到 Food Service。

**注意外部依赖:**
代码 `TravelServiceImpl.java` 中硬编码了对 Food Service 的调用地址：
`http://localhost:8002/food-service/process-trip`
如果该地址不可达，接口会返回发送失败的提示，但查询逻辑仍会执行。

---

## 🛠️ 开发工具提示

如果您使用 **IntelliJ IDEA** 或 **Eclipse**：
1.  请确保已安装 **Lombok Plugin**，否则 `Trip.java` 和 `Response.java` 中的 `@Data` 注解会报错。
2.  导入项目时选择 "Maven Project"，IDE 会自动解析 `pom.xml`。


---

## 🔗 服务调用关系与整体部署 (Service Topology & Baseline)

上面给出的工程 `travel2` 仅是完整微服务链路中的入口节点。为了建立完整的实验基准环境，整个系统包含 **6 个微服务**，它们构成了一条带有分支结构的调用路径。

### 1. 服务调用拓扑
这 6 个服务共同组成了一个实验基准（Baseline）链路，流量从 `travel2` 进入，向后传递。层级结构如下：

```text
travel2 ──➤ food ──➤ route ──➤ stationfood ──➤ fooddilivery
             │
             └──➤ trainfood
```
*(注：`travel2` 调用 `food`，后续服务根据业务逻辑形成链式及分支调用)*

### 2. 其余服务部署说明
除了 `travel2` 之外，其余 5 个微服务（`food`, `route`, `stationfood`, `fooddilivery`, `trainfood`）均**按照类似的方法部署**：

1.  **依赖管理**: 对每个工程的 `pom.xml` 执行依赖解析与下载。
    ```bash
    mvn clean dependency:resolve
    ```
2.  **构建打包**: 使用 Maven 生成可执行 JAR 包。
    ```bash
    mvn clean package -DskipTests
    ```
3.  **普通微服务部署**: 使用 Java 命令启动服务。
    ```bash
    java -jar target/xxx-service-0.0.1-SNAPSHOT.jar
    ```

> **💡 实验提示**:
> *   请确保所有 6 个服务都已成功启动。
> *   请注意各服务的端口配置，避免端口冲突（例如 `travel2` 使用 8080，`food` 使用 8002 等）。
> *   此部署模式为**普通微服务部署**，作为实验对照组的基准环境。