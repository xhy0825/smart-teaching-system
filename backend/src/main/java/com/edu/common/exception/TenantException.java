package com.edu.common.exception;

/**
 * 租户相关异常
 */
public class TenantException extends BusinessException {

    public TenantException(String message) {
        super(403, message);
    }

    public static TenantException notFound() {
        return new TenantException("租户不存在");
    }

    public static TenantException disabled() {
        return new TenantException("租户已禁用");
    }

    public static TenantException expired() {
        return new TenantException("租户服务已到期");
    }

    public static TenantException accessDenied() {
        return new TenantException("无权访问该租户数据");
    }
}
