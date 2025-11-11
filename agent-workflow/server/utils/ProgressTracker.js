class ProgressTracker {
    constructor() {
      this.listeners = [];
      this.currentStatus = {
        agentName: null,
        message: '',
        progress: 0,
        timestamp: null
      };
    }
  
    static getInstance() {
      if (!ProgressTracker.instance) {
        ProgressTracker.instance = new ProgressTracker();
      }
      return ProgressTracker.instance;
    }
  
    registerStatusListener(listener) {
      if (typeof listener !== 'function') {
        throw new Error('监听器必须是一个函数');
      }
      this.listeners.push(listener);
    }
  
    removeStatusListener(listener) {
      const index = this.listeners.indexOf(listener);
      if (index > -1) {
        this.listeners.splice(index, 1);
      }
    }
  
    updateProgress(agentName, message, progress) {
      this.currentStatus = {
        agentName,
        message,
        progress: Math.max(0, Math.min(1, progress)), // 确保进度在0-1之间
        timestamp: new Date().toISOString()
      };
  
      // 通知所有监听器
      this.listeners.forEach(listener => {
        try {
          listener(agentName, message, progress);
        } catch (error) {
          console.error('进度监听器执行错误:', error);
        }
      });
    }
  
    getCurrentStatus() {
      return { ...this.currentStatus };
    }
  
    reset() {
      this.currentStatus = {
        agentName: null,
        message: '',
        progress: 0,
        timestamp: null
      };
    }
  
    // 清理所有监听器
    clearListeners() {
      this.listeners = [];
    }
  }
  
  module.exports = ProgressTracker;
  