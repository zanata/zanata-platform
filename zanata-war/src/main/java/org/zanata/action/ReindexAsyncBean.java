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
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.log.Log;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HIterationProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.process.BackgroundProcess;
import org.zanata.process.ProcessHandle;

@Name("reindexAsync")
@Scope(ScopeType.APPLICATION)
@Startup
public class ReindexAsyncBean extends BackgroundProcess<ReindexAsyncBean.ReindexProcessHandle>
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

   private ReindexProcessHandle handle;

   @Create
   public void create()
   {
      handle = new ReindexProcessHandle();
      handle.setMaxProgress(0); //prevent progress indicator showing before first reindex
      handle.setHasError(false);

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

   /**
    * Sets reindex options for all indexable classes.
    *
    * @param purge Indicates whether to purge the indexes.
    * @param reindex Indicates whether to reindex.
    * @param optimize Indicates whether to optimize the indexes.
    */
   public void setOptions( boolean purge, boolean reindex, boolean optimize )
   {
      for( Class<?> c : indexables )
      {
         ReindexClassOptions classOptions;
         if( indexingOptions.containsKey( c ) )
         {
            classOptions = indexingOptions.get( c );
         }
         else
         {
            classOptions = new ReindexClassOptions(c);
            indexingOptions.put(c, classOptions);
         }

         classOptions.setPurge(purge);
         classOptions.setReindex(reindex);
         classOptions.setOptimize(optimize);
      }
   }

   public Collection<ReindexClassOptions> getReindexOptions()
   {
      return indexingOptions.values();
   }

   public ReindexProcessHandle getProcessHandle()
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

      handle = new ReindexProcessHandle();

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
      handle.setPrepared(true);
   }

   /**
    * Facility method to start the background process with this instance's own internal process handle.
    */
   public void startProcess()
   {
      // Invoke a self proxy
      ReindexAsyncBean self = (ReindexAsyncBean)Component.getInstance(this.getClass(), ScopeType.APPLICATION);
      self.startProcess( this.handle );
   }


   /**
    * Begin reindexing lucene search index. This method should only be called
    * after a single call to prepareReindex()
    */
   @Override
   protected void runProcess(ReindexProcessHandle handle) throws Exception
   {
      // TODO this is necessary because isInProgress checks number of operations, which may be 0
      // look at updating isInProgress not to care about count
      if (handle.getMaxProgress() == 0) {
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

         log.info("Re-indexing finished" + (handle.hasError() ? " with errors" : ""));
      }

      handle.setPrepared(false); // back to not being prepared
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
         handle.setHasError(true);
      }
      finally
      {
         if (results != null)
            results.close();
      }
   }


   public class ReindexProcessHandle extends ProcessHandle
   {
      public boolean isPrepared;
      public boolean hasError;

      public boolean isPrepared()
      {
         return isPrepared;
      }

      public void setPrepared(boolean prepared)
      {
         isPrepared = prepared;
      }

      public boolean hasError()
      {
         return hasError;
      }

      void setHasError(boolean hasError)
      {
         this.hasError = hasError;
      }
   }
}
