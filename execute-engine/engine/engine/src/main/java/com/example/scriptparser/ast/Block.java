package com.example.scriptparser.ast;

import java.util.List;

public class Block implements Statement {
    private final List<Statement> statements;

    public Block(List<Statement> statements) {
        this.statements = List.copyOf(statements);
    }

    public List<Statement> getStatements() {
        return statements;
    }

    @Override
    public String toString() {
        return "Block{statements=" + statements.size() + "}";
    }
}
