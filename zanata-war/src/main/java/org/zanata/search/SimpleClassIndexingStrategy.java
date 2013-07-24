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
package org.zanata.search;

import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;

import lombok.extern.slf4j.Slf4j;

/**
 * Indexing strategy that fetches all instances in a given class and indexes them.
 * This class batches the fetching of the entities and might be a bit slower as it does
 * not account for lazily loaded entity relationships.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class SimpleClassIndexingStrategy<T> extends AbstractIndexingStrategy<T>
{

   public static final int MAX_QUERY_ROWS = 5000;

   public SimpleClassIndexingStrategy(FullTextSession session, IndexerProcessHandle handle, Class<T> clazz)
   {
      super(session, handle, clazz);
   }

   @Override
   protected void onEntityIndexed(int n)
   {
      if (n % MAX_QUERY_ROWS == 0)
      {
         SimpleClassIndexingStrategy.log.info("restarting query for {} (n={})", clazz, n);
         scrollableResults.close();
         scrollableResults = getScrollableResults(session, clazz, n);
      }
   }

   @Override
   protected ScrollableResults getScrollableResults(FullTextSession session, Class<T> clazz, int firstResult)
   {
      Query query = getQuery(session, clazz);
      query.setFirstResult(firstResult);
      query.setMaxResults(MAX_QUERY_ROWS);
      return query.scroll(ScrollMode.FORWARD_ONLY);
   }

   @Override
   protected Query getQuery(FullTextSession session, Class<T> clazz)
   {
      return session.createQuery("from "+clazz.getName());
   }
}
