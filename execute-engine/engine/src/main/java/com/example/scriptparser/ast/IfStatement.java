package com.example.scriptparser.ast;

public class IfStatement implements Statement {
    private final String conditionVariable;
    private final Block trueBlock;

    public IfStatement(String conditionVariable, Block trueBlock) {
        this.conditionVariable = conditionVariable;
        this.trueBlock = trueBlock;
    }

    public String getConditionVariable() {
        return conditionVariable;
    }

    public Block getTrueBlock() {
        return trueBlock;
    }

    @Override
    public String toString() {
        return "IfStatement{condition='" + conditionVariable + "', block=" + trueBlock + "}";
    }
}
