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

import lombok.Getter;
import lombok.Setter;

/**
 * Utility to easily build SQL or HQL queries.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public final class QueryBuilder
{

   private String select;
   private String from;
   private String where;

   protected QueryBuilder()
   {

   }

   public static QueryBuilder select( String select )
   {
      QueryBuilder builder = new QueryBuilder();
      builder.select = select;
      return builder;
   }

   public QueryBuilder from( String from )
   {
      this.from = from;
      return this;
   }

   public QueryBuilder where( String where )
   {
      this.where = where;
      return this;
   }

   public static String and( String ... ops )
   {
      LogicalExpression clause = new LogicalExpression(LogicalOperator.AND);
      clause.operands = Lists.newArrayList(ops);
      return clause.toWhereClause();
   }

   public static String or( String ... ops )
   {
      LogicalExpression clause = new LogicalExpression(LogicalOperator.OR);
      clause.operands = Lists.newArrayList(ops);
      return clause.toWhereClause();
   }

   public String toQueryString()
   {
      return "SELECT " + this.select + " FROM " + this.from + " WHERE " + this.where;
   }

   // ============== Internal implementation classes


   private static interface WhereExpression
   {
      String toWhereClause();
   }

   private static class LogicalExpression implements WhereExpression
   {
      private static final String EMPTY_EXPRESSION = "";

      protected LogicalExpression(LogicalOperator operator)
      {
         this.operator = operator;
      }

      @Getter
      @Setter
      protected List<String> operands;

      @Getter
      protected LogicalOperator operator;

      @Override
      public String toWhereClause()
      {
         Iterable<String> notEmptyOperands = Iterables.filter(operands, StringNotEmptyPredicate.PREDICATE);
         if (Iterables.isEmpty(notEmptyOperands))
         {
            return EMPTY_EXPRESSION;
         }
         Joiner joiner = Joiner.on(" " + operator.name() + " ");
         StringBuilder expStr = new StringBuilder("(");
         joiner.appendTo(expStr, notEmptyOperands);
         expStr.append(")");
         return expStr.toString();
      }


   }

   private static enum StringNotEmptyPredicate implements Predicate<String>
   {
      PREDICATE;
      @Override
      public boolean apply(@Nullable String input)
      {
         return !Strings.isNullOrEmpty(input);
      }
   }

   private static class SimpleExpression implements WhereExpression
   {
      public SimpleExpression(String exp)
      {
         this.exp = exp;
      }

      protected String exp;

      @Override
      public String toWhereClause()
      {
         return exp;
      }
   }

   private enum LogicalOperator
   {
      OR,
      AND,
      NOT,
   }
}
