const express = require('express');
const cors = require('cors');
const winston = require('winston');
const dotenv = require('dotenv');
const path = require('path');
const workflowRoutes = require('./routes/workflow');
const ProgressTracker = require('./utils/ProgressTracker');

// 加载环境变量
dotenv.config();

// 创建Express应用
const app = express();
const PORT = process.env.PORT || 3000;

// 配置日志
const logger = winston.createLogger({
  level: 'info',
  format: winston.format.combine(
    winston.format.timestamp(),
    winston.format.json()
  ),
  transports: [
    new winston.transports.File({ filename: 'logs/error.log', level: 'error' }),
    new winston.transports.File({ filename: 'logs/combined.log' }),
    new winston.transports.Console({
      format: winston.format.simple()
    })
  ]
});

// 中间件配置
app.use(cors());
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// 静态文件服务
app.use(express.static(path.join(__dirname, '../public')));

// 路由配置
app.use('/api/workflow', workflowRoutes);

// 首页路由
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// 健康检查端点
app.get('/health', (req, res) => {
  res.json({ 
    status: 'ok', 
    timestamp: new Date().toISOString(),
    version: process.env.npm_package_version || '1.0.0'
  });
});

// WebSocket支持进度跟踪
const { createServer } = require('http');
const { WebSocketServer } = require('ws');

const server = createServer(app);
const wss = new WebSocketServer({ server });

// 进度跟踪WebSocket连接
wss.on('connection', (ws) => {
  logger.info('WebSocket客户端已连接');
  
  // 注册进度监听器
  const progressListener = (agentName, message, progress) => {
    if (ws.readyState === ws.OPEN) {
      ws.send(JSON.stringify({
        type: 'progress',
        agentName,
        message,
        progress,
        timestamp: new Date().toISOString()
      }));
    }
  };
  
  ProgressTracker.getInstance().registerStatusListener(progressListener);
  
  ws.on('close', () => {
    logger.info('WebSocket客户端已断开连接');
    ProgressTracker.getInstance().removeStatusListener(progressListener);
  });
  
  ws.on('error', (error) => {
    logger.error('WebSocket错误:', error);
  });
});

// 错误处理中间件
app.use((err, req, res, next) => {
  logger.error('服务器错误:', err);
  res.status(500).json({
    error: '内部服务器错误',
    message: process.env.NODE_ENV === 'development' ? err.message : '发生未知错误'
  });
});

// 404处理
app.use((req, res) => {
  res.status(404).json({
    error: '未找到请求的资源',
    path: req.path
  });
});

// 启动服务器
server.listen(PORT, () => {
  logger.info(`服务器已启动，监听端口 ${PORT}`);
  logger.info(`Web界面地址: http://localhost:${PORT}`);
  logger.info(`API文档地址: http://localhost:${PORT}/api/workflow`);
  logger.info(`健康检查地址: http://localhost:${PORT}/health`);
});

// 优雅关闭
process.on('SIGTERM', () => {
  logger.info('收到SIGTERM信号，开始优雅关闭...');
  server.close(() => {
    logger.info('服务器已关闭');
    process.exit(0);
  });
});

process.on('SIGINT', () => {
  logger.info('收到SIGINT信号，开始优雅关闭...');
  server.close(() => {
    logger.info('服务器已关闭');
    process.exit(0);
  });
});

module.exports = app;
