package com.example.gqlpcomposer.exception;

/**
 * 服务未找到异常
 */
public class ServiceNotFoundException extends RuntimeException {

    private final String serviceName;

    public ServiceNotFoundException(String serviceName) {
        super("服务未找到: " + serviceName);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
