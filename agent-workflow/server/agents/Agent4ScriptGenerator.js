const BaseAgent = require('./BaseAgent');

class Agent4ScriptGenerator extends BaseAgent {
  constructor() {
    super(
      '智能体4-脚本生成', 
      'agent4-prompt.txt',
      'agent4-examples.json'
    );
  }

  async process(context) {
    const additionalInfo = {
      mainContent: context.getAgent2Output()
    };

    // 如果是重新生成，添加详细的错误信息和上次生成内容
    if (context.get('needRegenerate')) {
      const errorType = context.get('errorType');
      const errorMessage = context.get('errorMessage');
      const previousScript = context.get('previousScript');

      // 构建更详细的错误反馈信息
      let errorFeedback = `===== ${errorType} =====\n`;
      errorFeedback += errorMessage + '\n\n';

      if (previousScript) {
        errorFeedback += '===== 上次生成的脚本 =====\n';
        errorFeedback += previousScript + '\n\n';
      }

      errorFeedback += '请针对上述问题进行修改，特别注意：\n';

      if (errorType === '语法错误') {
        errorFeedback += '1. 变量命名格式：应为\'变量名+8位随机数字\'格式\n';
        errorFeedback += '2. 每条语句末尾必须添加分号\n';
        errorFeedback += '3. GraphQL查询必须使用正确的syntax\n';
        errorFeedback += '4. 数据对齐语句左侧必须使用\'服务名.变量名\'格式\n';
        errorFeedback += '5. 插件调用格式应为\'插件名.扩展名/函数名(参数)\'\n';
      } else {
        errorFeedback += '1. 确保脚本逻辑与原代码功能等价\n';
        errorFeedback += '2. 检查数据流动和变量转换是否正确\n';
        errorFeedback += '3. 确保所有输入/输出结构与原微服务一致\n';
      }

      additionalInfo['错误反馈'] = errorFeedback;
    }

    const userMessage = this.createPrompt(context, additionalInfo);

    // 使用Claude模型名称，接收包含content和usage的结果
    const result = await this.executeAgent('claude-sonnet-4-5-20250929', userMessage);

    // 提取响应内容
    const response = result.content;

    // 将token使用信息存入context
    context.put('agent4TokenUsage', result.usage);

    // 更新上下文
    context.setAgent4Output(response);
    context.put('needRegenerate', false);

    return context;
  }
}

module.exports = Agent4ScriptGenerator;
