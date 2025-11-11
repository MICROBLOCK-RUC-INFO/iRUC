const fs = require('fs');
const path = require('path');

class SyntaxValidator {
  constructor() {
    // 加载PEG解析器
    this.loadParser();
  }

  loadParser() {
    try {
      const parserPath = path.join(__dirname, '../../grammar/parser.js');
      if (fs.existsSync(parserPath)) {
        this.parser = require(parserPath);
      } else {
        console.warn('PEG解析器未找到，使用基础验证模式');
        this.parser = null;
      }
    } catch (error) {
      console.error('加载PEG解析器失败:', error);
      this.parser = null;
    }
  }

  validate(script) {
    const errors = [];
    const suggestions = [];

    try {
      if (this.parser) {
        // 使用PEG解析器进行语法验证
        return this.validateWithPEG(script);
      } else {
        // 回退到基础验证
        return this.validateBasic(script);
      }
    } catch (error) {
      errors.push(`语法验证过程中发生错误: ${error.message}`);
      return new ValidationResult(false, errors, suggestions);
    }
  }

  validateWithPEG(script) {
    const errors = [];
    const suggestions = [];

    try {
      // 使用PEG解析器解析脚本
      const parseResult = this.parser.parse(script);
      
      // 如果解析成功，进行语义验证
      const semanticValidation = this.validateSemantics(parseResult);
      
      return new ValidationResult(
        semanticValidation.errors.length === 0,
        semanticValidation.errors,
        semanticValidation.suggestions
      );
    } catch (parseError) {
      // PEG解析失败，提供详细错误信息
      const errorLocation = this.extractErrorLocation(parseError, script);
      errors.push(`语法解析错误: ${parseError.message}`);
      
      if (errorLocation) {
        errors.push(`错误位置: 第${errorLocation.line}行，第${errorLocation.column}列`);
        suggestions.push(`检查第${errorLocation.line}行附近的语法是否正确`);
      }
      
      return new ValidationResult(false, errors, suggestions);
    }
  }

  validateSemantics(parseTree) {
    const errors = [];
    const suggestions = [];

    // 验证变量命名规范
    this.validateVariableNaming(parseTree, errors, suggestions);
    
    // 验证语句完整性
    this.validateStatementCompleteness(parseTree, errors, suggestions);
    
    // 验证数据流一致性
    this.validateDataFlow(parseTree, errors, suggestions);

    return { errors, suggestions };
  }

  validateVariableNaming(parseTree, errors, suggestions) {
    const variablePattern = /^[a-zA-Z][a-zA-Z0-9]*\d{8}$/;
    
    // 遍历解析树寻找变量声明
    this.traverseParseTree(parseTree, (node) => {
      if (node.type === 'variable_declaration' && node.name) {
        if (!variablePattern.test(node.name)) {
          errors.push(`变量名 '${node.name}' 不符合规范，应为'变量名+8位随机数字'格式`);
          suggestions.push(`将变量名 '${node.name}' 修改为类似 '${node.name}${this.generateRandomDigits(8)}' 的格式`);
        }
      }
    });
  }

  validateStatementCompleteness(parseTree, errors, suggestions) {
    this.traverseParseTree(parseTree, (node) => {
      if (node.type === 'statement' && !node.terminated) {
        errors.push(`语句缺少结束符: ${node.content}`);
        suggestions.push(`在语句 '${node.content}' 末尾添加分号`);
      }
    });
  }

  validateDataFlow(parseTree, errors, suggestions) {
    const declaredVariables = new Set();
    const usedVariables = new Set();

    // 收集变量声明和使用
    this.traverseParseTree(parseTree, (node) => {
      if (node.type === 'variable_declaration') {
        declaredVariables.add(node.name);
      } else if (node.type === 'variable_usage') {
        usedVariables.add(node.name);
      }
    });

    // 检查未声明的变量使用
    usedVariables.forEach(varName => {
      if (!declaredVariables.has(varName)) {
        errors.push(`使用了未声明的变量: ${varName}`);
        suggestions.push(`确保变量 '${varName}' 在使用前已被声明`);
      }
    });
  }

  traverseParseTree(node, callback) {
    if (!node || typeof node !== 'object') return;
    
    callback(node);
    
    // 递归遍历子节点
    Object.values(node).forEach(child => {
      if (Array.isArray(child)) {
        child.forEach(item => this.traverseParseTree(item, callback));
      } else if (typeof child === 'object') {
        this.traverseParseTree(child, callback);
      }
    });
  }

  validateBasic(script) {
    const errors = [];
    const suggestions = [];

    // 基础语法检查
    this.checkServiceDefinition(script, errors, suggestions);
    this.checkVariableNaming(script, errors, suggestions);
    this.checkStatementTerminators(script, errors, suggestions);
    this.checkGraphQLSyntax(script, errors, suggestions);

    return new ValidationResult(errors.length === 0, errors, suggestions);
  }

  checkServiceDefinition(script, errors, suggestions) {
    const servicePattern = /service\s+([a-zA-Z][a-zA-Z0-9-]*)\s*\{/;
    if (!servicePattern.test(script)) {
      errors.push('脚本必须包含有效的service定义');
      suggestions.push('添加正确格式的service定义，例如：service order-rpc { ... }');
    }
  }

  checkVariableNaming(script, errors, suggestions) {
    const variablePattern = /new\s+([a-zA-Z][a-zA-Z0-9]*\d{8})\s*=/g;
    const invalidVariables = [];
    
    let match;
    while ((match = variablePattern.exec(script)) !== null) {
      const varName = match[1];
      if (!/^[a-zA-Z][a-zA-Z0-9]*\d{8}$/.test(varName)) {
        invalidVariables.push(varName);
      }
    }

    invalidVariables.forEach(varName => {
      errors.push(`变量名 '${varName}' 不符合规范`);
      suggestions.push(`将变量名修改为符合'变量名+8位随机数字'格式`);
    });
  }

  checkStatementTerminators(script, errors, suggestions) {
    const lines = script.split('\n');
    lines.forEach((line, index) => {
      const trimmedLine = line.trim();
      if (trimmedLine && 
          !trimmedLine.endsWith(';') && 
          !trimmedLine.endsWith('{') && 
          !trimmedLine.endsWith('}') &&
          !trimmedLine.startsWith('//') &&
          !trimmedLine.startsWith('/*') &&
          !trimmedLine.endsWith('*/')) {
        errors.push(`第${index + 1}行缺少语句结束符: ${trimmedLine}`);
        suggestions.push(`在第${index + 1}行末尾添加分号`);
      }
    });
  }

  checkGraphQLSyntax(script, errors, suggestions) {
    const gqlPattern = /gql\s+(query|mutation)\s*\{([^}]*)\}/g;
    let match;
    
    while ((match = gqlPattern.exec(script)) !== null) {
      const operation = match[1];
      const body = match[2].trim();
      
      if (!body) {
        errors.push(`GraphQL ${operation} 主体为空`);
        suggestions.push(`为GraphQL ${operation} 添加具体的查询/变更内容`);
      }
    }
  }

  extractErrorLocation(error, script) {
    if (error.location) {
      return {
        line: error.location.start.line,
        column: error.location.start.column
      };
    }
    return null;
  }

  generateRandomDigits(length) {
    let result = '';
    for (let i = 0; i < length; i++) {
      result += Math.floor(Math.random() * 10);
    }
    return result;
  }
}

class ValidationResult {
  constructor(valid, errors, suggestions = []) {
    this.valid = valid;
    this.errors = errors;
    this.suggestions = suggestions;
  }

  isValid() {
    return this.valid;
  }

  getErrors() {
    return this.errors;
  }

  getSuggestions() {
    return this.suggestions;
  }

  getErrorMessage() {
    let message = '语法验证错误:\n';
    this.errors.forEach(error => {
      message += `- ${error}\n`;
    });
    
    if (this.suggestions.length > 0) {
      message += '\n修改建议:\n';
      this.suggestions.forEach(suggestion => {
        message += `- ${suggestion}\n`;
      });
    }
    
    return message;
  }
}

module.exports = { SyntaxValidator, ValidationResult };
