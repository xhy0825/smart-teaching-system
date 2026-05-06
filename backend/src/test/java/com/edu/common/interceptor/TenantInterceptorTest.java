package com.edu.common.interceptor;

import com.edu.common.util.TenantContextHolder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantInterceptorTest {

    private TenantInterceptor interceptor;

    @Mock
    private MappedStatement mappedStatement;

    @Mock
    private BoundSql boundSql;

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
    void testSimpleSelectAddsTenantCondition() throws Exception {
        String sql = "SELECT * FROM sys_user WHERE username = 'admin'";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql).setSql(argThat(s ->
                s != null && s.contains("tenant_id") && s.contains("100")
        ));
    }

    @Test
    void testSimpleSelectWithoutWhereAddsTenantCondition() throws Exception {
        String sql = "SELECT * FROM sys_user";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql).setSql(argThat(s ->
                s != null && s.contains("tenant_id = 100")
        ));
    }

    @Test
    void testUnionQueryAddsTenantConditionToEachBody() throws Exception {
        String sql = "SELECT * FROM sys_user WHERE username = 'a' UNION SELECT * FROM sys_user WHERE username = 'b'";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql).setSql(argThat(s -> {
            if (s == null) return false;
            // Count occurrences of tenant_id = 100 (should appear in both sides of UNION)
            int count = 0;
            int idx = 0;
            String target = "tenant_id = 100";
            while ((idx = s.indexOf(target, idx)) != -1) {
                count++;
                idx += target.length();
            }
            return count >= 2;
        }));
    }

    @Test
    void testExemptTableSkipsTenantCondition() throws Exception {
        String sql = "SELECT * FROM tenant WHERE id = 1";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql, never()).setSql(anyString());
    }

    @Test
    void testDefaultExemptTablesConfigured() throws Exception {
        // tenant_config and permission are exempt by default
        for (String exemptTable : new String[]{"tenant", "tenant_config", "permission"}) {
            String sql = "SELECT * FROM " + exemptTable;
            when(boundSql.getSql()).thenReturn(sql);

            interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

            verify(boundSql, never()).setSql(anyString());
        }
    }

    @Test
    void testParseFailureThrowsRuntimeException() {
        String badSql = "SELECT * FROM"; // invalid SQL
        when(boundSql.getSql()).thenReturn(badSql);

        assertThrows(RuntimeException.class, () ->
                interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql)
        );
    }

    @Test
    void testParseFailureIncludesOriginalSqlInMessage() {
        String badSql = "NOT VALID SQL @@@@";
        when(boundSql.getSql()).thenReturn(badSql);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql)
        );

        assertTrue(ex.getMessage().contains(badSql));
    }

    @Test
    void testCustomExemptTables() throws Exception {
        interceptor = new TenantInterceptor(Set.of("custom_table"));
        TenantContextHolder.setTenantId(100L);

        String sql = "SELECT * FROM custom_table WHERE id = 1";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql, never()).setSql(anyString());
    }

    @Test
    void testNoTenantIdSkipsInterception() throws Exception {
        TenantContextHolder.clear();

        String sql = "SELECT * FROM sys_user";
        when(boundSql.getSql()).thenReturn(sql);

        interceptor.beforeQuery(null, mappedStatement, null, null, null, boundSql);

        verify(boundSql, never()).setSql(anyString());
    }
}
