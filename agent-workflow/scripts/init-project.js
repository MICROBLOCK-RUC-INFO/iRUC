const fs = require('fs');
const path = require('path');

// 需要创建的目录
const directories = [
  'server/config',
  'server/models',
  'server/agents',
  'server/utils',
  'server/workflow',
  'server/routes',
  'server/system-prompts',
  'grammar',
  'flowise/chatflows',
  'flowise/config',
  'data',
  'logs',
  'public',
  'scripts'
];

// 创建目录
directories.forEach(dir => {
  const fullPath = path.join(process.cwd(), dir);
  if (!fs.existsSync(fullPath)) {
    fs.mkdirSync(fullPath, { recursive: true });
    console.log(`创建目录: ${dir}`);
  } else {
    console.log(`目录已存在: ${dir}`);
  }
});

console.log('项目目录结构初始化完成!');
