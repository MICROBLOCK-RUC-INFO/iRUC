package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 调用语句（call service;）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class CallStatement extends Statement {

    /**
     * 被调用的服务名
     */
    private String serviceName;

    @Override
    public StatementType getType() {
        return StatementType.SERVICE_CALL;
    }

    @Override
    public String toGqlpText() {
        return String.format("call %s;", serviceName);
    }
}
