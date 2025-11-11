const { spawn } = require('child_process');
const fs = require('fs');
const path = require('path');

// 检查Flowise是否已全局安装
function checkFlowiseInstallation() {
  return new Promise((resolve) => {
    const checkProcess = spawn('flowise', ['--version'], { stdio: 'pipe' });
    
    checkProcess.on('close', (code) => {
      resolve(code === 0);
    });
    
    checkProcess.on('error', () => {
      resolve(false);
    });
  });
}

// 安装Flowise
function installFlowise() {
  return new Promise((resolve, reject) => {
    console.log('正在安装Flowise...');
    
    const installProcess = spawn('npm', ['install', '-g', 'flowise'], { 
      stdio: 'inherit',
      shell: true 
    });
    
    installProcess.on('close', (code) => {
      if (code === 0) {
        console.log('Flowise安装完成');
        resolve();
      } else {
        reject(new Error(`Flowise安装失败，退出码: ${code}`));
      }
    });
    
    installProcess.on('error', (error) => {
      reject(error);
    });
  });
}

// 启动Flowise
function startFlowise() {
  return new Promise((resolve, reject) => {
    console.log('正在启动Flowise...');
    
    // 设置环境变量
    const env = {
      ...process.env,
      PORT: '3001',
      FLOWISE_USERNAME: 'admin',
      FLOWISE_PASSWORD: '1234',
      DATABASE_TYPE: 'sqlite',
      DATABASE_PATH: './flowise.db'
    };
    
    const flowiseProcess = spawn('flowise', ['start'], { 
      stdio: 'inherit',
      env: env,
      shell: true 
    });
    
    flowiseProcess.on('close', (code) => {
      console.log(`Flowise进程结束，退出码: ${code}`);
      resolve(code);
    });
    
    flowiseProcess.on('error', (error) => {
      reject(error);
    });
    
    // 处理进程信号
    process.on('SIGINT', () => {
      console.log('\n正在关闭Flowise...');
      flowiseProcess.kill('SIGINT');
    });
    
    process.on('SIGTERM', () => {
      console.log('\n正在关闭Flowise...');
      flowiseProcess.kill('SIGTERM');
    });
  });
}

// 主函数
async function main() {
  try {
    console.log('检查Flowise安装状态...');
    
    const isInstalled = await checkFlowiseInstallation();
    
    if (!isInstalled) {
      await installFlowise();
    } else {
      console.log('Flowise已安装');
    }
    
    await startFlowise();
    
  } catch (error) {
    console.error('启动Flowise失败:', error.message);
    process.exit(1);
  }
}

if (require.main === module) {
  main();
}

module.exports = { main };
