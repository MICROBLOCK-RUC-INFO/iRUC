// test-api-detailed.js
require('dotenv').config();
const axios = require('axios');

async function testApiDetailed() {
  console.log('='.repeat(60));
  console.log('API 连接详细测试');
  console.log('='.repeat(60));
  
  // 1. 检查环境变量
  console.log('\n【步骤1】检查环境变量:');
  const apiUrl = process.env.CLAUDE_API_URL;
  const apiKey = process.env.CLAUDE_API_KEY;
  
  console.log('✓ API URL:', apiUrl);
  console.log('✓ API Key:', apiKey ? `存在 (前10字符: ${apiKey.substring(0, 10)}...)` : '❌ 未设置');
  
  if (!apiKey) {
    console.error('\n❌ 错误: CLAUDE_API_KEY 未设置，请检查 .env 文件');
    return;
  }

  // 2. 测试不同的 max_tokens 值
  const testCases = [
    { name: '小值测试 (100 tokens)', max_tokens: 100 },
    { name: '中值测试 (1000 tokens)', max_tokens: 1000 },
    { name: '大值测试 (4096 tokens)', max_tokens: 4096 },
    { name: '超大值测试 (8192 tokens)', max_tokens: 8192 }
  ];

  for (const testCase of testCases) {
    console.log(`\n【步骤2】${testCase.name}:`);
    
    const requestBody = {
      model: 'claude-sonnet-4-5-20250929',
      messages: [
        {
          role: 'system',
          content: '你是一个helpful助手'
        },
        {
          role: 'user',
          content: '请简单说"你好"'
        }
      ],
      temperature: 0.1,
      max_tokens: testCase.max_tokens
    };

    try {
      const startTime = Date.now();
      const response = await axios.post(apiUrl, requestBody, {
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${apiKey}`
        },
        timeout: 60000,
        validateStatus: (status) => true // 接受所有状态码
      });
      const endTime = Date.now();

      if (response.status === 200) {
        console.log(`✅ 成功! 耗时: ${endTime - startTime}ms`);
        console.log('响应内容:', response.data.choices[0].message.content);
        console.log('Token使用:', response.data.usage);
      } else {
        console.log(`❌ 失败! 状态码: ${response.status}`);
        console.log('响应:', JSON.stringify(response.data, null, 2));
        break; // 如果失败就停止后续测试
      }
    } catch (error) {
      console.log(`❌ 异常!`);
      if (error.response) {
        console.log('状态码:', error.response.status);
        console.log('响应:', JSON.stringify(error.response.data, null, 2));
      } else {
        console.log('错误:', error.message);
      }
      break; // 如果异常就停止后续测试
    }

    // 等待1秒再进行下一个测试
    await new Promise(resolve => setTimeout(resolve, 1000));
  }

  console.log('\n' + '='.repeat(60));
  console.log('测试完成');
  console.log('='.repeat(60));
}

testApiDetailed();
