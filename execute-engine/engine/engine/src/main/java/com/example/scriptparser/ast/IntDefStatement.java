package com.example.scriptparser.ast;

public class IntDefStatement implements Statement {
    private final String variableName;
    private final int value;

    public IntDefStatement(String variableName, int value) {
        this.variableName = variableName;
        this.value = value;
    }

    public String getVariableName() {
        return variableName;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "IntDefStatement{var='" + variableName + "', value=" + value + "}";
    }
}
