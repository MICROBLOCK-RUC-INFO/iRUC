const BaseAgent = require('./BaseAgent');

class Agent2FunctionEncapsulator extends BaseAgent {
  constructor() {
    super(
      '智能体2-函数封装', 
      'agent2-prompt.txt',
      'agent2-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getAgent1Output()
    };

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Claude模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('claude-sonnet-4-5-20250929', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent2TokenUsage', result.usage);

    // 更新上下文
    context.setAgent2Output(response);

    return context;
  }
}

module.exports = Agent2FunctionEncapsulator;
