package com.example.scriptparser.ast;

public class ValueNode {
    private final Object value;

    public ValueNode(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "ValueNode{" +
                "value=" + (value != null ? value.toString() : "null") +
                '}';
    }
}
