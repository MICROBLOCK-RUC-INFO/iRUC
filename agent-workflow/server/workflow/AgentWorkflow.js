const Agent1CodeBlockIdentifier = require('../agents/Agent1CodeBlockIdentifier');
const Agent2FunctionEncapsulator = require('../agents/Agent2FunctionEncapsulator');
const Agent3PluginPacker = require('../agents/Agent3PluginPacker');
const Agent4ScriptGenerator = require('../agents/Agent4ScriptGenerator');
const Agent5ScriptCombiner = require('../agents/Agent5ScriptCombiner');
const Agent6Validator = require('../agents/Agent6Validator');
const { SyntaxValidator } = require('../utils/SyntaxValidator');
const InputData = require('../models/InputData');
const OutputData = require('../models/OutputData');
const WorkflowContext = require('../models/WorkflowContext');
const fs = require('fs');
const path = require('path');

class AgentWorkflow {
  constructor() {
    this.agent1 = new Agent1CodeBlockIdentifier();
    this.agent2 = new Agent2FunctionEncapsulator();
    this.agent3 = new Agent3PluginPacker();
    this.agent4 = new Agent4ScriptGenerator();
    this.agent5 = new Agent5ScriptCombiner();
    this.agent6 = new Agent6Validator();
    this.syntaxValidator = new SyntaxValidator();

    // 从环境变量加载最大重试次数
    this.maxRetries = parseInt(process.env.MAX_RETRIES) || 3;

    // 初始化CSV记录器
    this.csvFilePath = path.join(process.cwd(), 'workflow_stats.csv');
    this.currentRound = 0;
    this.roundData = {};
    this.initCSV();
  }

  initCSV() {
    // 创建CSV文件并写入表头
    const header = '轮次,类型,智能体1,智能体2,智能体3,智能体4,智能体5,智能体6\n';
    fs.writeFileSync(this.csvFilePath, header, 'utf8');
    console.log(`✅ CSV统计文件已创建: ${this.csvFilePath}`);
  }

  recordAgentStats(agentName, timeMs, tokenUsage) {
    // 记录当前轮次的数据
    if (!this.roundData[this.currentRound]) {
      this.roundData[this.currentRound] = {
        time: {},
        token: {}
      };
    }

    this.roundData[this.currentRound].time[agentName] = timeMs;
    this.roundData[this.currentRound].token[agentName] = tokenUsage ? tokenUsage.totalTokens : 0;
  }

  writeRoundToCSV() {
    // 将当前轮次的数据写入CSV
    const round = this.currentRound;
    const data = this.roundData[round];

    if (!data) return;

    // 写入时间行
    const timeLine = `第${round}轮,时间(ms),${data.time['智能体1'] || ''},${data.time['智能体2'] || ''},${data.time['智能体3'] || ''},${data.time['智能体4'] || ''},${data.time['智能体5'] || ''},${data.time['智能体6'] || ''}\n`;
    fs.appendFileSync(this.csvFilePath, timeLine, 'utf8');

    // 写入token行
    const tokenLine = `第${round}轮,Token数,${data.token['智能体1'] || ''},${data.token['智能体2'] || ''},${data.token['智能体3'] || ''},${data.token['智能体4'] || ''},${data.token['智能体5'] || ''},${data.token['智能体6'] || ''}\n`;
    fs.appendFileSync(this.csvFilePath, tokenLine, 'utf8');

    console.log(`📊 第${round}轮数据已写入CSV`);
  }

  async execute(inputData) {
    const context = new WorkflowContext(inputData.getCode(), inputData.getAuxiliaryInfo());
    let pluginCode;

    try {
      // 开始第一轮
      this.currentRound = 1;

      // 执行智能体1 - 代码块识别
      console.log('执行智能体1 - 代码块识别...');
      const startTime1 = Date.now();
      await this.agent1.process(context);
      const endTime1 = Date.now();
      const time1 = endTime1 - startTime1;
      console.log(`智能体1执行完成，耗时: ${time1}ms`);
      
      // 从context获取token使用信息（需要各智能体在process中设置）
      const usage1 = context.get('agent1TokenUsage');
      this.recordAgentStats('智能体1', time1, usage1);

      // 执行智能体2 - 函数封装
      console.log('执行智能体2 - 函数封装...');
      const startTime2 = Date.now();
      await this.agent2.process(context);
      const endTime2 = Date.now();
      const time2 = endTime2 - startTime2;
      console.log(`智能体2执行完成，耗时: ${time2}ms`);
      
      const usage2 = context.get('agent2TokenUsage');
      this.recordAgentStats('智能体2', time2, usage2);

      // 分叉1: 执行智能体3 - 函数打包 (生成插件代码)
      console.log('执行智能体3 - 函数打包...');
      const startTime3 = Date.now();
      await this.agent3.process(context);
      const endTime3 = Date.now();
      const time3 = endTime3 - startTime3;
      console.log(`智能体3执行完成，耗时: ${time3}ms`);
      
      const usage3 = context.get('agent3TokenUsage');
      this.recordAgentStats('智能体3', time3, usage3);
      
      // 分叉2: 执行4/5/6以及语法校验
      console.log('开始执行脚本生成与验证流程...');
      await this.processScriptGenerationAndValidation(context);

      // 合并两个分支的结果
      const finalScript = context.getAgent5Output();
      pluginCode = context.getAgent3Output(); // 从上下文中获取最终的插件代码

      // 返回最终结果: 脚本和插件代码
      return new OutputData(finalScript, pluginCode);

    } catch (error) {
      console.error('工作流执行过程中出错:', error.message);
      throw new Error(`工作流执行失败: ${error.message}`);
    }
  }

  async processScriptGenerationAndValidation(context) {
    let functionalRetries = 0;
    let functionalPassed = false;

    // 功能校验循环
    while (functionalRetries < this.maxRetries && !functionalPassed) {
      // 执行智能体4 - 脚本生成
      console.log('执行智能体4 - 脚本生成...');
      const startTime4 = Date.now();
      await this.agent4.process(context);
      const endTime4 = Date.now();
      const time4 = endTime4 - startTime4;
      console.log(`智能体4执行完成，耗时: ${time4}ms`);
      
      const usage4 = context.get('agent4TokenUsage');
      this.recordAgentStats('智能体4', time4, usage4);

      // 执行智能体5 - 脚本组合
      console.log('执行智能体5 - 脚本组合...');
      const startTime5 = Date.now();
      await this.agent5.process(context);
      const endTime5 = Date.now();
      const time5 = endTime5 - startTime5;
      console.log(`智能体5执行完成，耗时: ${time5}ms`);
      
      const usage5 = context.get('agent5TokenUsage');
      this.recordAgentStats('智能体5', time5, usage5);

      // 执行智能体6 - 功能校验
      console.log('执行智能体6 - 功能校验...');
      const startTime6 = Date.now();
      await this.agent6.process(context);
      const endTime6 = Date.now();
      const time6 = endTime6 - startTime6;
      console.log(`智能体6执行完成，耗时: ${time6}ms`);
      
      const usage6 = context.get('agent6TokenUsage');
      this.recordAgentStats('智能体6', time6, usage6);

      // 写入当前轮次数据到CSV
      this.writeRoundToCSV();

      // 检查功能校验结果
      functionalPassed = context.get('validationPassed');

      if (functionalPassed) {
        console.log('功能校验通过！');
        // 通过功能校验后进行语法校验
        await this.processSyntaxValidation(context);
        return;
      } else {
        console.log(`功能校验未通过，重试中... (${functionalRetries + 1}/${this.maxRetries})`);
        functionalRetries++;

        // 准备详细的错误信息供下一次生成使用
        const errorMessage = context.getAgent6Output();
        context.put('needRegenerate', true);
        context.put('errorMessage', errorMessage);
        context.put('errorType', '功能校验错误');

        // 保存上次生成的内容，用于比较和改进
        context.put('previousScript', context.getAgent5Output());

        // 功能验证未通过时，返回智能体1、2、3重新生成
        if (functionalRetries < this.maxRetries) {
          console.log('功能校验失败，返回智能体1、2、3重新执行...');
          
          // 进入下一轮
          this.currentRound++;
          
          // 重新执行智能体1
          console.log('重新执行智能体1 - 代码块识别...');
          const reStartTime1 = Date.now();
          await this.agent1.process(context);
          const reEndTime1 = Date.now();
          const reTime1 = reEndTime1 - reStartTime1;
          console.log(`智能体1重新执行完成，耗时: ${reTime1}ms`);
          
          const reUsage1 = context.get('agent1TokenUsage');
          this.recordAgentStats('智能体1', reTime1, reUsage1);

          // 重新执行智能体2
          console.log('重新执行智能体2 - 函数封装...');
          const reStartTime2 = Date.now();
          await this.agent2.process(context);
          const reEndTime2 = Date.now();
          const reTime2 = reEndTime2 - reStartTime2;
          console.log(`智能体2重新执行完成，耗时: ${reTime2}ms`);
          
          const reUsage2 = context.get('agent2TokenUsage');
          this.recordAgentStats('智能体2', reTime2, reUsage2);

          // 重新执行智能体3
          console.log('重新执行智能体3 - 函数打包...');
          const reStartTime3 = Date.now();
          await this.agent3.process(context);
          const reEndTime3 = Date.now();
          const reTime3 = reEndTime3 - reStartTime3;
          console.log(`智能体3重新执行完成，耗时: ${reTime3}ms`);
          
          const reUsage3 = context.get('agent3TokenUsage');
          this.recordAgentStats('智能体3', reTime3, reUsage3);
        }
      }
    }

    // 修改点2: 功能校验达到最大次数仍未通过时，以最后一次结果为准，进行语法校验
    if (!functionalPassed) {
      console.log('警告：功能校验达到最大重试次数，以最后一次结果为准，继续进行语法校验...');
      // 使用最后一次的agent5输出进行语法校验
      await this.processSyntaxValidation(context);
    }
  }

  async processSyntaxValidation(context) {
    let syntaxRetries = 0;
    let syntaxValid = false;

    // 语法校验循环
    while (syntaxRetries < this.maxRetries && !syntaxValid) {
      // 语法验证
      console.log('执行语法验证...');
      const syntaxStartTime = performance.now();
      const syntaxResult = this.syntaxValidator.validate(context.getAgent5Output());
      const syntaxEndTime = performance.now();
      const durationMs = syntaxEndTime - syntaxStartTime;
      console.log(`语法验证完成，耗时: ${durationMs.toFixed(3)}ms (${(durationMs * 1000).toFixed(2)}μs)`);


      if (syntaxResult.isValid()) {
        syntaxValid = true;
        console.log('语法验证通过！');
        return;
      } else {
        console.log('语法验证未通过:');
        syntaxResult.getErrors().forEach(error => console.log(`- ${error}`));

        syntaxRetries++;

        if (syntaxRetries < this.maxRetries) {
          // 准备详细的语法错误信息供重新生成使用
          let detailedErrorInfo = '语法验证错误详情:\n';

          syntaxResult.getErrors().forEach(error => {
            detailedErrorInfo += `- ${error}\n`;
          });

          // 添加当前脚本内容，以便智能体4能看到问题所在
          detailedErrorInfo += '\n当前有问题的脚本:\n';
          detailedErrorInfo += context.getAgent5Output() + '\n';

          // 标记需要针对语法问题重新生成
          context.put('needRegenerate', true);
          context.put('errorMessage', detailedErrorInfo);
          context.put('errorType', '语法错误');

          // 保存问题脚本供对比
          context.put('previousScript', context.getAgent5Output());

          console.log('正在根据语法错误反馈重新生成脚本...');

          // 进入下一轮（语法错误修复）
          this.currentRound++;

          // 重新执行智能体4和5，不再经过智能体6
          console.log(`重新执行智能体4 - 脚本生成... (${syntaxRetries}/${this.maxRetries})`);
          const reStartTime4 = Date.now();
          await this.agent4.process(context);
          const reEndTime4 = Date.now();
          const reTime4 = reEndTime4 - reStartTime4;
          console.log(`智能体4重新执行完成，耗时: ${reTime4}ms`);
          
          const reUsage4 = context.get('agent4TokenUsage');
          this.recordAgentStats('智能体4', reTime4, reUsage4);

          console.log('重新执行智能体5 - 脚本组合...');
          const reStartTime5 = Date.now();
          await this.agent5.process(context);
          const reEndTime5 = Date.now();
          const reTime5 = reEndTime5 - reStartTime5;
          console.log(`智能体5重新执行完成，耗时: ${reTime5}ms`);
          
          const reUsage5 = context.get('agent5TokenUsage');
          this.recordAgentStats('智能体5', reTime5, reUsage5);

          // 写入当前轮次数据到CSV
          this.writeRoundToCSV();
        }
      }
    }

    if (!syntaxValid) {
      console.log('警告：语法验证达到最大重试次数，使用最后一次生成结果作为输出');
    }
  }
}

module.exports = AgentWorkflow;
