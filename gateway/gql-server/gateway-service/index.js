const { ApolloServer } = require('@apollo/server');
const { ApolloGateway } = require('@apollo/gateway');

const express = require('express');
const { expressMiddleware } = require('@apollo/server/express4');

const gateway = new ApolloGateway({
  serviceList: [
    { name: 'account-service', url: 'http://localhost:4001/graphql' },
    { name: 'statistic-service', url: 'http://localhost:4002/graphql' },//其他网关
    { name: 'train-services', url: 'http://localhost:4003/graphql' },
  ],
});

const server = new ApolloServer({
  gateway,
  subscriptions: false,
  introspection: true,
});

async function startServer() {
  // 启动 Apollo Server
  await server.start();

  // 创建 Express 应用
  const app = express();
  app.use(express.json());

  // 将 Apollo Server 中间件添加到 Express 应用
  app.use('/graphql', expressMiddleware(server));

  // 启动 HTTP 服务器
  app.listen(4000, () => {
    console.log(`🚀 Gateway ready at http://localhost:4000/graphql`);
  });
}

// 启动服务
startServer();
