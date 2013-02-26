/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.search;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.criterion.Projections;
import org.hibernate.search.FullTextSession;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Slf4j
public abstract class ClassIndexer <T>
{
   //TODO make this configurable
   private static final int DEFAULT_BATCH_SIZE = 5000;

   private final int batchSize = DEFAULT_BATCH_SIZE;

   public int getEntityCount(FullTextSession session, Class<T> clazz)
   {
      return (Integer) session.createCriteria(clazz).setProjection(Projections.rowCount()).list().get(0);
   }

   public void index(FullTextSession session, IndexerProcessHandle handle, Class<T> clazz)
   {
      log.info("Setting manual-flush and ignore-cache for {}", clazz);
      session.setFlushMode(FlushMode.MANUAL);
      session.setCacheMode(CacheMode.IGNORE);
      ScrollableResults results = null;
      try
      {
         int n = 0;
         results = getScrollableResults(session, clazz, n);
         while (results.next() && !handle.shouldStop())
         {
            n++;
            T entity = (T) results.get(0); // index each element
            session.index(entity);
            handle.incrementProgress(1);
            if (n % batchSize == 0)
            {
               log.info("periodic flush and clear for {} (n={})", clazz, n);
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
               results.close();
               results = getScrollableResults(session, clazz, n);
            }
         }
         session.flushToIndexes(); // apply changes to indexes
         session.clear(); // clear since the queue is processed
      }
      catch (Exception e)
      {
         log.warn("Unable to index objects of type {}", e, clazz.getName());
         handle.setHasError(true);
      }
      finally
      {
         if (results != null)
            results.close();
      }
   }

   private ScrollableResults getScrollableResults(FullTextSession session, Class<T> clazz, int fromIndex)
   {
      Query query = getQuery(session, clazz);
//      Criteria query = session.createCriteria(clazz);
      query.setFirstResult(fromIndex).setMaxResults(batchSize);
      return query.scroll(ScrollMode.FORWARD_ONLY);
   }

   /**
    * Create a query which returns all instances of clazz
    * @param clazz
    * @return
    */
   protected abstract Query getQuery(FullTextSession session, Class<T> clazz);

}
