const BaseAgent = require('./BaseAgent');

class Agent6Validator extends BaseAgent {
  constructor() {
    super(
      '智能体6-功能校验器', 
      'agent6-prompt.txt',
      'agent6-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getAgent5Output()
    };

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Gemini模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('gemini-2.5-pro', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent6TokenUsage', result.usage);

    // 更新上下文
    context.setAgent6Output(response);

    // 判断验证结果
    const passed = this.parseValidationResult(response);
    context.put('validationPassed', passed);

    if (!passed) {
      // 如果验证未通过，设置需要重新生成标记
      context.put('needRegenerate', true);
      context.put('errorMessage', response);
      context.put('errorType', '功能校验错误');
    }

    return context;
  }

  parseValidationResult(response) {
    if (!response || typeof response !== 'string') {
      return false;
    }

    const trimmedResponse = response.trim().toLowerCase();
    
    // 检查各种表示"通过"的词汇
    const passKeywords = ['通过', 'pass', 'passed', 'success', 'successful', '成功'];
    const failKeywords = ['不通过', 'fail', 'failed', 'error', '错误', '失败'];

    // 检查是否以通过关键词开头
    for (const keyword of passKeywords) {
      if (trimmedResponse.startsWith(keyword)) {
        return true;
      }
    }

    // 检查是否以失败关键词开头
    for (const keyword of failKeywords) {
      if (trimmedResponse.startsWith(keyword)) {
        return false;
      }
    }

    // 如果没有明确的关键词，检查内容长度和内容特征
    // 通常验证通过的回复比较简短
    if (trimmedResponse.length < 100 && !trimmedResponse.includes('错误') && !trimmedResponse.includes('问题')) {
      return true;
    }

    // 默认返回false，要求明确的通过标识
    return false;
  }
}

module.exports = Agent6Validator;
