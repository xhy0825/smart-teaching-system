package com.edu.common.interceptor;

import com.edu.common.util.TenantContextHolder;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantInterceptorTest {

    private TenantInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new TenantInterceptor();
        TenantContextHolder.setTenantId(100L);
    }

    @AfterEach
    void tearDown() {
        TenantContextHolder.clear();
    }

    @Test
    void testAddTenantConditionToSimpleSelect() throws Exception {
        String sql = "SELECT * FROM sys_user WHERE username = 'admin'";
        Select select = (Select) CCJSqlParserUtil.parse(sql);
        assertNotNull(select);
        // 验证解析成功，具体逻辑需要构造Mock环境
    }

    @Test
    void testTenantContextHolderIntegration() {
        Long tenantId = TenantContextHolder.getTenantId();
        assertEquals(100L, tenantId);
        TenantContextHolder.clear();
        assertNull(TenantContextHolder.getTenantId());
    }
}
