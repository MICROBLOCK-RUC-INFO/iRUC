class InputData {
    constructor(code, auxiliaryInfo) {
      this.code = code;
      this.auxiliaryInfo = auxiliaryInfo;
    }
  
    getCode() {
      return this.code;
    }
  
    getAuxiliaryInfo() {
      return this.auxiliaryInfo;
    }
  
    // 验证输入数据
    validate() {
      const errors = [];
      
      if (!this.code || typeof this.code !== 'string' || this.code.trim().length === 0) {
        errors.push('代码内容不能为空');
      }
      
      if (this.auxiliaryInfo && typeof this.auxiliaryInfo !== 'string') {
        errors.push('辅助信息必须为字符串类型');
      }
      
      return {
        isValid: errors.length === 0,
        errors
      };
    }
  
    // 转换为JSON
    toJSON() {
      return {
        code: this.code,
        auxiliaryInfo: this.auxiliaryInfo
      };
    }
  
    // 从JSON创建实例
    static fromJSON(json) {
      return new InputData(json.code, json.auxiliaryInfo);
    }
  }
  
  module.exports = InputData;
  