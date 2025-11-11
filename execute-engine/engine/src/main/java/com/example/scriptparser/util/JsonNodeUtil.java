package com.example.scriptparser.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ValueNode;

public class JsonNodeUtil {

    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    public static ValueNode createValueNode(Object value) {
        if (value instanceof Boolean b) {
            return factory.booleanNode(b);
        } else if (value instanceof Integer i) {
            return factory.numberNode(i);
        } else if (value instanceof Long l) {
            return factory.numberNode(l);
        } else if (value instanceof Double d) {
            return factory.numberNode(d);
        } else if (value instanceof Float f) {
            return factory.numberNode(f);
        } else if (value instanceof String s) {
            return factory.textNode(s);
        } else if (value == null) {
            return factory.nullNode();
        }
        System.err.println("Warning: Unsupported type for JsonNode conversion: " + (value != null ? value.getClass() : "null"));
        return factory.nullNode();
    }

    public static JsonNode createDefaultJsonNode() {
        return factory.nullNode();
    }
}
