package com.example.scriptparser.interpreter;

import com.example.scriptparser.ast.*;
import com.example.scriptparser.model.ExecutionContext;
import com.example.scriptparser.util.JsonNodeUtil;

import java.util.List;

public class Interpreter {

    public void interpretScript(List<Statement> script, ExecutionContext context) {
        System.out.println("--- Starting Interpretation ---");
        try {
            interpretBlock(script, context);
        } catch (Exception e) {
            System.err.println("\n--- Interpretation Halted Due to Error ---");
            System.err.println("Runtime Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            System.out.println("\n--- Interpretation Finished ---");
            System.out.println("Final Context State:");
            System.out.println(context);
        }
    }

    private void interpretBlock(List<Statement> statements, ExecutionContext context) {
        if (statements == null) return;
        for (Statement statement : statements) {
            interpretStatement(statement, context);
        }
    }

    private void interpretStatement(Statement statement, ExecutionContext context) {
        if (statement instanceof GqlQueryStatement stmt) {
            interpretGqlQuery(stmt, context);
        } else if (statement instanceof DataAlignStatement stmt) {
            interpretDataAlign(stmt, context);
        } else if (statement instanceof PluginCallStatement stmt) {
            interpretPluginCall(stmt, context);
        } else if (statement instanceof DefStatement stmt) {
            interpretDef(stmt, context);
        } else if (statement instanceof SetStatement stmt) {
            interpretSet(stmt, context);
        } else if (statement instanceof IntDefStatement stmt) {
            interpretIntDef(stmt, context);
        } else if (statement instanceof IntCalStatement stmt) {
            interpretIntCal(stmt, context);
        } else if (statement instanceof IfStatement stmt) {
            interpretIf(stmt, context);
        } else if (statement instanceof WhileStatement stmt) {
            interpretWhile(stmt, context);
        } else if (statement instanceof Block block) {
            interpretBlock(block.getStatements(), context);
        } else {
            throw new RuntimeException("Unknown statement type: " + statement.getClass().getName());
        }
    }

    private void interpretGqlQuery(GqlQueryStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting GqlQuery: " + stmt.getVariableName());
        context.setVariable(stmt.getVariableName(), JsonNodeUtil.createDefaultJsonNode());
    }

    private void interpretDataAlign(DataAlignStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting DataAlign: " + stmt.getOutputVariable());
        Object sourceValue = context.getVariable(stmt.getSourceVariable());
        context.setVariable(stmt.getOutputVariable(), sourceValue);
    }

    private void interpretPluginCall(PluginCallStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting PluginCall: " + stmt.getVariableName());
        context.setVariable(stmt.getVariableName(), JsonNodeUtil.createDefaultJsonNode());
    }

    private void interpretDef(DefStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting Def: " + stmt.getVariableName());
        Object literalValue = stmt.getValue().getValue();
        context.setVariable(stmt.getVariableName(), JsonNodeUtil.createValueNode(literalValue));
    }

    private void interpretSet(SetStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting Set: " + stmt.getVariableName());
        Object literalValue = stmt.getValue().getValue();
        context.setVariable(stmt.getVariableName(), JsonNodeUtil.createValueNode(literalValue));
    }

    private void interpretIntDef(IntDefStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting IntDef: " + stmt.getVariableName());
        context.setVariable(stmt.getVariableName(), stmt.getValue());
    }

    private void interpretIntCal(IntCalStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting IntCal: " + stmt.getTargetVariable());
        Object sourceValueObj = context.getVariable(stmt.getSourceVariable());
        if (!(sourceValueObj instanceof Integer sourceValue)) {
            throw new RuntimeException("Type error in CAL: Variable is not an integer");
        }

        int result;
        switch (stmt.getOperator()) {
            case "+":
                result = sourceValue + stmt.getOperand();
                break;
            case "-":
                result = sourceValue - stmt.getOperand();
                break;
            default:
                throw new RuntimeException("Unsupported operator: " + stmt.getOperator());
        }

        context.setVariable(stmt.getTargetVariable(), result);
    }

    private void interpretIf(IfStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting If: " + stmt.getConditionVariable());
        boolean executeBlock = evaluateCondition(stmt.getConditionVariable(), context);
        if (executeBlock) {
            interpretBlock(stmt.getTrueBlock().getStatements(), context);
        }
    }

    private void interpretWhile(WhileStatement stmt, ExecutionContext context) {
        System.out.println("Interpreting While: " + stmt.getConditionVariable());
        int iteration = 0;
        final int maxIterations = 1000;

        while (evaluateCondition(stmt.getConditionVariable(), context)) {
            iteration++;
            if (iteration > maxIterations) {
                System.err.println("Warning: While loop exceeded max iterations");
                break;
            }
            interpretBlock(stmt.getLoopBlock().getStatements(), context);
        }
    }

    private boolean evaluateCondition(String variableName, ExecutionContext context) {
        Object conditionValue = context.getVariable(variableName);
        if (conditionValue == null) {
            throw new RuntimeException("Condition variable not defined: " + variableName);
        }

        if (conditionValue instanceof Integer intValue) {
            if (intValue < 0) {
                throw new RuntimeException("Integer condition cannot be negative: " + intValue);
            }
            return intValue > 0;
        }

        throw new RuntimeException("Invalid condition type for variable: " + variableName);
    }
}
