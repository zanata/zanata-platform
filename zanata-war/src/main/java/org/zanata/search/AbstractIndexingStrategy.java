package org.zanata.search;

import org.hibernate.Query;
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
   private IndexerProcessHandle handle;
   private int sessionClearBatchSize = 1000;
   FullTextSession session;
   Class<T> clazz;
   ScrollableResults scrollableResults;


   public AbstractIndexingStrategy(FullTextSession session, IndexerProcessHandle handle, Class<T> clazz)
   {
      this.session = session;
      this.handle = handle;
      this.clazz = clazz;
   }

   /**
    * Performs the indexing.
    */
   public void invoke()
   {
      int n = 0;
      try
      {
         scrollableResults = getScrollableResults(session, clazz, n);
         while (scrollableResults.next() && !handle.shouldStop())
         {
            n++;
            T entity = (T) scrollableResults.get(0); // index each element
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
    * Returns the Scrollable results
    * @param session Session used to query and index the entities
    * @param clazz The type of entity to be returned by the Scrollable results
    * @param firstResult
    * @return
    */
   protected abstract ScrollableResults getScrollableResults(FullTextSession session, Class<T> clazz, int firstResult);

   /**
    * Create a query which returns instances of clazz
    * @param clazz The type of objects being returned by this query.
    * @return
    */
   protected abstract Query getQuery(FullTextSession session, Class<T> clazz);
}