package org.zanata.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.EntityManagerFactory;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
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
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TMTranslationUnit;
import org.zanata.process.RunnableProcess;
import org.zanata.search.AbstractIndexingStrategy;
import org.zanata.search.ClassIndexer;
import org.zanata.search.HTextFlowTargetIndexingStrategy;
import org.zanata.search.IndexerProcessHandle;
import org.zanata.service.ProcessManagerService;

@Name("reindexAsync")
@Scope(ScopeType.APPLICATION)
@Startup
public class ReindexAsyncBean extends RunnableProcess<IndexerProcessHandle> implements Serializable
{
   private static final long serialVersionUID = 1L;

   @Logger
   private Log log;

   @In
   EntityManagerFactory entityManagerFactory;

   @In
   ProcessManagerService processManagerServiceImpl;

   private FullTextSession session;

   // we use a list to ensure predictable order
   private List<Class<?>> indexables = new ArrayList<Class<?>>();
   private LinkedHashMap<Class<?>, ReindexClassOptions> indexingOptions = new LinkedHashMap<Class<?>, ReindexClassOptions>();
   private Class<?> currentClass;

   private IndexerProcessHandle handle;

   @Create
   public void create()
   {
      handle = new IndexerProcessHandle(0);

      indexables.add(HAccount.class);
      indexables.add(HGlossaryEntry.class);
      indexables.add(HGlossaryTerm.class);
      indexables.add(HProject.class);
      indexables.add(HProjectIteration.class);
      indexables.add(TMTranslationUnit.class);

      // NB we put the largest tables at the bottom, so that the small
      // tables can be indexed early
      indexables.add(HTextFlow.class);
      indexables.add(HTextFlowTarget.class);

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

   public List<ReindexClassOptions> getReindexOptions()
   {
      List<ReindexClassOptions> result = new ArrayList<ReindexClassOptions>();
      for (Class<?> clazz : indexingOptions.keySet())
      {
         result.add(indexingOptions.get(clazz));
      }
      return result;
   }

   public IndexerProcessHandle getProcessHandle()
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
    * are properly initialised before the asynchronous run() method is
    * called.
    */
   private void prepareReindex()
   {
      // TODO print message? throw exception?
      if (handle.isInProgress())
         return;

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
            totalOperations += getIndexer(clazz).getEntityCount(session, clazz);
         }

         if (opts.isOptimize())
         {
            totalOperations++;
         }
      }
      handle = new IndexerProcessHandle(totalOperations);
   }

   /**
    * Facility method to start the background process with this instance's own internal process handle.
    */
   public void startProcess()
   {
      prepareReindex();
      // Invoke a self proxy
      processManagerServiceImpl.startProcess(this, this.handle);
   }

   @SuppressWarnings("rawtypes")
   ClassIndexer getIndexer(Class<?> clazz)
   {
      if( clazz.equals( HTextFlowTarget.class ) )
      {
         return new ClassIndexer<HTextFlowTarget>() {
            @Override
            public AbstractIndexingStrategy<HTextFlowTarget> createIndexingStrategy(FullTextSession session, IndexerProcessHandle handle, Class clazz)
            {
               return new HTextFlowTargetIndexingStrategy(session, handle, clazz);
            }
         };
      }
      else
      {
         return new ClassIndexer();
      }
   }

   /**
    * Begin reindexing lucene search index. This method should only be called
    * after a single call to prepareReindex()
    */
   @Override
   protected void run(IndexerProcessHandle handle) throws Exception
   {
      // TODO this is necessary because isInProgress checks number of operations, which may be 0
      // look at updating isInProgress not to care about count
      if (handle.getMaxProgress() == 0)
      {
         log.info("Reindexing aborted because there are no actions to perform (may be indexing an empty table)");
         return;
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
            getIndexer(clazz).index(session, handle, clazz);
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
   }
}
