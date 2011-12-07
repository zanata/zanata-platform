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
   @Logger
   private Log log;

   @In
   EntityManagerFactory entityManagerFactory;

   private FullTextSession session;

   private Set<Class<?>> indexables = new HashSet<Class<?>>();

   private boolean reindexing;

   private int objectCount;
   private int objectProgress;
   private boolean hasError;

   @Create
   public void create()
   {
      reindexing = false;
      hasError = false;

      objectCount = 0;
      objectProgress = 0;

      // TODO: find a version of this that works:
      // indexables.addAll(StandardDeploymentStrategy.instance().getAnnotatedClasses().get(Indexed.class.getName()));
      indexables.add(HIterationProject.class);
      indexables.add(HAccount.class);
      indexables.add(HTextFlow.class);
   }

   /**
    * Prepare to reindex lucene search index. This ensures that progress counts
    * are properly initialised before the asynchronous startReindex() method is
    * called.
    */
   public void prepareReindex()
   {
      // TODO print message? throw exception?
      if (reindexing)
         return;

      hasError = false;
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
   }

   /**
    * Begin reindexing lucene search index. This method should only be called
    * after a single call to prepareReindex()
    */
   @Asynchronous
   public void startReindex()
   {
      if (!reindexing)
      {
         throw new RuntimeException("startReindex() must not be called before prepareReindex()");
      }

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

      log.info("Re-indexing finished" + (hasError ? " with errors" : ""));
      reindexing = false;
   }

   private void reindex(Class<?> clazz)
   {
      log.info("Re-indexing {0}", clazz);

      ScrollableResults results = null;
      try
      {
         session.purgeAll(clazz);
         session.setFlushMode(FlushMode.MANUAL);
         session.setCacheMode(CacheMode.IGNORE);

         results = session.createCriteria(clazz).setFetchSize(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);

         int index = 0;
         while (results.next())
         {
            objectProgress++;
            index++;
            session.index(results.get(0)); // index each element
            if (index % BATCH_SIZE == 0)
            {
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
            }
         }
         session.flushToIndexes(); // apply changes to indexes
         session.clear(); // clear since the queue is processed
      }
      catch (Exception e)
      {
         log.warn("Unable to index objects of type {0}", e, clazz.getName());
         hasError = true;
      }
      finally
      {
         if (results != null)
            results.close();
      }
   }

   /**
    * @return true if a reindex operation is running, false otherwise
    */
   public boolean isReindexing()
   {
      return reindexing;
   }

   public boolean hasError()
   {
      return hasError;
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
