const peg = require('pegjs');
const fs = require('fs');
const path = require('path');

function buildParser() {
  try {
    console.log('开始构建PEG解析器...');
    
    // 读取语法文件
    const grammarPath = path.join(__dirname, 'syntax.pegjs');
    
    if (!fs.existsSync(grammarPath)) {
      console.error(`语法文件不存在: ${grammarPath}`);
      process.exit(1);
    }
    
    const grammar = fs.readFileSync(grammarPath, 'utf-8');
    console.log('语法文件读取成功');
    
    // 生成解析器
    console.log('正在生成解析器...');
    const parser = peg.generate(grammar, {
      output: 'source',
      format: 'commonjs',
      optimize: 'speed',
      cache: true
    });
    
    // 创建解析器文件内容
    const parserContent = `// 自动生成的解析器文件，请勿手动编辑
// 生成时间: ${new Date().toISOString()}

${parser}

// 导出解析器
module.exports = {
  parse: peg$parse,
  SyntaxError: peg$SyntaxError
};
`;
    
    // 写入解析器文件
    const parserPath = path.join(__dirname, 'parser.js');
    fs.writeFileSync(parserPath, parserContent, 'utf-8');
    
    console.log('PEG解析器构建成功!');
    console.log(`解析器文件保存至: ${parserPath}`);
    
    // 测试解析器
    testParser(parserPath);
    
  } catch (error) {
    console.error('构建PEG解析器失败:', error.message);
    if (error.location) {
      console.error(`错误位置: 第${error.location.start.line}行，第${error.location.start.column}列`);
    }
    process.exit(1);
  }
}

function testParser(parserPath) {
  try {
    console.log('\n正在测试解析器...');
    
    // 加载生成的解析器
    delete require.cache[require.resolve(parserPath)];
    const parser = require(parserPath);
    
    // 测试用例
    const testScript = `service order-rpc {
    new order12345678 = gql query { getOrder };
    
    new result12345678 = myorder.exe/processOrder(order12345678);
    
    output order-rpc.result = result12345678;
    
    return result12345678;
}`;
    
    const result = parser.parse(testScript);
    console.log('✓ 解析器测试通过');
    console.log('解析结果结构:', JSON.stringify(result, null, 2));
    
  } catch (testError) {
    console.warn('⚠ 解析器测试失败，但构建成功:', testError.message);
    if (testError.location) {
      console.warn(`测试错误位置: 第${testError.location.start.line}行，第${testError.location.start.column}列`);
    }
  }
}

// 如果直接运行此脚本，则构建解析器
if (require.main === module) {
  buildParser();
}

module.exports = { buildParser };
