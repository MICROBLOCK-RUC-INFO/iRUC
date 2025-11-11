class WorkflowContext {
    constructor(code, auxiliaryInfo) {
      this.code = code;
      this.auxiliaryInfo = auxiliaryInfo;
      this.agent1Output = null;
      this.agent2Output = null;
      this.agent3Output = null;
      this.agent4Output = null;
      this.agent5Output = null;
      this.agent6Output = null;
      this.additionalData = new Map();
      this.createdAt = new Date();
    }
  
    // Getters
    getCode() { return this.code; }
    getAuxiliaryInfo() { return this.auxiliaryInfo; }
    getAgent1Output() { return this.agent1Output; }
    getAgent2Output() { return this.agent2Output; }
    getAgent3Output() { return this.agent3Output; }
    getAgent4Output() { return this.agent4Output; }
    getAgent5Output() { return this.agent5Output; }
    getAgent6Output() { return this.agent6Output; }
  
    // Setters
    setCode(code) { this.code = code; }
    setAuxiliaryInfo(auxiliaryInfo) { this.auxiliaryInfo = auxiliaryInfo; }
    setAgent1Output(output) { this.agent1Output = output; }
    setAgent2Output(output) { this.agent2Output = output; }
    setAgent3Output(output) { this.agent3Output = output; }
    setAgent4Output(output) { this.agent4Output = output; }
    setAgent5Output(output) { this.agent5Output = output; }
    setAgent6Output(output) { this.agent6Output = output; }
  
    // 额外数据管理
    put(key, value) {
      this.additionalData.set(key, value);
    }
  
    get(key) {
      return this.additionalData.get(key);
    }
  
    has(key) {
      return this.additionalData.has(key);
    }
  
    remove(key) {
      return this.additionalData.delete(key);
    }
  
    // 获取所有额外数据
    getAdditionalData() {
      return Object.fromEntries(this.additionalData);
    }
  
    // 清空上下文
    clear() {
      this.agent1Output = null;
      this.agent2Output = null;
      this.agent3Output = null;
      this.agent4Output = null;
      this.agent5Output = null;
      this.agent6Output = null;
      this.additionalData.clear();
    }
  
    // 转换为JSON
    toJSON() {
      return {
        code: this.code,
        auxiliaryInfo: this.auxiliaryInfo,
        agent1Output: this.agent1Output,
        agent2Output: this.agent2Output,
        agent3Output: this.agent3Output,
        agent4Output: this.agent4Output,
        agent5Output: this.agent5Output,
        agent6Output: this.agent6Output,
        additionalData: this.getAdditionalData(),
        createdAt: this.createdAt.toISOString()
      };
    }
  
    // 从JSON创建实例
    static fromJSON(json) {
      const context = new WorkflowContext(json.code, json.auxiliaryInfo);
      context.agent1Output = json.agent1Output;
      context.agent2Output = json.agent2Output;
      context.agent3Output = json.agent3Output;
      context.agent4Output = json.agent4Output;
      context.agent5Output = json.agent5Output;
      context.agent6Output = json.agent6Output;
      
      if (json.additionalData) {
        Object.entries(json.additionalData).forEach(([key, value]) => {
          context.put(key, value);
        });
      }
      
      if (json.createdAt) {
        context.createdAt = new Date(json.createdAt);
      }
      
      return context;
    }
  }
  
  module.exports = WorkflowContext;
  