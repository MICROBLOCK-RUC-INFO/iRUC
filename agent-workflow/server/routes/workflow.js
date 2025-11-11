const express = require('express');
const router = express.Router();
const AgentWorkflow = require('../workflow/AgentWorkflow');
const InputData = require('../models/InputData');
const ProgressTracker = require('../utils/ProgressTracker');

// 工作流执行端点
router.post('/execute', async (req, res) => {
  try {
    const { code, auxiliaryInfo } = req.body;

    // 验证输入数据
    const inputData = new InputData(code, auxiliaryInfo);
    const validation = inputData.validate();
    
    if (!validation.isValid) {
      return res.status(400).json({
        error: '输入数据验证失败',
        details: validation.errors
      });
    }

    // 创建工作流实例
    const workflow = new AgentWorkflow();

    // 重置进度跟踪器
    ProgressTracker.getInstance().reset();

    // 执行工作流
    const result = await workflow.execute(inputData);

    // 返回结果
    res.json({
      success: true,
      data: result.toJSON(),
      message: '工作流执行完成'
    });

  } catch (error) {
    console.error('工作流执行错误:', error);
    res.status(500).json({
      error: '工作流执行失败',
      message: error.message
    });
  }
});

// 获取工作流状态
router.get('/status', (req, res) => {
  const status = ProgressTracker.getInstance().getCurrentStatus();
  res.json({
    success: true,
    data: status
  });
});

// 健康检查
router.get('/health', (req, res) => {
  res.json({ 
    status: 'ok',
    service: 'workflow-api',
    timestamp: new Date().toISOString()
  });
});

module.exports = router;
