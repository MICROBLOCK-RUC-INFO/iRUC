package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据查询语句
 * 格式: new variable = gql query/mutation { gqlBody };
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class DataQueryStatement extends Statement {

    /**
     * 结果变量名
     */
    private String variable;

    /**
     * 查询类型（query 或 mutation）
     */
    private String queryType;

    /**
     * GraphQL 查询体
     */
    private String gqlBody;

    @Override
    public StatementType getType() {
        return StatementType.DATA_QUERY;
    }

    @Override
    public String toGqlpText() {
        return String.format("new %s = gql %s { %s };", variable, queryType, gqlBody);
    }
}
