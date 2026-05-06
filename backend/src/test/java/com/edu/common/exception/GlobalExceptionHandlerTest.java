package com.edu.common.exception;

import com.edu.common.entity.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void testHandleBusinessException() {
        BusinessException e = new BusinessException("测试业务异常");
        Result<Void> result = handler.handleBusinessException(e);
        assertEquals(400, result.getCode());
        assertEquals("测试业务异常", result.getMessage());
    }

    @Test
    void testHandleTenantException() {
        TenantException e = TenantException.accessDenied();
        Result<Void> result = handler.handleTenantException(e);
        assertEquals(403, result.getCode());
        assertEquals("无权访问该租户数据", result.getMessage());
    }

    @Test
    void testTenantExceptionStaticMethods() {
        TenantException notFound = TenantException.notFound();
        assertEquals("租户不存在", notFound.getMessage());

        TenantException disabled = TenantException.disabled();
        assertEquals("租户已禁用", disabled.getMessage());

        TenantException expired = TenantException.expired();
        assertEquals("租户服务已到期", expired.getMessage());
    }
}
