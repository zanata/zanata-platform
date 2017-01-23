/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import java.util.List;
import javax.annotation.Nullable;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Utility to easily build SQL or HQL queries.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public final class QueryBuilder {
    private String select;
    private String from;
    private String where;
    private String leftJoin;
    private String withClause;
    private boolean isExistsSubQuery = false;
    private boolean isNotExistsSubQuery = false;
    private String orderBy;

    protected QueryBuilder() {
    }

    public static QueryBuilder select(String select) {
        QueryBuilder builder = new QueryBuilder();
        builder.select = select;
        return builder;
    }
    // TODO this and with only works in HQL.

    public static QueryBuilder exists() {
        QueryBuilder builder = new QueryBuilder();
        builder.isExistsSubQuery = true;
        return builder;
    }
    // TODO this and with only works in HQL.

    public static QueryBuilder notExists() {
        QueryBuilder builder = new QueryBuilder();
        builder.isNotExistsSubQuery = true;
        return builder;
    }

    public QueryBuilder from(String from) {
        this.from = from;
        return this;
    }

    public QueryBuilder where(String where) {
        this.where = where;
        return this;
    }

    public static String and(String... ops) {
        return and(Lists.newArrayList(ops));
    }

    public static String and(List<String> ops) {
        return LogicalExpression.conjunction(ops).toWhereClause();
    }

    public static String or(String... ops) {
        return or(Lists.newArrayList(ops));
    }

    public static String or(List<String> ops) {
        return LogicalExpression.disjunction(ops).toWhereClause();
    }

    public QueryBuilder leftJoin(String association) {
        this.leftJoin = association;
        return this;
    }

    public QueryBuilder with(String withClause) {
        this.withClause = withClause;
        return this;
    }

    public String toQueryString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (select != null && !isExistsSubQuery && !isNotExistsSubQuery) {
            stringBuilder.append("SELECT ").append(select);
        } else if (isExistsSubQuery) {
            stringBuilder.append(" EXISTS (");
        } else {
            stringBuilder.append(" NOT EXISTS (");
        }
        stringBuilder.append(" FROM ").append(from);
        if (!Strings.isNullOrEmpty(leftJoin)) {
            stringBuilder.append(" LEFT JOIN ").append(leftJoin);
            if (withClause != null) {
                stringBuilder.append(" WITH ").append(withClause);
            }
        }
        stringBuilder.append(" WHERE ").append(where);
        if (isExistsSubQuery || isNotExistsSubQuery) {
            stringBuilder.append(")");
        }
        if (!Strings.isNullOrEmpty(orderBy)) {
            stringBuilder.append(" ORDER BY ").append(orderBy);
        }
        return stringBuilder.toString();
    }

    public QueryBuilder orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }
    // ============== Internal implementation classes

    private static interface WhereExpression {

        String toWhereClause();
    }

    private static class LogicalExpression implements WhereExpression {
        private static final String EMPTY_EXPRESSION = "";
        private List<String> operands;
        private LogicalOperator operator;

        private LogicalExpression(LogicalOperator operator) {
            this.operator = operator;
        }

        public static WhereExpression conjunction(List<String> ops) {
            LogicalExpression expression =
                    new LogicalExpression(LogicalOperator.AND);
            expression.setOperands(ops);
            return expression;
        }

        public static WhereExpression disjunction(List<String> ops) {
            LogicalExpression expression =
                    new LogicalExpression(LogicalOperator.OR);
            expression.setOperands(ops);
            return expression;
        }

        @Override
        public String toWhereClause() {
            Iterable<String> notEmptyOperands = Iterables.filter(operands,
                    StringNotEmptyPredicate.PREDICATE);
            if (Iterables.isEmpty(notEmptyOperands)) {
                return EMPTY_EXPRESSION;
            }
            Joiner joiner = Joiner.on(" " + operator.name() + " ");
            StringBuilder expStr = new StringBuilder("(");
            joiner.appendTo(expStr, notEmptyOperands);
            expStr.append(")");
            return expStr.toString();
        }

        public void setOperands(final List<String> operands) {
            this.operands = operands;
        }
    }

    private static enum StringNotEmptyPredicate implements Predicate<String> {
        PREDICATE;

        @Override
        public boolean apply(@Nullable String input) {
            return !Strings.isNullOrEmpty(input);
        }
    }

    private static class SimpleExpression implements WhereExpression {
        private String exp;

        private SimpleExpression(String exp) {
            this.exp = exp;
        }

        @Override
        public String toWhereClause() {
            return exp;
        }
    }

    private enum LogicalOperator {
        OR,
        AND,
        NOT;

    }
}
