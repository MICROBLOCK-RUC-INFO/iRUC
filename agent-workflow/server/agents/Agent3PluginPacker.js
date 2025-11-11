const BaseAgent = require('./BaseAgent');

class Agent3PluginPacker extends BaseAgent {
  constructor() {
    super(
      '智能体3-函数打包', 
      'agent3-prompt.txt',
      'agent3-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getAgent2Output()
    };

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Claude模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('claude-sonnet-4-5-20250929', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent3TokenUsage', result.usage);

    // 更新上下文
    context.setAgent3Output(response);

    return context;
  }
}

module.exports = Agent3PluginPacker;
