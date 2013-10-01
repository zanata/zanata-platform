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
import org.zanata.async.AsyncTask;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.search.AbstractIndexingStrategy;
import org.zanata.search.ClassIndexer;
import org.zanata.search.HTextFlowTargetIndexingStrategy;
import org.zanata.search.SimpleClassIndexingStrategy;
import org.zanata.service.AsyncTaskManagerService;

@Name("reindexAsync")
@Scope(ScopeType.APPLICATION)
@Startup
public class ReindexAsyncBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Logger
    private Log log;

    @In
    EntityManagerFactory entityManagerFactory;

    @In
    AsyncTaskManagerService asyncTaskManagerServiceImpl;

    private FullTextSession session;

    // we use a list to ensure predictable order
    private List<Class<?>> indexables = new ArrayList<Class<?>>();
    private LinkedHashMap<Class<?>, ReindexClassOptions> indexingOptions =
            new LinkedHashMap<Class<?>, ReindexClassOptions>();
    private Class<?> currentClass;

    private AsyncTaskHandle<Boolean> handle;

    @Create
    public void create() {
        indexables.add(HAccount.class);
        indexables.add(HGlossaryEntry.class);
        indexables.add(HGlossaryTerm.class);
        indexables.add(HProject.class);
        indexables.add(HProjectIteration.class);
        indexables.add(TransMemoryUnit.class);

        // NB we put the largest tables at the bottom, so that the small
        // tables can be indexed early
        indexables.add(HTextFlowTarget.class);

        for (Class<?> clazz : indexables) {
            indexingOptions.put(clazz, new ReindexClassOptions(clazz));
        }
    }

    /**
     * Sets reindex options for all indexable classes.
     *
     * @param purge
     *            Indicates whether to purge the indexes.
     * @param reindex
     *            Indicates whether to reindex.
     * @param optimize
     *            Indicates whether to optimize the indexes.
     */
    public void setOptions(boolean purge, boolean reindex, boolean optimize) {
        for (Class<?> c : indexables) {
            ReindexClassOptions classOptions;
            if (indexingOptions.containsKey(c)) {
                classOptions = indexingOptions.get(c);
            } else {
                classOptions = new ReindexClassOptions(c);
                indexingOptions.put(c, classOptions);
            }

            classOptions.setPurge(purge);
            classOptions.setReindex(reindex);
            classOptions.setOptimize(optimize);
        }
    }

    public List<ReindexClassOptions> getReindexOptions() {
        List<ReindexClassOptions> result = new ArrayList<ReindexClassOptions>();
        for (Class<?> clazz : indexingOptions.keySet()) {
            result.add(indexingOptions.get(clazz));
        }
        return result;
    }

    public AsyncTaskHandle<Boolean> getProcessHandle() {
        return handle;
    }

    public String getCurrentClassName() {
        if (currentClass == null) {
            return "none";
        }
        return currentClass.getSimpleName();
    }

    /**
     * Returns the number of total operations to perform
     */
    private int getTotalOperations() {
        session =
                Search.getFullTextSession((Session) entityManagerFactory
                        .createEntityManager().getDelegate());

        // set up progress counter
        int totalOperations = 0;
        for (Class<?> clazz : indexables) {
            ReindexClassOptions opts = indexingOptions.get(clazz);
            if (opts.isPurge()) {
                totalOperations++;
            }

            if (opts.isReindex()) {
                totalOperations += getIndexer(clazz).getEntityCount();
            }

            if (opts.isOptimize()) {
                totalOperations++;
            }
        }
        return totalOperations;
    }

    /**
     * Facility method to start the background process with this instance's own
     * internal process handle.
     */
    public void startProcess() {
        String taskId =
                asyncTaskManagerServiceImpl.startTask(new ReindexTask());
        this.handle = asyncTaskManagerServiceImpl.getHandle(taskId);
    }

    @SuppressWarnings("rawtypes")
    ClassIndexer getIndexer(Class<?> clazz) {
        AbstractIndexingStrategy strategy;
        // TODO add a strategy which uses TransMemoryStreamingDAO
        if (clazz.equals(HTextFlowTarget.class)) {
            strategy = new HTextFlowTargetIndexingStrategy(session);
        } else {
            strategy = new SimpleClassIndexingStrategy(clazz, session);
        }
        return new ClassIndexer(session, handle, clazz, strategy);
    }

    /**
     * Private reindex Asynchronous task. NB: Separate from the main Bean class
     * as it is not recommended to reuse async tasks.
     */
    private class ReindexTask implements
            AsyncTask<Boolean, AsyncTaskHandle<Boolean>> {
        private AsyncTaskHandle<Boolean> handle;

        @Override
        public AsyncTaskHandle<Boolean> getHandle() {
            if (handle == null) {
                handle = new AsyncTaskHandle<Boolean>();
                handle.setMaxProgress(getTotalOperations());
            }
            return handle;
        }

        @Override
        public Boolean call() throws Exception {
            // TODO this is necessary because isInProgress checks number of
            // operations, which may be 0
            // look at updating isInProgress not to care about count
            if (getHandle().getMaxProgress() == 0) {
                log.info("Reindexing aborted because there are no actions to perform (may be indexing an empty table)");
                return null;
            }
            for (Class<?> clazz : indexables) {
                if (!getHandle().isCancelled()
                        && indexingOptions.get(clazz).isPurge()) {
                    log.info("purging index for {0}", clazz);
                    currentClass = clazz;
                    session.purgeAll(clazz);
                    getHandle().increaseProgress(1);
                }
                if (!getHandle().isCancelled()
                        && indexingOptions.get(clazz).isReindex()) {
                    log.info("reindexing {0}", clazz);
                    currentClass = clazz;
                    getIndexer(clazz).index();
                }
                if (!getHandle().isCancelled()
                        && indexingOptions.get(clazz).isOptimize()) {
                    log.info("optimizing {0}", clazz);
                    currentClass = clazz;
                    session.getSearchFactory().optimize(clazz);
                    getHandle().increaseProgress(1);
                }
            }

            if (getHandle().isCancelled()) {
                log.info("index operation canceled by user");
            } else {
                if (getHandle().getCurrentProgress() != getHandle()
                        .getMaxProgress()) {
                    // @formatter: off
                    log.warn(
                            "Did not reindex the expected number of objects. Counted {0} but indexed {1}. "
                                    + "The index may be out-of-sync. "
                                    + "This may be caused by lack of sufficient memory, or by database activity during reindexing.",
                            getHandle().getMaxProgress(), getHandle()
                                    .getCurrentProgress());
                    // @formatter: on
                }

                log.info("Re-indexing finished");
            }
            return true;
        }
    }
}
