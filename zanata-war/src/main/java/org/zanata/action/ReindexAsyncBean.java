package org.zanata.action;

import java.util.Collection;
import java.util.HashMap;
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
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;

@Name("reindexAsync")
@Scope(ScopeType.APPLICATION)
@Startup
public class ReindexAsyncBean
{

   //TODO make this configurable
   private static final int BATCH_SIZE = 5000;

   @Logger
   private Log log;

   @In
   EntityManagerFactory entityManagerFactory;

   private FullTextSession session;

   private Set<Class<?>> indexables = new HashSet<Class<?>>();
   private HashMap<Class<?>, ReindexClassOptions> indexingOptions = new HashMap<Class<?>, ReindexClassOptions>();
   private Class<?> currentClass;

   private boolean hasError;

   private ProcessHandle handle;

   @Create
   public void create()
   {
      handle = new ProcessHandle();
      handle.setMaxProgress(0); //prevent progress indicator showing before first reindex
      hasError = false;

      // TODO: find a version of this that works:
      // indexables.addAll(StandardDeploymentStrategy.instance().getAnnotatedClasses().get(Indexed.class.getName()));
      indexables.add(HIterationProject.class);
      indexables.add(HAccount.class);
      indexables.add(HTextFlow.class);
      indexables.add(HProjectIteration.class);
      indexables.add(HTextFlowTarget.class);
      indexables.add(HGlossaryTerm.class);
      indexables.add(HGlossaryEntry.class);

      for (Class<?> clazz : indexables)
      {
         indexingOptions.put(clazz, new ReindexClassOptions(clazz));
      }
   }

   public Collection<ReindexClassOptions> getReindexOptions()
   {
      return indexingOptions.values();
   }

   public ProcessHandle getProcessHandle()
   {
      return handle;
   }

   public String getCurrentClassName()
   {
      if (currentClass == null)
      {
         return "none";
      }
      return currentClass.getSimpleName();
   }

   /**
    * Prepare to reindex lucene search index. This ensures that progress counts
    * are properly initialised before the asynchronous startReindex() method is
    * called.
    */
   public void prepareReindex()
   {
      // TODO print message? throw exception?
      if (handle.isInProgress())
         return;

      handle = new ProcessHandle();
      handle.start();

      hasError = false;

      log.info("Re-indexing started");

      session = Search.getFullTextSession((Session) entityManagerFactory.createEntityManager().getDelegate());

      // set up progress counter
      int totalOperations = 0;
      for (Class<?> clazz : indexables)
      {
         ReindexClassOptions opts = indexingOptions.get(clazz);
         if (opts.isPurge())
         {
            totalOperations++;
         }

         if (opts.isReindex())
         {
            totalOperations += (Integer) session.createCriteria(clazz).setProjection(Projections.rowCount()).list().get(0);
         }

         if (opts.isOptimize())
         {
            totalOperations++;
         }
      }
      handle.setMaxProgress(totalOperations);
      handle.setCurrentProgress(0);
   }

   /**
    * Begin reindexing lucene search index. This method should only be called
    * after a single call to prepareReindex()
    */
   @Asynchronous
   public void startReindex()
   {
      // TODO this is necessary because isInProgress checks number of operations, which may be 0
      // look at updating isInProgress not to care about count
      if (handle.maxProgress == 0) {
         handle.finish();
         log.info("Reindexing aborted because there are no actions to perform (may be indexing an empty table)");
         return;
      }
      if (!handle.isInProgress())
      {
         throw new RuntimeException("startReindex() must not be called before prepareReindex()");
      }

      for (Class<?> clazz : indexables)
      {
         if (!handle.shouldStop() && indexingOptions.get(clazz).isPurge())
         {
            log.info("purging index for {0}", clazz);
            currentClass = clazz;
            session.purgeAll(clazz);
            handle.incrementProgress(1);
         }
         if (!handle.shouldStop() && indexingOptions.get(clazz).isReindex())
         {
            log.info("reindexing {0}", clazz);
            currentClass = clazz;
            reindex(clazz);
         }
         if (!handle.shouldStop() && indexingOptions.get(clazz).isOptimize())
         {
            log.info("optimizing {0}", clazz);
            currentClass = clazz;
            session.getSearchFactory().optimize(clazz);
            handle.incrementProgress(1);
         }
      }

      if (handle.shouldStop()) {
         log.info("index operation canceled by user");
      }
      else
      {
         if (handle.getCurrentProgress() != handle.getMaxProgress())
         {
            // @formatter: off
            log.warn("Did not reindex the expected number of objects. Counted {0} but indexed {1}. "
                  + "The index may be out-of-sync. "
                  + "This may be caused by lack of sufficient memory, or by database activity during reindexing.", handle.getMaxProgress(), handle.getCurrentProgress());
            // @formatter: on
         }

         log.info("Re-indexing finished" + (hasError ? " with errors" : ""));
      }

      handle.finish();
   }

   private void reindex(Class<?> clazz)
   {
      ScrollableResults results = null;
      try
      {

         log.info("Setting manual-flush and ignore-cache for {0}", clazz);
         session.setFlushMode(FlushMode.MANUAL);
         session.setCacheMode(CacheMode.IGNORE);

         int index = 0;
         results = session.createCriteria(clazz).setFirstResult(index).setMaxResults(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
         while (results.next() && !handle.shouldStop())
         {
            index++;
            session.index(results.get(0)); // index each element
            handle.incrementProgress(1);
            if (index % BATCH_SIZE == 0)
            {
               log.info("periodic flush and clear for {0} (index {1})", clazz, index);
               session.flushToIndexes(); // apply changes to indexes
               session.clear(); // clear since the queue is processed
               results.close();
               results = session.createCriteria(clazz).setFirstResult(index).setMaxResults(BATCH_SIZE).scroll(ScrollMode.FORWARD_ONLY);
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

   // TODO merge everything below into ProcessHandle in 1.7 branch

   public class ProcessHandle
   {
      private boolean shouldStop = false;
      private int minProgress = 0;
      private int maxProgress = 100;
      private int currentProgress = 0;
      private long startTime = -1;
      private long finishTime = -1;

      public boolean isInProgress()
      {
         return this.isStarted() && !this.isFinished() && currentProgress < maxProgress;
      }

      public void stop()
      {
         shouldStop = true;
      }

      public boolean shouldStop()
      {
         return shouldStop;
      }

      public int getMaxProgress()
      {
         return maxProgress;
      }

      public void setMaxProgress(int maxProgress)
      {
         this.maxProgress = maxProgress;
      }

      public int getMinProgress()
      {
         return minProgress;
      }

      public void setMinProgress(int minProgress)
      {
         this.minProgress = minProgress;
      }

      public int getCurrentProgress()
      {
         return currentProgress;
      }

      void start()
      {
         if( !this.isInProgress() && this.startTime == -1 )
         {
            this.startTime = System.currentTimeMillis();
         }
      }

      void finish()
      {
         this.finishTime = System.currentTimeMillis();
      }

      public void setCurrentProgress(int currentProgress)
      {
         this.start(); // start if it hasn't been done yet
         this.currentProgress = currentProgress;
      }

      public void incrementProgress(int increment)
      {
         this.start(); // start if it hasn't been done yet
         this.currentProgress += increment;
      }

      public boolean isStarted()
      {
         return this.startTime != -1;
      }

      public boolean isFinished()
      {
         return this.finishTime != -1;
      }

      public long getEstimatedTimeRemaining()
      {
         if( this.startTime == -1 )
         {
            return 0;
         }

         long currentTime = System.currentTimeMillis();
         long timeElapsed = currentTime - this.startTime;
         //avoid divide by zero. Slight inaccuracy is acceptable for this approximation.
         long averageTimePerProgressUnit = timeElapsed / (this.currentProgress == 0 ? 1 : this.currentProgress);

         return averageTimePerProgressUnit * (this.maxProgress - this.currentProgress);
      }

      public long getElapsedTime()
      {
         if (!isStarted())
         {
            return 0;
         }

         if (isFinished())
         {
            return getFinishTime() - getStartTime();
         }
         else
         {
            return System.currentTimeMillis() - getStartTime();
         }
      }

      public long getStartTime()
      {
         return this.startTime;
      }

      /**
       * @return Process finish time (cancelled or otherwise), or -1 if the process hasn't finished yet.
       */
      public long getFinishTime()
      {
         return this.finishTime;
      }

      // methods to add in process handle subclass
      public boolean hasError()
      {
         return hasError;
      }
   }
}
