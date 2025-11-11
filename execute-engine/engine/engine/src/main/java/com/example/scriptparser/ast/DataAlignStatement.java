package com.example.scriptparser.ast;

public class DataAlignStatement implements Statement {
    private final String outputVariable;
    private final String sourceVariable;

    public DataAlignStatement(String outputVariable, String sourceVariable) {
        this.outputVariable = outputVariable;
        this.sourceVariable = sourceVariable;
    }

    public String getOutputVariable() {
        return outputVariable;
    }

    public String getSourceVariable() {
        return sourceVariable;
    }

    @Override
    public String toString() {
        return "DataAlignStatement{output='" + outputVariable + "', source='" + sourceVariable + "'}";
    }
}
