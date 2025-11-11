package com.example.scriptparser.ast;

public class GqlQueryStatement implements Statement {
    private final String variableName;
    private final String queryBody;

    public GqlQueryStatement(String variableName, String queryBody) {
        this.variableName = variableName;
        this.queryBody = queryBody;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getQueryBody() {
        return queryBody;
    }

    @Override
    public String toString() {
        return "GqlQueryStatement{var='" + variableName + "', query='" + queryBody + "'}";
    }
}
