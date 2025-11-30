# GraphQL Federation 项目 README

## 项目概述

这是一个基于 Apollo Federation 的 GraphQL 微服务架构项目,包含多个子服务和一个统一的网关。

## 项目结构

```
.
├── gateway-service/              # Apollo Gateway 网关服务
├── graphql-account-service/      # 账户服务
├── graphql-statistics-service/   # 统计服务
└── graphql-notification-service/ # 通知服务
```

## 各服务说明

### 1. Database Schema (db.js)
负责定义数据库模型和连接配置:
- 使用 Sequelize ORM 连接 PostgreSQL 数据库
- 定义了以下数据模型:
  - **Trip**: 行程信息(车次、路线、时间等)
  - **FoodOrder**: 餐饮订单
  - **TrainFood**: 列车餐饮
  - **Route**: 路线信息
  - **StationFoodStore**: 车站餐饮店铺
  - **FoodDeliveryOrder**: 餐饮配送订单

### 2. GraphQL Schema & Resolvers (schema.js)
定义 GraphQL 类型系统和数据解析逻辑:
- 定义了所有数据类型的 GraphQL Schema
- 实现了查询(Query)和变更(Mutation)的解析器
- 支持 CRUD 操作(创建、读取、更新、删除)

### 3. Gateway (gateway-service/index.js)
**Apollo Gateway 是整个系统的核心网关**,负责:
- **统一入口**: 提供单一的 GraphQL 端点 (http://localhost:4000/graphql)
- **服务发现**: 自动发现和连接所有子服务
- **查询路由**: 将客户端查询智能路由到对应的数据库/服务
- **Schema 组合**: 将多个子服务的 Schema 组合成统一的 Schema
- **查询规划**: 优化跨服务的查询执行计划

配置的子服务:
```javascript
serviceList: [
  { name: 'account-service', url: 'http://localhost:4001/graphql' },
  { name: 'statistic-service', url: 'http://localhost:4002/graphql' },
  { name: 'train-services', url: 'http://localhost:4003/graphql' },
]
```

## 部署指南

### 前置要求
- Node.js (v14 或更高版本)
- PostgreSQL 数据库
- npm 或 yarn 包管理器

### 1. 环境配置

首先配置数据库连接,在 `db.js` 中修改:
```javascript
const sequelize = new Sequelize('数据库名', '用户名', '密码', {
  host: 'localhost',
  dialect: 'postgres',
  port: 5432,
});
```

### 2. 安装依赖

为每个服务安装依赖:

```bash
# 安装 Gateway 依赖
cd gateway-service
npm install

# 安装 Account 服务依赖
cd ../graphql-account-service
npm install

# 安装 Statistics 服务依赖
cd ../graphql-statistics-service
npm install

# 安装 Notification 服务依赖
cd ../graphql-notification-service
npm install
```

### 3. 启动服务

#### 方式一: 使用启动脚本(推荐)

```bash
# 启动所有服务
chmod +x start-all.sh
./start-all.sh

# 停止所有服务
chmod +x stop-all.sh
./stop-all.sh
```

#### 方式二: 手动启动

**重要**: 必须先启动所有子服务,最后启动 Gateway

```bash
# 1. 启动 Account 服务 (端口 4001)
cd graphql-account-service
node index.js &

# 2. 启动 Statistics 服务 (端口 4002)
cd ../graphql-statistics-service
node index.js &

# 3. 启动 Notification 服务 (端口 4003)
cd ../graphql-notification-service
node index.js &

# 4. 最后启动 Gateway (端口 4000)
cd ../gateway-service
node index.js
```

### 4. 验证部署

访问 Gateway 的 GraphQL Playground:
```
http://localhost:4000/graphql
```

测试查询示例:
```graphql
query {
  getFoodOrderByOrderId(orderId: "Express") {
    id
    orderId
    foodType
    stationName
    storeName
    foodName
    price
  }
}
```

## 如何创建新的子图服务

### 使用 AI 辅助创建子图

当你需要为新的数据库创建一个子图服务时,可以使用大模型(如 ChatGPT、Claude 等)来快速生成代码。

#### 步骤 1: 准备 Prompt

向大模型输入以下 prompt 模板:

```
现在有一个 apollo 的联合网关项目
-----------------------------------------
其 gateway-service 的 index.js 内容如下：

const { ApolloServer } = require('@apollo/server');
const { ApolloGateway } = require('@apollo/gateway');
const express = require('express');
const { expressMiddleware } = require('@apollo/server/express4');

const gateway = new ApolloGateway({
  serviceList: [
    { name: 'account-service', url: 'http://localhost:4001/graphql' },
  ],
});

const server = new ApolloServer({
  gateway,
  subscriptions: false,
  introspection: true,
});

async function startServer() {
  await server.start();
  const app = express();
  app.use(express.json());
  app.use('/graphql', expressMiddleware(server));
  app.listen(4000, () => {
    console.log(`🚀 Gateway ready at http://localhost:4000/graphql`);
  });
}

startServer();

--------------------------------------
其某个具体数据库 service 内容如下：
graphql-account-service：

db.js 内容如下：
const { Sequelize, DataTypes } = require('sequelize');

const sequelize = new Sequelize('postgres://admin:12345678@localhost:5432/account_db', {
  dialect: 'postgres',
});

const Account = sequelize.define('Account', {
  id: {
    type: DataTypes.BIGINT,
    primaryKey: true,
    autoIncrement: true,
  },
  name: {
    type: DataTypes.STRING,
    allowNull: false,
    unique: true,
  },
  lastSeen: {
    type: DataTypes.DATE,
  },
  incomes: {
    type: DataTypes.ARRAY(DataTypes.STRING),
  },
  expenses: {
    type: DataTypes.ARRAY(DataTypes.STRING),
  },
  saving: {
    type: DataTypes.STRING,
    allowNull: false,
  },
});

module.exports = { sequelize, Account };

index.js 内容如下：
const { ApolloServer } = require('@apollo/server');
const { buildSubgraphSchema } = require('@apollo/subgraph');
const express = require('express');
const { expressMiddleware } = require('@apollo/server/express4');
const { typeDefs, resolvers } = require('./schema');
const { sequelize } = require('./db');
const bodyParser = require('body-parser');

const server = new ApolloServer({
  schema: buildSubgraphSchema({
    typeDefs,
    resolvers,
  }),
});

const app = express();

async function startServer() {
  await server.start();
  app.use(bodyParser.json({ limit: '10mb' }));
  app.use('/graphql', express.json(), expressMiddleware(server));

  sequelize.sync().then(() => {
    app.listen({ port: 4001 }, () => {
      console.log(`🚀 Account Subgraph ready at http://localhost:4001/graphql`);
    });
  });
}

startServer();

schema.js 内容如下：
const { gql } = require('apollo-server');
const { Account } = require('./db');

const typeDefs = gql`
  type Account {
    id: ID!
    name: String!
    lastSeen: String
    incomes: [String]
    expenses: [String]
    saving: String!
  }

  type Query {
    getAccountByName(name: String!): Account
  }

  input AccountInput {
    name: String!
    lastSeen: String
    incomes: [String]
    expenses: [String]
    saving: String!
  }

  type Mutation {
    updateAccount(account: AccountInput!): Account
  }
`;

const resolvers = {
  Query: {
    async getAccountByName(_, { name }) {
      return await Account.findOne({ where: { name } });
    },
  },
  Mutation: {
    async updateAccount(_, { account }) {
      const [updatedAccount] = await Account.upsert(account);
      return updatedAccount;
    },
  },
};

module.exports = { typeDefs, resolvers };

----------------------------------------------------
现在，我有一个新的数据库要加入这个联合网关

数据库类型为 pg 数据库，数据库名为 [你的数据库名]，表名为 [你的表名]，表的 schema 如下：
[粘贴你的表结构]

一组示例数据为：
[粘贴示例数据]

需要实现的查询方法：
[描述你需要的查询和变更操作]

给出新增的项目结构、所有文件的完整代码。在一个回复中给出，用中文回答。
```

#### 步骤 2: 替换占位符

将 prompt 中的占位符替换为你的实际信息:
- `[你的数据库名]`: 例如 `statistic_db`
- `[你的表名]`: 例如 `DataPoints`
- `[粘贴你的表结构]`: 粘贴数据库表的列定义
- `[粘贴示例数据]`: 粘贴一行示例数据
- `[描述你需要的查询和变更操作]`: 例如 "需要实现 getDataPointByAccount 查询和 updateDataPoint 变更"

#### 步骤 3: 获取生成的代码

大模型会返回完整的项目结构和代码,包括:
- 项目文件夹结构
- `db.js` - 数据库模型定义
- `schema.js` - GraphQL Schema 和 Resolvers
- `index.js` - 服务启动文件
- `package.json` - 依赖配置

#### 步骤 4: 创建新服务

```bash
# 1. 创建新服务目录
mkdir graphql-your-service-name
cd graphql-your-service-name

# 2. 将大模型生成的代码分别保存到对应文件
# - db.js
# - schema.js
# - index.js
# - package.json

# 3. 安装依赖
npm install

# 4. 测试服务
node index.js
```

#### 步骤 5: 注册到 Gateway

在 `gateway-service/index.js` 中添加新服务:

```javascript
const gateway = new ApolloGateway({
  serviceList: [
    { name: 'account-service', url: 'http://localhost:4001/graphql' },
    { name: 'statistic-service', url: 'http://localhost:4002/graphql' },
    { name: 'your-new-service', url: 'http://localhost:4004/graphql' }, // 新增
  ],
});
```

#### 步骤 6: 更新启动脚本

在 `start-all.sh` 中添加新服务的启动命令:

```bash
# 启动新服务
echo "Starting Your New Service on port 4004..."
cd graphql-your-service-name
node index.js &
cd ..
```

### 示例: 创建 Statistics 服务

假设你要为 `statistic_db` 数据库创建服务,表结构如下:

```
表名: DataPoints
列:
- id (jsonb, 主键)
- incomes (array)
- expenses (array)
- statistics (jsonb)
- rates (jsonb)
```

使用上述 prompt 模板,大模型会生成类似这样的代码:

**db.js**:
```javascript
const { Sequelize, DataTypes } = require('sequelize');

const sequelize = new Sequelize('postgres://admin:12345678@localhost:5432/statistic_db', {
  dialect: 'postgres',
});

const DataPoint = sequelize.define('DataPoint', {
  id: {
    type: DataTypes.JSONB,
    primaryKey: true,
  },
  incomes: {
    type: DataTypes.ARRAY(DataTypes.STRING),
  },
  expenses: {
    type: DataTypes.ARRAY(DataTypes.STRING),
  },
  statistics: {
    type: DataTypes.JSONB,
  },
  rates: {
    type: DataTypes.JSONB,
  },
}, {
  tableName: 'DataPoints',
  timestamps: true,
});

module.exports = { sequelize, DataPoint };
```

**schema.js**:
```javascript
const { gql } = require('apollo-server');
const { DataPoint } = require('./db');

const typeDefs = gql`
  type DataPointId {
    account: String!
    date: String!
  }

  type DataPoint {
    id: DataPointId!
    incomes: [String]
    expenses: [String]
    statistics: Statistics
    rates: Rates
  }

  type Statistics {
    SAVING_AMOUNT: Float
    INCOMES_AMOUNT: Float
    EXPENSES_AMOUNT: Float
  }

  type Rates {
    EUR: Float
    RUB: Float
    USD: Float
    BASE: Float
  }

  type Query {
    getDataPointByAccount(account: String!): DataPoint
  }

  input DataPointInput {
    account: String!
    date: String!
    incomes: [String]
    expenses: [String]
  }

  type Mutation {
    updateDataPoint(input: DataPointInput!): DataPoint
  }
`;

const resolvers = {
  Query: {
    async getDataPointByAccount(_, { account }) {
      const today = new Date().toISOString().split('T')[0];
      return await DataPoint.findOne({
        where: {
          id: { account, date: today }
        }
      });
    },
  },
  Mutation: {
    async updateDataPoint(_, { input }) {
      const [dataPoint] = await DataPoint.upsert({
        id: { account: input.account, date: input.date },
        incomes: input.incomes,
        expenses: input.expenses,
      });
      return dataPoint;
    },
  },
};

module.exports = { typeDefs, resolvers };
```
