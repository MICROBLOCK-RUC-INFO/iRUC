const fs = require('fs');
const path = require('path');
const winston = require('winston');

class DatabaseConfig {
  constructor() {
    this.logger = winston.createLogger({
      level: 'info',
      format: winston.format.json(),
      transports: [
        new winston.transports.Console()
      ]
    });

    this.config = {
      // SQLite配置（默认）
      sqlite: {
        type: 'sqlite',
        database: process.env.DATABASE_PATH || path.join(__dirname, '../../data/workflow.db'),
        synchronize: process.env.NODE_ENV === 'development',
        logging: process.env.NODE_ENV === 'development'
      },
      
      // PostgreSQL配置
      postgres: {
        type: 'postgres',
        host: process.env.DB_HOST || 'localhost',
        port: parseInt(process.env.DB_PORT) || 5432,
        username: process.env.DB_USERNAME || 'workflow_user',
        password: process.env.DB_PASSWORD || 'password',
        database: process.env.DB_NAME || 'workflow_db',
        synchronize: process.env.NODE_ENV === 'development',
        logging: process.env.NODE_ENV === 'development',
        ssl: process.env.DB_SSL === 'true' ? { rejectUnauthorized: false } : false
      },

      // MySQL配置
      mysql: {
        type: 'mysql',
        host: process.env.DB_HOST || 'localhost',
        port: parseInt(process.env.DB_PORT) || 3306,
        username: process.env.DB_USERNAME || 'workflow_user',
        password: process.env.DB_PASSWORD || 'password',
        database: process.env.DB_NAME || 'workflow_db',
        synchronize: process.env.NODE_ENV === 'development',
        logging: process.env.NODE_ENV === 'development'
      }
    };

    this.currentConfig = this.getCurrentConfig();
    this.initializeDatabase();
  }

  getCurrentConfig() {
    const dbType = process.env.DATABASE_TYPE || 'sqlite';
    
    if (!this.config[dbType]) {
      this.logger.warn(`不支持的数据库类型: ${dbType}，使用默认SQLite配置`);
      return this.config.sqlite;
    }
    
    return this.config[dbType];
  }

  initializeDatabase() {
    try {
      if (this.currentConfig.type === 'sqlite') {
        this.initializeSQLite();
      } else {
        this.initializeOtherDatabase();
      }
    } catch (error) {
      this.logger.error('数据库初始化失败:', error);
      throw error;
    }
  }

  initializeSQLite() {
    // 确保SQLite数据库目录存在
    const dbPath = this.currentConfig.database;
    const dbDir = path.dirname(dbPath);
    
    if (!fs.existsSync(dbDir)) {
      fs.mkdirSync(dbDir, { recursive: true });
      this.logger.info(`创建数据库目录: ${dbDir}`);
    }

    // 创建基本表结构
    this.createBasicTables();
  }

  initializeOtherDatabase() {
    this.logger.info(`初始化${this.currentConfig.type}数据库连接`);
    // 这里可以添加其他数据库的初始化逻辑
  }

  createBasicTables() {
    // 创建工作流执行记录表
    const workflowTable = `
      CREATE TABLE IF NOT EXISTS workflow_executions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        execution_id TEXT UNIQUE NOT NULL,
        input_code TEXT NOT NULL,
        auxiliary_info TEXT,
        status TEXT DEFAULT 'running',
        current_agent TEXT,
        progress REAL DEFAULT 0.0,
        error_message TEXT,
        result_script TEXT,
        result_plugin_code TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        completed_at DATETIME
      );
    `;

    // 创建智能体执行记录表
    const agentTable = `
      CREATE TABLE IF NOT EXISTS agent_executions (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        execution_id TEXT NOT NULL,
        agent_name TEXT NOT NULL,
        agent_order INTEGER NOT NULL,
        input_data TEXT,
        output_data TEXT,
        status TEXT DEFAULT 'pending',
        error_message TEXT,
        execution_time_ms INTEGER,
        started_at DATETIME,
        completed_at DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (execution_id) REFERENCES workflow_executions(execution_id)
      );
    `;

    // 创建配置表
    const configTable = `
      CREATE TABLE IF NOT EXISTS system_config (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        config_key TEXT UNIQUE NOT NULL,
        config_value TEXT NOT NULL,
        description TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
      );
    `;

    // 创建日志表
    const logTable = `
      CREATE TABLE IF NOT EXISTS execution_logs (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        execution_id TEXT NOT NULL,
        level TEXT NOT NULL,
        message TEXT NOT NULL,
        metadata TEXT,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (execution_id) REFERENCES workflow_executions(execution_id)
      );
    `;

    const tables = [workflowTable, agentTable, configTable, logTable];
    
    if (this.currentConfig.type === 'sqlite') {
      const sqlite3 = require('sqlite3');
      const db = new sqlite3.Database(this.currentConfig.database);
      
      tables.forEach(tableSQL => {
        db.run(tableSQL, (err) => {
          if (err) {
            this.logger.error('创建表失败:', err);
          }
        });
      });
      
      // 插入默认配置
      this.insertDefaultConfig(db);
      
      db.close();
    }
  }

  insertDefaultConfig(db) {
    const defaultConfigs = [
      {
        key: 'max_retries',
        value: '3',
        description: '最大重试次数'
      },
      {
        key: 'timeout_seconds',
        value: '90',
        description: 'API调用超时时间（秒）'
      },
      {
        key: 'claude_model',
        value: 'claude-3-7-sonnet-20250219',
        description: 'Claude模型名称'
      },
      {
        key: 'gemini_model',
        value: 'gemini-2.5-pro-exp-03-25',
        description: 'Gemini模型名称'
      },
      {
        key: 'openai_model',
        value: 'gpt-4',
        description: 'OpenAI模型名称'
      }
    ];

    const insertSQL = `
      INSERT OR IGNORE INTO system_config (config_key, config_value, description)
      VALUES (?, ?, ?)
    `;

    defaultConfigs.forEach(config => {
      db.run(insertSQL, [config.key, config.value, config.description], (err) => {
        if (err) {
          this.logger.error('插入默认配置失败:', err);
        }
      });
    });
  }

  getConnection() {
    return this.currentConfig;
  }

  // 工作流数据访问方法
  async saveWorkflowExecution(executionData) {
    if (this.currentConfig.type === 'sqlite') {
      return this.saveWorkflowExecutionSQLite(executionData);
    }
    // 其他数据库类型的实现
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async saveWorkflowExecutionSQLite(executionData) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        INSERT INTO workflow_executions 
        (execution_id, input_code, auxiliary_info, status, current_agent, progress)
        VALUES (?, ?, ?, ?, ?, ?)
      `;
      
      const params = [
        executionData.executionId,
        executionData.inputCode,
        executionData.auxiliaryInfo,
        executionData.status || 'running',
        executionData.currentAgent,
        executionData.progress || 0.0
      ];
      
      db.run(sql, params, function(err) {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve({ id: this.lastID, executionId: executionData.executionId });
        }
      });
    });
  }

  async updateWorkflowExecution(executionId, updateData) {
    if (this.currentConfig.type === 'sqlite') {
      return this.updateWorkflowExecutionSQLite(executionId, updateData);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async updateWorkflowExecutionSQLite(executionId, updateData) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const setParts = [];
      const params = [];
      
      Object.entries(updateData).forEach(([key, value]) => {
        setParts.push(`${key} = ?`);
        params.push(value);
      });
      
      setParts.push('updated_at = CURRENT_TIMESTAMP');
      params.push(executionId);
      
      const sql = `
        UPDATE workflow_executions 
        SET ${setParts.join(', ')}
        WHERE execution_id = ?
      `;
      
      db.run(sql, params, function(err) {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve({ changes: this.changes });
        }
      });
    });
  }

  async getWorkflowExecution(executionId) {
    if (this.currentConfig.type === 'sqlite') {
      return this.getWorkflowExecutionSQLite(executionId);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async getWorkflowExecutionSQLite(executionId) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        SELECT * FROM workflow_executions 
        WHERE execution_id = ?
      `;
      
      db.get(sql, [executionId], (err, row) => {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve(row);
        }
      });
    });
  }

  async saveAgentExecution(agentData) {
    if (this.currentConfig.type === 'sqlite') {
      return this.saveAgentExecutionSQLite(agentData);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async saveAgentExecutionSQLite(agentData) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        INSERT INTO agent_executions 
        (execution_id, agent_name, agent_order, input_data, output_data, status, execution_time_ms, started_at, completed_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
      `;
      
      const params = [
        agentData.executionId,
        agentData.agentName,
        agentData.agentOrder,
        agentData.inputData,
        agentData.outputData,
        agentData.status,
        agentData.executionTimeMs,
        agentData.startedAt,
        agentData.completedAt
      ];
      
      db.run(sql, params, function(err) {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve({ id: this.lastID });
        }
      });
    });
  }

  async getExecutionLogs(executionId) {
    if (this.currentConfig.type === 'sqlite') {
      return this.getExecutionLogsSQLite(executionId);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async getExecutionLogsSQLite(executionId) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        SELECT * FROM execution_logs 
        WHERE execution_id = ?
        ORDER BY created_at ASC
      `;
      
      db.all(sql, [executionId], (err, rows) => {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve(rows);
        }
      });
    });
  }

  async saveExecutionLog(logData) {
    if (this.currentConfig.type === 'sqlite') {
      return this.saveExecutionLogSQLite(logData);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async saveExecutionLogSQLite(logData) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        INSERT INTO execution_logs 
        (execution_id, level, message, metadata)
        VALUES (?, ?, ?, ?)
      `;
      
      const params = [
        logData.executionId,
        logData.level,
        logData.message,
        JSON.stringify(logData.metadata || {})
      ];
      
      db.run(sql, params, function(err) {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve({ id: this.lastID });
        }
      });
    });
  }

  async getSystemConfig(key) {
    if (this.currentConfig.type === 'sqlite') {
      return this.getSystemConfigSQLite(key);
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async getSystemConfigSQLite(key) {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        SELECT config_value FROM system_config 
        WHERE config_key = ?
      `;
      
      db.get(sql, [key], (err, row) => {
        db.close();
        if (err) {
          reject(err);
        } else {
          resolve(row ? row.config_value : null);
        }
      });
    });
  }

  async getAllSystemConfig() {
    if (this.currentConfig.type === 'sqlite') {
      return this.getAllSystemConfigSQLite();
    }
    throw new Error(`不支持的数据库类型: ${this.currentConfig.type}`);
  }

  async getAllSystemConfigSQLite() {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        SELECT config_key, config_value, description 
        FROM system_config 
        ORDER BY config_key
      `;
      
      db.all(sql, [], (err, rows) => {
        db.close();
        if (err) {
          reject(err);
        } else {
          const config = {};
          rows.forEach(row => {
            config[row.config_key] = {
              value: row.config_value,
              description: row.description
            };
          });
          resolve(config);
        }
      });
    });
  }

  // 清理方法
  async cleanup() {
    // 清理旧的执行记录（保留最近30天）
    if (this.currentConfig.type === 'sqlite') {
      return this.cleanupSQLite();
    }
  }

  async cleanupSQLite() {
    const sqlite3 = require('sqlite3');
    const { Database } = require('sqlite3');
    
    return new Promise((resolve, reject) => {
      const db = new Database(this.currentConfig.database);
      
      const sql = `
        DELETE FROM workflow_executions 
        WHERE created_at < datetime('now', '-30 days')
      `;
      
      db.run(sql, [], function(err) {
        if (err) {
          db.close();
          reject(err);
        } else {
          const logSql = `
            DELETE FROM execution_logs 
            WHERE created_at < datetime('now', '-30 days')
          `;
          
          db.run(logSql, [], function(logErr) {
            db.close();
            if (logErr) {
              reject(logErr);
            } else {
              resolve({ 
                deletedExecutions: this.changes,
                deletedLogs: this.changes 
              });
            }
          });
        }
      });
    });
  }
}

// 单例模式
let databaseInstance = null;

function getDatabaseConfig() {
  if (!databaseInstance) {
    databaseInstance = new DatabaseConfig();
  }
  return databaseInstance;
}

module.exports = {
  DatabaseConfig,
  getDatabaseConfig
};
