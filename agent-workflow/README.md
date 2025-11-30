# 智能体工作流项目

基于LangChain.js构建的智能代码转换工作流系统。

## 功能特性

- 基于LangChain.js的智能体链式处理
- PEG语法解析器进行代码语法校验
- 支持对话调优和示例训练
- 多模型支持（Claude、Gemini、OpenAI）

## 安装与运行

1. 安装依赖：

npm install

2. 配置环境变量：

cp .env.example .env

编辑.env文件填入API密钥

1. 构建项目：

npm run build

4. 启动后端服务：

npm start

## 使用说明

1. 访问 http://localhost:3000 使用API接口或由浏览器直接访问
2. 发送服务源代码到智能体工作流
3. 等待智能体给出转换后的GraphQL+语句。

