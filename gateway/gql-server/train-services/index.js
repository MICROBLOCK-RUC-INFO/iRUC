const { ApolloServer } = require('@apollo/server');
const { buildSubgraphSchema } = require('@apollo/subgraph');
const express = require('express');
const { expressMiddleware } = require('@apollo/server/express4');
const { typeDefs, resolvers } = require('./schema');
const { sequelize } = require('./db');
const bodyParser = require('body-parser');

// 创建 Apollo Server 实例，使用 Federation 支持
const server = new ApolloServer({
  schema: buildSubgraphSchema({
    typeDefs,
    resolvers,
  }),
});

// 创建 Express 应用
const app = express();

// 启动 Apollo Server，并与 Express 集成
async function startServer() {
  try {
    // 测试数据库连接
    await sequelize.authenticate();
    console.log('✅ 数据库连接成功');

    // 启动 Apollo Server
    await server.start();
    console.log('✅ Apollo Server 启动成功');

    // 增加请求体大小限制
    app.use(bodyParser.json({ limit: '10mb' }));

    // 使用 Apollo 中间件处理 /graphql 路由
    app.use('/graphql', express.json(), expressMiddleware(server));

    // 同步数据库（开发环境使用，生产环境应使用迁移）
    await sequelize.sync({ alter: false }); // 设置为 true 会自动修改表结构
    console.log('✅ 数据库同步成功');

    // 启动 Express 服务器
    const PORT = 4003;
    app.listen({ port: PORT }, () => {
      console.log(`🚀 Statistic Subgraph ready at http://localhost:${PORT}/graphql`);
    });
  } catch (error) {
    console.error('❌ 服务启动失败:', error);
    process.exit(1);
  }
}

// 启动服务
startServer();
