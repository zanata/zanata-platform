package org.zanata.action;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManagerFactory;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.async.Asynchronous;
import org.jboss.seam.log.Log;
import org.zanata.model.HAccount;
import org.zanata.model.HIterationProject;
import org.zanata.model.HTextFlow;

@Name("reindexAsync")
@Scope(ScopeType.APPLICATION)
@Startup
public class ReindexAsyncBean
{

   private static final int BATCH_SIZE = 500;

   // TODO add fields for progress indicator
   // TODO add getters/setters for progress indicators

   @Logger
   private Log log;

   @In
   EntityManagerFactory entityManagerFactory;

   private FullTextSession session;

   private Set<Class<?>> indexables = new HashSet<Class<?>>();

   private boolean reindexing;

   private int objectCount;
   private int objectProgress;

   @Create
   public void create()
   {
      reindexing = false;

      // TODO: find a version of this that works:
      // indexables.addAll(StandardDeploymentStrategy.instance().getAnnotatedClasses().get(Indexed.class.getName()));
      indexables.add(HIterationProject.class);
      indexables.add(HAccount.class);
      indexables.add(HTextFlow.class);
   }

   /**
    * Begin reindexing lucene search index. This method should not be called
    * when isReindexing() returns true.
    */
   @Asynchronous
   public void reindexDatabase()
   {
      // TODO print message? throw exception?
      if (reindexing)
         return;

      reindexing = true;
      log.info("Re-indexing started");

      session = Search.getFullTextSession((Session) entityManagerFactory.createEntityManager().getDelegate());

      // set up progress counter
      objectCount = 0;
      for (Class<?> clazz : indexables)
      {
         objectCount += (Integer) session.createCriteria(clazz).setProjection(Projections.rowCount()).list().get(0);
      }
      objectProgress = 0;

      // reindex all @Indexed entities
      for (Class<?> clazz : indexables)
      {
         reindex(clazz);
      }

      if (objectCount != objectProgress)
      {
         // @formatter: off
         log.warn("Did not reindex the expected number of objects. Counted {0} but indexed {1}. "
         + "The index may be out-of-sync. "
         + "This is most likely caused by database activity during reindexing.", objectCount, objectProgress);
         // @formatter: on
      }

      log.info("Re-indexing finished");
      reindexing = false;
   }

   private void reindex(Class<?> clazz)
   {
      log.info("Re-indexing {0}", clazz);
      try
      {
         session.purgeAll(clazz);
         session.setFlushMode(FlushMode.MANUAL);
         session.setCacheMode(CacheMode.IGNORE);
         ScrollableResults results;
         Boolean processedAllResults = false;
         int currentBatchIndex = 0;


         while (!processedAllResults)
         {
            results = session.createCriteria(clazz).setFirstResult(currentBatchIndex).setMaxResults(BATCH_SIZE).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);

            int index = 0;
            while (results.next())
            {
               // TODO increment currentProgress indicator
               objectProgress++;
               index++;
               session.index(results.get(0)); // index each element
               if (index % BATCH_SIZE == 0)
               {
                  session.flushToIndexes(); // apply changes to indexes
                  session.clear(); // clear since the queue is processed
               }
            }
            results.close();
            processedAllResults = (index < BATCH_SIZE);
            currentBatchIndex += BATCH_SIZE;
         }
      }
      catch (Exception e)
      {
         log.warn("Unable to index objects of type {0}", e, clazz.getName());
      }
   }

   /**
    * @return true if a reindex operation is running, false otherwise
    */
   public boolean isReindexing()
   {
      return reindexing;
   }

   /**
    * @return the total number of objects to be reindexed
    */
   public int getObjectCount()
   {
      return objectCount;
   }

   /**
    * @return the number of objects that have been reindexed
    */
   public int getObjectProgress()
   {
      return objectProgress;
   }

}
