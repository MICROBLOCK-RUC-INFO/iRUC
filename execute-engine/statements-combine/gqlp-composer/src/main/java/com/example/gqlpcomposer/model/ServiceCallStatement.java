package com.example.gqlpcomposer.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.ArrayList;

/**
 * 服务调用语句组合（包含前置赋值、调用、后置赋值）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCallStatement extends Statement {

    /**
     * 前置赋值语句列表（准备请求参数）
     */
    private List<AssignStatement> preAssignments = new ArrayList<>();

    /**
     * 远程调用语句
     */
    private CallStatement callStatement;

    /**
     * 后置赋值语句列表（处理响应）
     */
    private List<AssignStatement> postAssignments = new ArrayList<>();

    @Override
    public StatementType getType() {
        return StatementType.SERVICE_CALL;
    }

    @Override
    public String toGqlpText() {
        StringBuilder sb = new StringBuilder();

        // 前置赋值
        for (AssignStatement assign : preAssignments) {
            sb.append(assign.toGqlpText()).append("\n");
        }

        // 远程调用
        if (callStatement != null) {
            sb.append(callStatement.toGqlpText()).append("\n");
        }

        // 后置赋值
        for (AssignStatement assign : postAssignments) {
            sb.append(assign.toGqlpText()).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * 获取被调用的服务名
     */
    public String getCalledServiceName() {
        return callStatement != null ? callStatement.getServiceName() : null;
    }
}
