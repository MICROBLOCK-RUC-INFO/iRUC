package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 返回语句
 * 格式: return globalVariable;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ReturnStatement extends Statement {

    /**
     * 返回的全局变量
     */
    private String returnVariable;

    @Override
    public StatementType getType() {
        return StatementType.RETURN;
    }

    @Override
    public String toGqlpText() {
        return String.format("return %s;", returnVariable);
    }
}
