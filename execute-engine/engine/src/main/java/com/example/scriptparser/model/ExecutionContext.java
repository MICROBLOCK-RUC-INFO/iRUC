package com.example.scriptparser.model;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ExecutionContext {
    private final Map<String, Object> variables = new HashMap<>();

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public Map<String, Object> getAllVariables() {
        return new HashMap<>(variables);
    }

    @Override
    public String toString() {
        String varsString = variables.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + valueToString(entry.getValue()))
                .collect(Collectors.joining(", "));
        return "ExecutionContext{" + varsString + "}";
    }

    private String valueToString(Object value) {
        if (value == null) return "null";
        return value.toString();
    }
}
