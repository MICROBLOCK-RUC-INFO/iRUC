package com.example.gqlpcomposer.exception;

import java.util.List;

/**
 * 循环依赖异常
 */
public class CircularDependencyException extends RuntimeException {

    private final List<String> dependencyChain;

    public CircularDependencyException(String message, List<String> dependencyChain) {
        super(message + " 依赖链: " + String.join(" -> ", dependencyChain));
        this.dependencyChain = dependencyChain;
    }

    public List<String> getDependencyChain() {
        return dependencyChain;
    }
}
