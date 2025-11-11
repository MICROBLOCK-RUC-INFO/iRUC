const fs = require('fs');
const path = require('path');
const ApiClient = require('../utils/ApiClient');
const ProgressTracker = require('../utils/ProgressTracker');

class BaseAgent {
  constructor(agentName, systemPromptPath, examplesPath = null) {
    this.apiClient = new ApiClient();
    this.agentName = agentName;
    this.systemPrompt = this.loadSystemPrompt(systemPromptPath);
    this.examples = examplesPath ? this.loadExamples(examplesPath) : [];
    this.progressTracker = ProgressTracker.getInstance();
  }

  loadSystemPrompt(promptPath) {
    try {
      const fullPath = path.join(__dirname, '../system-prompts', promptPath);
      if (!fs.existsSync(fullPath)) {
        throw new Error(`系统提示词文件不存在: ${fullPath}`);
      }
      return fs.readFileSync(fullPath, 'utf-8');
    } catch (error) {
      throw new Error(`无法加载系统提示词文件: ${promptPath} - ${error.message}`);
    }
  }

  loadExamples(examplesPath) {
    try {
      const fullPath = path.join(__dirname, '../system-prompts', examplesPath);
      if (!fs.existsSync(fullPath)) {
        console.warn(`示例文件不存在: ${fullPath}`);
        return [];
      }
      const content = fs.readFileSync(fullPath, 'utf-8');
      return JSON.parse(content);
    } catch (error) {
      console.warn(`无法加载示例文件: ${examplesPath} - ${error.message}`);
      return [];
    }
  }

  // 构建包含示例的完整提示词
  buildFullPrompt() {
    let fullPrompt = this.systemPrompt;
    
    // 如果有示例，添加到提示词中
    if (this.examples.length > 0) {
      fullPrompt += '\n\n## 示例对话\n\n';
      
      this.examples.forEach((example, index) => {
        if (example.description) {
          fullPrompt += `### 示例 ${index + 1}: ${example.description}\n\n`;
        }
        
        if (example.conversations && Array.isArray(example.conversations)) {
          example.conversations.forEach(conv => {
            if (conv.role === 'user') {
              fullPrompt += `**用户输入:**\n${conv.content}\n\n`;
            } else if (conv.role === 'assistant') {
              fullPrompt += `**输出:**\n${conv.content}\n\n`;
            }
          });
        }
        
        fullPrompt += '---\n\n';
      });
      
      fullPrompt += '请参考以上示例，处理以下实际输入:\n\n';
    }
    
    return fullPrompt;
  }

  async process(context) {
    throw new Error('子类必须实现process方法');
  }

  // 修改：返回包含content和usage的对象
  async executeAgent(modelName, userMessage) {
    try {
      console.log(`[${this.agentName}] 正在调用API (${modelName})...`);
      this.progressTracker.updateProgress(this.agentName, '正在调用API...', 0.25);

      const fullPrompt = this.buildFullPrompt();
      
      // 验证输入
      if (!userMessage || userMessage.trim().length === 0) {
        throw new Error('用户消息不能为空');
      }

      if (!fullPrompt || fullPrompt.trim().length === 0) {
        throw new Error('系统提示词不能为空');
      }

      // callApiWithRetry 返回 { content, usage }
      const result = await this.apiClient.callApiWithRetry(
        modelName, 
        fullPrompt, 
        userMessage, 
        this.agentName,
        3 // 最大重试次数
      );

      if (!result || !result.content || result.content.trim().length === 0) {
        throw new Error('API返回了空响应');
      }

      console.log(`[${this.agentName}] API调用完成`);
      this.progressTracker.updateProgress(this.agentName, 'API调用完成', 1.0);

      // 返回完整的结果对象，包含content和usage
      return result;
    } catch (error) {
      console.error(`[${this.agentName}] API调用失败:`, error.message);
      this.progressTracker.updateProgress(this.agentName, `API调用失败: ${error.message}`, 0);
      throw new Error(`${this.agentName} - 调用API失败: ${error.message}`);
    }
  }

  createPrompt(context, additionalInfo = {}) {
    let prompt = '';

    // 添加主要内容
    if (additionalInfo.mainContent) {
      prompt += additionalInfo.mainContent + '\n\n';
    }

    // 添加辅助信息
    const auxiliaryInfo = context.getAuxiliaryInfo();
    if (auxiliaryInfo && auxiliaryInfo.trim().length > 0) {
      prompt += '辅助信息:\n' + auxiliaryInfo + '\n\n';
    }

    // 添加额外信息
    Object.entries(additionalInfo).forEach(([key, value]) => {
      if (key !== 'mainContent' && value && value.toString().trim().length > 0) {
        prompt += `${key}:\n${value}\n\n`;
      }
    });

    return prompt.trim();
  }

  // 验证上下文
  validateContext(context) {
    if (!context) {
      throw new Error('上下文不能为空');
    }

    if (!context.getCode || typeof context.getCode !== 'function') {
      throw new Error('上下文必须有getCode方法');
    }

    const code = context.getCode();
    if (!code || code.trim().length === 0) {
      throw new Error('上下文中的代码不能为空');
    }

    return true;
  }

  // 获取智能体信息
  getAgentInfo() {
    return {
      name: this.agentName,
      hasSystemPrompt: !!this.systemPrompt,
      hasExamples: this.examples.length > 0,
      exampleCount: this.examples.length,
      systemPromptLength: this.systemPrompt ? this.systemPrompt.length : 0
    };
  }
}

module.exports = BaseAgent;
