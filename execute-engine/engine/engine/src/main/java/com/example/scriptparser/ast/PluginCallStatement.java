package com.example.scriptparser.ast;

import java.util.List;

public class PluginCallStatement implements Statement {
    private final String variableName;
    private final String pluginName;
    private final String functionName;
    private final List<String> arguments;

    public PluginCallStatement(String variableName, String pluginName, String functionName, List<String> arguments) {
        this.variableName = variableName;
        this.pluginName = pluginName;
        this.functionName = functionName;
        this.arguments = List.copyOf(arguments);
    }

    public String getVariableName() {
        return variableName;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getFunctionName() {
        return functionName;
    }

    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public String toString() {
        return "PluginCallStatement{var='" + variableName + "', plugin='" + pluginName +
                "', func='" + functionName + "', args=" + arguments + "}";
    }
}
