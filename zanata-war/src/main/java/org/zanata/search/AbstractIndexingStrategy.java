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
   private ScrollableResults newScrollableResults;
   private final Class<T> clazz;
   private final FullTextSession session;

   /**
    * @param clazz The type of entity to be returned by the Scrollable results
    */
   public AbstractIndexingStrategy(Class<T> clazz, FullTextSession session)
   {
      this.clazz = clazz;
      this.session = session;
   }

   /**
    * Performs the indexing.
    */
   public void invoke(IndexerProcessHandle handle)
   {
      int n = 0;
      newScrollableResults = queryResults(n);
      try
      {
         while (newScrollableResults.next() && !handle.shouldStop())
         {
            n++;
            T entity = (T) newScrollableResults.get(0); // index each element
            session.index(entity);
            handle.incrementProgress(1);
            if (n % sessionClearBatchSize == 0)
            {
               log.info("periodic flush and clear for {} (n={})", clazz, n);
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
            }
            onEntityIndexed(n);
         }
      }
      finally
      {
         if( newScrollableResults != null )
         {
            newScrollableResults.close();
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
    * @param clazz The type of entity to be returned by the Scrollable results
    * @return
    */
   protected abstract ScrollableResults queryResults(int offset);

   Class<T> getClazz()
   {
      return clazz;
   }

   ScrollableResults getScrollableResults()
   {
      return newScrollableResults;
   }

   void setScrollableResults(ScrollableResults scrollableResults)
   {
      this.newScrollableResults = scrollableResults;
   }

   FullTextSession getSession()
   {
      return session;
   }

}
