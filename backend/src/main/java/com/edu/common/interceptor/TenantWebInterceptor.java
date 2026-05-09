package com.edu.common.interceptor;

import com.edu.common.util.TenantContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Web层租户拦截器，从请求头获取租户ID
 */
@Slf4j
@Component
public class TenantWebInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantIdStr = request.getHeader(TENANT_HEADER);

        if (tenantIdStr != null && !tenantIdStr.isEmpty()) {
            try {
                Long tenantId = Long.parseLong(tenantIdStr);
                TenantContextHolder.setTenantId(tenantId);
                log.debug("设置租户上下文: tenantId={}", tenantId);
            } catch (NumberFormatException e) {
                log.warn("无效的租户ID: {}", tenantIdStr);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                  Object handler, Exception ex) {
        // 清理租户上下文
        TenantContextHolder.clear();
    }
}