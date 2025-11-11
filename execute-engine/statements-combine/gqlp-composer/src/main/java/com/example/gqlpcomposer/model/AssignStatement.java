package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 赋值语句
 * 格式: assign globalVariable/variable = globalVariable/variable;
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class AssignStatement extends Statement {

    /**
     * 左值变量
     */
    private String leftVariable;

    /**
     * 右值变量
     */
    private String rightVariable;

    @Override
    public StatementType getType() {
        return StatementType.ASSIGN;
    }

    @Override
    public String toGqlpText() {
        return String.format("assign %s = %s;", leftVariable, rightVariable);
    }

    /**
     * 判断是否为全局变量（包含服务名前缀）
     */
    public boolean isGlobalVariable(String variable) {
        return variable != null && variable.contains(".");
    }
}
