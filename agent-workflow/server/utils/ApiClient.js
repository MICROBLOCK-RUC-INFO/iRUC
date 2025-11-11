const axios = require('axios');
const winston = require('winston');
const ProgressTracker = require('./ProgressTracker');

class ApiClient {
  constructor() {
    this.logger = winston.createLogger({
      level: 'info',
      format: winston.format.json(),
      transports: [
        new winston.transports.Console()
      ]
    });

    // 从环境变量或配置文件加载API配置
    this.claudeApiUrl = process.env.CLAUDE_API_URL;
    this.geminiApiUrl = process.env.GEMINI_API_URL;
    this.openaiApiUrl = process.env.OPENAI_API_URL;

    // API密钥
    this.claudeApiKey = process.env.CLAUDE_API_KEY;
    this.geminiApiKey = process.env.GEMINI_API_KEY;
    this.openaiApiKey = process.env.OPENAI_API_KEY;

    // 超时配置
    this.connectTimeout = parseInt(process.env.API_TIMEOUT_CONNECT) || 60000;
    this.readTimeout = parseInt(process.env.API_TIMEOUT_READ) || 90000;
    this.writeTimeout = parseInt(process.env.API_TIMEOUT_WRITE) || 90000;

    // 初始化进度跟踪器
    this.progressTracker = ProgressTracker.getInstance();

    // 验证API密钥
    this.validateApiKeys();
  }

  validateApiKeys() {
    const missingKeys = [];
    
    if (!this.claudeApiKey) {
      missingKeys.push('CLAUDE_API_KEY');
    }
    
    if (!this.geminiApiKey) {
      missingKeys.push('GEMINI_API_KEY');
    }
    
    if (missingKeys.length > 0) {
      this.logger.warn(`缺少API密钥: ${missingKeys.join(', ')}`);
    }
  }

  // 直接调用API的方法（修改返回值，包含token信息）
  async callApi(model, systemPrompt, userMessage, agentName = 'Unknown') {
    try {
      this.progressTracker.updateProgress(agentName, '正在连接API...', 0.3);

      // 构建请求体
      const requestBody = {
        model: model,
        messages: [
          {
            role: 'system',
            content: systemPrompt
          },
          {
            role: 'user',
            content: userMessage
          }
        ],
        temperature: 0.1,
        max_tokens: 8192
      };

      // 根据模型名称选择API URL和密钥
      let apiUrl, apiKey;
      if (model.startsWith('gemini')) {
        apiUrl = this.geminiApiUrl;
        apiKey = this.geminiApiKey;
      } else if (model.startsWith('claude')) {
        apiUrl = this.claudeApiUrl;
        apiKey = this.claudeApiKey;
      } else if (model.startsWith('gpt')) {
        apiUrl = this.openaiApiUrl;
        apiKey = this.openaiApiKey;
      } else {
        // 默认使用Claude
        apiUrl = this.claudeApiUrl;
        apiKey = this.claudeApiKey;
      }

      if (!apiKey) {
        throw new Error(`未设置${model}模型的API密钥`);
      }

      // 发送HTTP请求
      const response = await axios.post(apiUrl, requestBody, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${apiKey}`
        },
        timeout: this.readTimeout
      });

      this.progressTracker.updateProgress(agentName, '正在处理API响应...', 0.6);

      // 检查响应状态
      if (response.status !== 200) {
        this.progressTracker.updateProgress(agentName, `API调用失败: ${response.status}`, 0);
        throw new Error(`API调用失败: ${response.status} ${response.statusText}`);
      }

      // 解析响应
      const responseData = response.data;
      if (!responseData.choices || responseData.choices.length === 0) {
        throw new Error('API响应格式错误：缺少choices字段');
      }

      const choice = responseData.choices[0];
      if (!choice.message || !choice.message.content) {
        throw new Error('API响应格式错误：缺少message.content字段');
      }

      // 提取并打印token使用信息
      const tokenUsage = this.logTokenUsage(responseData, model, agentName);

      this.progressTracker.updateProgress(agentName, 'API调用成功', 0.9);

      // 修改返回值：返回内容和token使用信息
      return {
        content: choice.message.content,
        usage: tokenUsage
      };

    } catch (error) {
      this.progressTracker.updateProgress(agentName, `API调用失败: ${error.message}`, 0);
      this.logger.error(`API调用失败 [${agentName}]:`, error.message);
      
      if (error.code === 'ECONNABORTED') {
        throw new Error(`API调用超时: ${error.message}`);
      } else if (error.response) {
        throw new Error(`API调用失败: ${error.response.status} ${error.response.statusText}`);
      } else {
        throw error;
      }
    }
  }

  // 修改方法：返回token使用情况
  logTokenUsage(responseData, model, agentName) {
    try {
      // 尝试从响应中提取usage信息
      const usage = responseData.usage;
      
      if (usage) {
        const promptTokens = usage.prompt_tokens || 0;
        const completionTokens = usage.completion_tokens || 0;
        const totalTokens = usage.total_tokens || (promptTokens + completionTokens);
        
        // 格式化输出token统计信息
        console.log('\n' + '='.repeat(60));
        console.log(`🤖 ${agentName} - Token使用统计`);
        console.log('='.repeat(60));
        console.log(`📊 模型: ${model}`);
        console.log(`📥 输入Token数 (Prompt): ${promptTokens.toLocaleString()}`);
        console.log(`📤 输出Token数 (Completion): ${completionTokens.toLocaleString()}`);
        console.log(`📊 总Token数 (Total): ${totalTokens.toLocaleString()}`);
        console.log('='.repeat(60) + '\n');

        // 记录到日志
        this.logger.info(`Token统计 [${agentName}] - 模型: ${model}, 输入: ${promptTokens}, 输出: ${completionTokens}, 总计: ${totalTokens}`);
        
        // 返回token使用信息
        return {
          promptTokens,
          completionTokens,
          totalTokens
        };
      } else {
        // 如果没有usage字段，记录警告
        console.log(`⚠️ ${agentName} - 未能获取Token使用信息 (API响应中不包含usage字段)`);
        this.logger.warn(`未能获取Token使用信息 [${agentName}]`);
        
        // 返回默认值
        return {
          promptTokens: 0,
          completionTokens: 0,
          totalTokens: 0
        };
      }
    } catch (error) {
      // 如果解析token信息出错，记录错误但不中断流程
      console.log(`❌ ${agentName} - 解析Token信息时出错: ${error.message}`);
      this.logger.error(`解析Token信息失败 [${agentName}]:`, error.message);
      
      // 返回默认值
      return {
        promptTokens: 0,
        completionTokens: 0,
        totalTokens: 0
      };
    }
  }

  // 带重试机制的API调用
  async callApiWithRetry(model, systemPrompt, userMessage, agentName = 'Unknown', maxRetries = 3) {
    let lastError;
    
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
      try {
        this.logger.info(`${agentName} - 尝试第 ${attempt} 次API调用`);
        const result = await this.callApi(model, systemPrompt, userMessage, agentName);
        return result;
      } catch (error) {
        lastError = error;
        this.logger.warn(`${agentName} - 第 ${attempt} 次尝试失败:`, error.message);
        
        if (attempt < maxRetries) {
          const delay = Math.pow(2, attempt) * 1000; // 指数退避
          this.logger.info(`${agentName} - 等待 ${delay}ms 后重试...`);
          await new Promise(resolve => setTimeout(resolve, delay));
        }
      }
    }
    
    throw new Error(`${agentName} - 所有重试尝试都失败了。最后错误: ${lastError.message}`);
  }

  // 为了兼容性，保留原来的模型获取方法（但实际不使用LangChain）
  getClaudeModel() {
    return {
      invoke: async (messages) => {
        const systemPrompt = messages.find(m => m.role === 'system')?.content || '';
        const userMessage = messages.find(m => m.role === 'user')?.content || '';
        const result = await this.callApi('claude-sonnet-4-5-20250929', systemPrompt, userMessage);
        return { content: result.content };
      }
    };
  }

  getGeminiModel() {
    return {
      invoke: async (messages) => {
        const systemPrompt = messages.find(m => m.role === 'system')?.content || '';
        const userMessage = messages.find(m => m.role === 'user')?.content || '';
        const result = await this.callApi('gemini-2.5-pro', systemPrompt, userMessage);
        return { content: result.content };
      }
    };
  }

  getOpenAIModel() {
    return {
      invoke: async (messages) => {
        const systemPrompt = messages.find(m => m.role === 'system')?.content || '';
        const userMessage = messages.find(m => m.role === 'user')?.content || '';
        const result = await this.callApi('gpt-4', systemPrompt, userMessage);
        return { content: result.content };
      }
    };
  }
}

module.exports = ApiClient;
