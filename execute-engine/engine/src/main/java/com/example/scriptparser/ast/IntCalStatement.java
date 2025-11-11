package com.example.scriptparser.ast;

public class IntCalStatement implements Statement {
    private final String targetVariable;
    private final String sourceVariable;
    private final String operator;
    private final int operand;

    public IntCalStatement(String targetVariable, String sourceVariable, String operator, int operand) {
        this.targetVariable = targetVariable;
        this.sourceVariable = sourceVariable;
        this.operator = operator;
        this.operand = operand;
    }

    public String getTargetVariable() {
        return targetVariable;
    }

    public String getSourceVariable() {
        return sourceVariable;
    }

    public String getOperator() {
        return operator;
    }

    public int getOperand() {
        return operand;
    }

    @Override
    public String toString() {
        return "IntCalStatement{target='" + targetVariable + "', source='" + sourceVariable +
                "', op='" + operator + "', operand=" + operand + "}";
    }
}
