package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * 插件调用语句
 * 格式: new variableList = plugin file/function(variableList);
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class PluginCallStatement extends Statement {

    /**
     * 输出变量列表
     */
    private List<String> outputVariables;

    /**
     * 插件文件名
     */
    private String pluginFile;

    /**
     * 函数名
     */
    private String functionName;

    /**
     * 输入变量列表
     */
    private List<String> inputVariables;

    @Override
    public StatementType getType() {
        return StatementType.PLUGIN_CALL;
    }

    @Override
    public String toGqlpText() {
        String outputVars = String.join(", ", outputVariables);
        String inputVars = String.join(", ", inputVariables);
        return String.format("new %s = plugin %s/%s(%s);",
                outputVars, pluginFile, functionName, inputVars);
    }
}
