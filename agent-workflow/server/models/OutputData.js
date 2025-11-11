class OutputData {
    constructor(script, pluginCode) {
      this.script = script;
      this.pluginCode = pluginCode;
      this.timestamp = new Date();
    }
  
    getScript() {
      return this.script;
    }
  
    getPluginCode() {
      return this.pluginCode;
    }
  
    getTimestamp() {
      return this.timestamp;
    }
  
    // 转换为JSON
    toJSON() {
      return {
        script: this.script,
        pluginCode: this.pluginCode,
        timestamp: this.timestamp.toISOString()
      };
    }
  
    // 从JSON创建实例
    static fromJSON(json) {
      const outputData = new OutputData(json.script, json.pluginCode);
      if (json.timestamp) {
        outputData.timestamp = new Date(json.timestamp);
      }
      return outputData;
    }
  
    // 验证输出数据
    validate() {
      const errors = [];
      
      if (!this.script || typeof this.script !== 'string') {
        errors.push('脚本内容不能为空');
      }
      
      if (!this.pluginCode || typeof this.pluginCode !== 'string') {
        errors.push('插件代码不能为空');
      }
      
      return {
        isValid: errors.length === 0,
        errors
      };
    }
  }
  
  module.exports = OutputData;
  