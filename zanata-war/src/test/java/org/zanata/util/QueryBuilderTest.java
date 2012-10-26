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

import org.hibernate.criterion.Restrictions;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.zanata.util.QueryBuilder.and;
import static org.zanata.util.QueryBuilder.or;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class QueryBuilderTest
{
   @Test
   public void simpleQueryTest()
   {
      String query =
      QueryBuilder.select("col1, col2").from("DatabaseTable")
            .where("col1 = 2 and col2 = 1").toQueryString();

      assertThat(query, equalToIgnoringCase("select col1, col2 from DatabaseTable where col1 = 2 and col2 = 1"));
   }

   @Test
   public void andQueryTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable")
                  .where(and("col1 = 2", "col2 = 1")).toQueryString();

      assertThat(query, equalToIgnoringCase("select col1, col2 from DatabaseTable where (col1 = 2 and col2 = 1)"));
   }

   @Test
   public void orQueryTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable")
                  .where(or("col1 = 2", "col2 = 1")).toQueryString();

      assertThat(query, equalToIgnoringCase("select col1, col2 from DatabaseTable where (col1 = 2 or col2 = 1)"));
   }

   @Test
   public void complexQueryTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable")
                  .where(and("col1 = 2", or("or1", "or2", "or3"), and("and1", "and2"))).toQueryString();

      assertThat(query, equalToIgnoringCase("SELECT col1, col2 FROM DatabaseTable WHERE (col1 = 2 AND (or1 OR or2 OR or3) AND (and1 AND and2))"));
   }

   @Test
   public void emptyArgumentsQueryTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable")
                  .where(and("col1 = 2", or(), and("and1", "and2"), and())).toQueryString();

      assertThat(query, equalToIgnoringCase("SELECT col1, col2 FROM DatabaseTable WHERE (col1 = 2 AND (and1 AND and2))"));

   }

   @Test
   public void complexQueryWithLeftJoinTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable").leftJoin("Table2").with(Restrictions.eq("Table2.content", ":content").toString())
                  .where(and("col1 = 2", or("or1", "or2", "or3"), and("and1", "and2"))).toQueryString();

      assertThat(query, equalToIgnoringCase("SELECT col1, col2 FROM DatabaseTable LEFT JOIN Table2 WITH Table2.content=:content WHERE (col1 = 2 AND (or1 OR or2 OR or3) AND (and1 AND and2))"));
   }

   @Test
   public void existsSubQueryTest()
   {
      String query =
            QueryBuilder.exists().from("DatabaseTable")
                  .where("col1 = 2 and col2 = 1").toQueryString();

      assertThat(query, equalToIgnoringCase(" exists ( from DatabaseTable where col1 = 2 and col2 = 1)"));
   }

   @Test
   public void orderByQueryTest()
   {
      String query =
            QueryBuilder.select("col1, col2").from("DatabaseTable")
                  .where("col1 = 2 and col2 = 1")
                  .orderBy("col1").toQueryString();

      assertThat(query, equalToIgnoringCase("select col1, col2 from DatabaseTable where col1 = 2 and col2 = 1 order by col1"));
   }
}
