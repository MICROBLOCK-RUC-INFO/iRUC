package com.example.scriptparser.ast;

public class WhileStatement implements Statement {
    private final String conditionVariable;
    private final Block loopBlock;

    public WhileStatement(String conditionVariable, Block loopBlock) {
        this.conditionVariable = conditionVariable;
        this.loopBlock = loopBlock;
    }

    public String getConditionVariable() {
        return conditionVariable;
    }

    public Block getLoopBlock() {
        return loopBlock;
    }

    @Override
    public String toString() {
        return "WhileStatement{condition='" + conditionVariable + "', block=" + loopBlock + "}";
    }
}
