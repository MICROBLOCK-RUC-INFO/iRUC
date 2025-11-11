const BaseAgent = require('./BaseAgent');

class Agent1CodeBlockIdentifier extends BaseAgent {
  constructor() {
    super(
      '智能体1-代码块识别', 
      'agent1-prompt.txt',
      'agent1-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getCode()
    };

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Claude模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('claude-sonnet-4-5-20250929', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent1TokenUsage', result.usage);

    // 更新上下文
    context.setAgent1Output(response);

    return context;
  }
}

module.exports = Agent1CodeBlockIdentifier;
