package com.edu.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 租户上下文持有者，用于存储当前请求的租户ID
 */
public class TenantContextHolder {

    private static final TransmittableThreadLocal<Long> TENANT_ID = new TransmittableThreadLocal<>();

    public static void setTenantId(Long tenantId) {
        TENANT_ID.set(tenantId);
    }

    public static Long getTenantId() {
        return TENANT_ID.get();
    }

    public static void clear() {
        TENANT_ID.remove();
    }
}
