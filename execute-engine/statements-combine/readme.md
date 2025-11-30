# GraphQL+ Composer Service

GraphQL+文档组合服务，用于将多个微服务的GraphQL+文档组合为单一文档。

## 功能特性

- 解析GraphQL+语法
- 实现文档组合算法
- 支持循环依赖检测
- 自动检测入口服务
- 支持多种输入方式：JSON请求体、文件上传、ZIP压缩包上传
- RESTful API接口

## 快速开始

### 环境要求
- Java 17+
- Maven 3.8+

### 编译运行
```bash
# 编译
mvn clean compile

# 运行
mvn spring-boot:run

# 或者打包后运行
mvn clean package
java -jar target/gqlp-composer-1.0.0.jar
```

## API使用示例

### 方法1：JSON请求体方式

```bash
curl -X POST http://localhost:8080/api/v1/gqlp/compose \
  -H "Content-Type: application/json" \
  -d '{
    "files": [
      {
        "fileName": "order-API",
        "content": "service order-API:{\n    assign order-API.OrderSnReq.OrderSn = order@e2c17.Sn;\n    call payment-RPC;\n    assign payResp@a7bf9 = payment-RPC.OrderSnResp;\n}"
      },
      {
        "fileName": "payment-RPC", 
        "content": "service payment-RPC:{\n    new payment@e2c17 = gql query { getByOrderSn };\n    assign payment-RPC.OrderSnResp.PaymentDetail = payment@e2c17;\n    return payment-RPC.OrderSnResp;\n}"
      }
    ]
  }'
```

### 方法2：文件上传方式

**1. 创建测试文件**

`order-API.gqlp`:
```graphql
service order-API:{
    //remote procedure call (RPC)
    assign order-API.OrderSnReq.OrderSn = order@e2c17.Sn;
    call payment-RPC;
    assign payResp@a7bf9 = payment-RPC.OrderSnResp;
}
```

`payment-RPC.gqlp`:
```graphql
service payment-RPC:{
    //data query
    new payment@e2c17 = gql query { getByOrderSn };
    //return
    assign payment-RPC.OrderSnResp.PaymentDetail = payment@e2c17;
    return payment-RPC.OrderSnResp;
}
```

**2. 使用curl上传**

```bash
# 上传多个.gqlp文件
curl -X POST http://localhost:8080/api/v1/gqlp/compose/upload \
  -F "files=@order-API.gqlp" \
  -F "files=@payment-RPC.gqlp"

# 上传多个文件并指定入口服务
curl -X POST http://localhost:8080/api/v1/gqlp/compose/upload \
  -F "files=@order-API.gqlp" \
  -F "files=@payment-RPC.gqlp" \
  -F "entryServiceName=order-API"
```

### 方法3：使用绝对路径上传

```bash
# Linux/Mac 绝对路径
curl -X POST http://localhost:8080/api/v1/gqlp/compose/upload \
  -F "files=@/home/user/project/order-API.gqlp" \
  -F "files=@/home/user/project/payment-RPC.gqlp"

# Windows 路径示例
curl -X POST http://localhost:8080/api/v1/gqlp/compose/upload \
  -F "files=@C:\project\order-API.gqlp" \
  -F "files=@C:\project\payment-RPC.gqlp"
```

## 其他API端点

```bash
# 获取服务信息
curl http://localhost:8080/api/v1/gqlp/info

# 获取上传文件信息
curl http://localhost:8080/api/v1/gqlp/upload/info

# 健康检查
curl http://localhost:8080/api/health
```

## 响应示例

```json
{
  "composedDocument": "// 组合后的GraphQL+文档\n// 由iRUC系统自动生成\n\n//赋值操作\nassign order-API.OrderSnReq.OrderSn = order@e2c17.Sn;\n//数据查询\nnew payment@e2c17 = gql query { getByOrderSn };\n//赋值操作\nassign payment-RPC.OrderSnResp.PaymentDetail = payment@e2c17;\n//赋值操作\nassign payResp@a7bf9 = payment-RPC.OrderSnResp;\n",
  "success": true,
  "entryServiceName": "order-API",
  "serviceCount": 2,
  "processingDetails": "成功组合2个服务的GraphQL+文档，入口服务: order-API"
}
```

## 文件格式与限制

### 支持的文件格式
- `.gqlp` - GraphQL+文档文件
- `.txt` - 文本文件（内容需符合GraphQL+语法）

### 文件上传限制
- 单个文件最大：10MB
- 请求总大小最大：50MB
- 最多文件数量：20个
- 支持的字符编码：UTF-8
