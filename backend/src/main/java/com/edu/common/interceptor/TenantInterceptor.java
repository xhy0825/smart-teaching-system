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

/**
 * 租户拦截器，使用JSqlParser自动为SQL添加租户条件
 */
@Slf4j
public class TenantInterceptor implements InnerInterceptor {

    private static final String TENANT_ID_COLUMN = "tenant_id";

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler rh, BoundSql boundSql) throws SQLException {
        Long tenantId = TenantContextHolder.getTenantId();

        if (tenantId == null) {
            return;
        }

        try {
            String originalSql = boundSql.getSql();
            Select select = (Select) CCJSqlParserUtil.parse(originalSql);

            SelectBody selectBody = select.getSelectBody();
            if (selectBody instanceof PlainSelect) {
                PlainSelect plainSelect = (PlainSelect) selectBody;
                Expression where = plainSelect.getWhere();

                EqualsTo tenantCondition = new EqualsTo();
                tenantCondition.setLeftExpression(new Column(TENANT_ID_COLUMN));
                tenantCondition.setRightExpression(new LongValue(tenantId));

                if (where != null) {
                    plainSelect.setWhere(new AndExpression(where, tenantCondition));
                } else {
                    plainSelect.setWhere(tenantCondition);
                }
            }

            String newSql = select.toString();
            PluginUtils.MPBoundSql mpBs = PluginUtils.mpBoundSql(boundSql);
            mpBs.sql(newSql);

            log.debug("租户SQL拦截: tenantId={}, newSql={}", tenantId, newSql);

        } catch (Exception e) {
            log.warn("租户SQL解析失败，跳过拦截: {}", e.getMessage());
        }
    }
}
