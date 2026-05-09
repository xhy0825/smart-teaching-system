package com.edu.common.interceptor;

import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.edu.common.util.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 租户拦截器，使用JSqlParser自动为SQL添加租户条件
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";

    /**
     * Default exempt tables that should not have tenant_id appended.
     * These tables either don't have tenant_id column or are linked via parent table.
     */
    private static final Set<String> DEFAULT_EXEMPT_TABLES = Set.of(
            "tenant",
            "tenant_config",
            "permission",
            "class",              // no tenant_id column
            "grade",              // linked via school
            "school",             // linked via tenant
            "sys_user",           // already filtered by service
            "question",           // linked via question_bank
            "exam_question",      // linked via exam_paper
            "answer",             // linked via answer_sheet
            "student_wrong_question", // linked via student
            "user_role",          // linked via user
            "role_permission"     // linked via role
    );

    private final Set<String> exemptTables;

    public TenantInterceptor() {
        this.exemptTables = new HashSet<>(DEFAULT_EXEMPT_TABLES);
    }

    public TenantInterceptor(Set<String> exemptTables) {
        this.exemptTables = new HashSet<>(DEFAULT_EXEMPT_TABLES);
        if (exemptTables != null) {
            this.exemptTables.addAll(exemptTables);
        }
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler rh, BoundSql boundSql) throws SQLException {
        Long tenantId = TenantContextHolder.getTenantId();

        if (tenantId == null) {
            return;
        }

        String originalSql = boundSql.getSql();
        Select select;
        try {
            select = (Select) CCJSqlParserUtil.parse(originalSql);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse SQL for tenant filtering: " + originalSql, e);
        }

        SelectBody selectBody = select.getSelectBody();
        if (selectBody instanceof PlainSelect plainSelect) {
            processPlainSelect(plainSelect, tenantId);
        } else if (selectBody instanceof SetOperationList setOpList) {
            processSetOperationList(setOpList, tenantId);
        }

        String newSql = select.toString();
        PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
        mpBs.sql(newSql);

        log.debug("租户SQL拦截: tenantId={}, newSql={}", tenantId, newSql);
    }

    private void processPlainSelect(PlainSelect plainSelect, Long tenantId) {
        if (isExemptTable(plainSelect)) {
            return;
        }

        EqualsTo tenantCondition = createTenantCondition(tenantId);
        Expression where = plainSelect.getWhere();

        if (where != null) {
            plainSelect.setWhere(new AndExpression(where, tenantCondition));
        } else {
            plainSelect.setWhere(tenantCondition);
        }
    }

    private void processSetOperationList(SetOperationList setOpList, Long tenantId) {
        List<SelectBody> selectBodies = setOpList.getSelects();
        if (selectBodies == null) {
            return;
        }
        for (SelectBody body : selectBodies) {
            if (body instanceof PlainSelect plainSelect) {
                processPlainSelect(plainSelect, tenantId);
            }
        }
    }

    private boolean isExemptTable(PlainSelect plainSelect) {
        if (plainSelect.getFromItem() == null) {
            return false;
        }
        // 从FromItem中提取实际表名，去掉可能的别名
        String fromItemStr = plainSelect.getFromItem().toString().toLowerCase().trim();
        // 处理带别名的情况: "grade g" 或 "grade as g"
        String tableName = fromItemStr.split("\\s+")[0];
        return exemptTables.contains(tableName);
    }

    private EqualsTo createTenantCondition(Long tenantId) {
        EqualsTo tenantCondition = new EqualsTo();
        tenantCondition.setLeftExpression(new Column(TENANT_ID_COLUMN));
        tenantCondition.setRightExpression(new LongValue(tenantId));
        return tenantCondition;
    }
}
