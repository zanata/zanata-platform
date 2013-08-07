package org.zanata.search;

import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;

import lombok.extern.slf4j.Slf4j;

/**
 * Base indexing strategy.
 *
 * @param <T> The type of object that this indexing strategy handles.
 */
@Slf4j
public abstract class AbstractIndexingStrategy<T>
{
   private int sessionClearBatchSize = 1000;
   private ScrollableResults scrollableResults;
   private final Class<T> entityType;
   private final FullTextSession session;

   /**
    * @param entityType The type of entity to be returned by the Scrollable results
    */
   public AbstractIndexingStrategy(Class<T> entityType, FullTextSession session)
   {
      this.entityType = entityType;
      this.session = session;
   }

   /**
    * Performs the indexing.
    */
   public void invoke(IndexerProcessHandle handle)
   {
      int n = 0;
      scrollableResults = queryResults(n);
      try
      {
         while (scrollableResults.next() && !handle.shouldStop())
         {
            n++;
            T entity = (T) scrollableResults.get(0);
            session.index(entity);
            handle.incrementProgress(1);
            if (n % sessionClearBatchSize == 0)
            {
               log.info("periodic flush and clear for {} (n={})", entityType, n);
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
            }
            onEntityIndexed(n);
         }
      }
      finally
      {
         if( scrollableResults != null )
         {
            scrollableResults.close();
         }
      }
   }

   /**
    * Callback method that is called every time an entity is indexed.
    * @param n The entity number that was indexed.
    */
   protected abstract void onEntityIndexed(int n);

   /**
    * Returns the Scrollable results for instances of clazz
    * @param offset
    * @param entityType The type of entity to be returned by the Scrollable results
    * @return
    */
   protected abstract ScrollableResults queryResults(int offset);

   Class<T> getEntityType()
   {
      return entityType;
   }

   ScrollableResults getScrollableResults()
   {
      return scrollableResults;
   }

   void setScrollableResults(ScrollableResults scrollableResults)
   {
      this.scrollableResults = scrollableResults;
   }

   FullTextSession getSession()
   {
      return session;
   }

}
