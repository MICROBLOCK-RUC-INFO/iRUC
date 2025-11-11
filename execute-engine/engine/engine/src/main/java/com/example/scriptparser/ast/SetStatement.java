package com.example.scriptparser.ast;

public class SetStatement implements Statement {
    private final String variableName;
    private final ValueNode value;

    public SetStatement(String variableName, ValueNode value) {
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public ValueNode getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "SetStatement{var='" + variableName + "', value=" + value.getValue() + "}";
    }
}
