const BaseAgent = require('./BaseAgent');

class Agent5ScriptCombiner extends BaseAgent {
  constructor() {
    super(
      '智能体5-脚本组合', 
      'agent5-prompt.txt',
      'agent5-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getAgent4Output(),
      '当前服务名': this.extractServiceName(context.getAuxiliaryInfo())
    };

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Claude模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('claude-sonnet-4-5-20250929', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent5TokenUsage', result.usage);

    // 更新上下文
    context.setAgent5Output(response);

    return context;
  }

  extractServiceName(auxiliaryInfo) {
    if (!auxiliaryInfo) {
      return 'unknown-service';
    }

    // 尝试不同的模式匹配服务名
    const patterns = [
      /(?:服务名|service\s+name|current\s+service)\s*[:：]\s*([a-zA-Z][a-zA-Z0-9-]*)/i,
      /service\s*[:：]\s*([a-zA-Z][a-zA-Z0-9-]*)/i,
      /当前服务\s*[:：]\s*([a-zA-Z][a-zA-Z0-9-]*)/i
    ];

    for (const pattern of patterns) {
      const match = auxiliaryInfo.match(pattern);
      if (match && match[1]) {
        return match[1].trim();
      }
    }

    // 如果没有匹配到，尝试从第一行提取
    const lines = auxiliaryInfo.split('\n');
    for (const line of lines) {
      const trimmedLine = line.trim();
      if (trimmedLine && /^[a-zA-Z][a-zA-Z0-9-]*$/.test(trimmedLine)) {
        return trimmedLine;
      }
    }

    // 默认服务名
    return 'unknown-service';
  }
}

module.exports = Agent5ScriptCombiner;
