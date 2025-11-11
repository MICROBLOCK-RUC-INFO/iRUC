#!/bin/bash

# 启动 Account 服务
echo "Starting Account service on port 4001..."
cd graphql-account-service
#npm install  # 安装依赖（如果第一次运行）
node index.js &
cd ..

# 启动 DataPoint 服务
echo "Starting Statistic service on port 4002..."
cd graphql-statistics-service
#npm install  # 安装依赖（如果第一次运行）
node index.js &
cd ..

# 启动 Notification 服务
echo "Starting Notification service on port 4003..."
cd graphql-notification-service
#npm install  # 安装依赖（如果第一次运行）
node index.js &
cd ..

sleep 2

# 启动 Apollo Gateway 服务
echo "Starting Gateway service on port 4000..."
cd gateway-service
#npm install  # 安装依赖（如果第一次运行）
node index.js &
cd ..

echo "All services have been started!"
