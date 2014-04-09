package org.zanata.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.Synchronized;
import org.zanata.ServerConstants;
import org.zanata.action.ReindexClassOptions;
import org.zanata.async.AsyncTask;
import org.zanata.async.TimedAsyncHandle;
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

@Name("searchIndexManager")
@Scope(ScopeType.APPLICATION)
@Startup
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
@Slf4j
public class SearchIndexManager implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    EntityManagerFactory entityManagerFactory;

    @In
    AsyncTaskManagerService asyncTaskManagerServiceImpl;

    // we use a list to ensure predictable order
    private final List<Class<?>> indexables = new ArrayList<Class<?>>();
    private final LinkedHashMap<Class<?>, ReindexClassOptions> indexingOptions =
            new LinkedHashMap<Class<?>, ReindexClassOptions>();
    private Class<?> currentClass;

    private TimedAsyncHandle<Void> handle;

    @Create
    public void create() {
        // TODO get the list of classes from Hibernate Search
        // ie FullTextSession.getSearchFactory().getStatistics().getIndexedClassNames()
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

    public TimedAsyncHandle<Void> getProcessHandle() {
        return handle;
    }

    public String getCurrentClassName() {
        if (currentClass == null) {
            return "none";
        }
        return currentClass.getSimpleName();
    }

    /**
     * Facility method to start the background process with this instance's own
     * internal process handle.
     */
    public void startProcess() {
        assert handle == null || handle.isDone();
        final ReindexTask reindexTask = new ReindexTask(entityManagerFactory);
        String taskId =
                asyncTaskManagerServiceImpl.startTask(reindexTask);
        this.handle = (TimedAsyncHandle) asyncTaskManagerServiceImpl.getHandle(taskId);
    }

    /**
     * Private reindex Asynchronous task. NB: Separate from the main Bean class
     * as it is not recommended to reuse async tasks.
     */
    private class ReindexTask implements
            AsyncTask<Void, TimedAsyncHandle<Void>> {
        private final EntityManagerFactory entityManagerFactory;

        @Getter
        @Nonnull
        private final TimedAsyncHandle<Void> handle;

        public ReindexTask(EntityManagerFactory entityManagerFactory) {
            this.entityManagerFactory = entityManagerFactory;
            String name = getClass().getSimpleName(); //+":"+indexingOptions
            this.handle = new TimedAsyncHandle<Void>(name);
            FullTextSession session = openFullTextSession();
            try {
                handle.setMaxProgress(getTotalOperations(session));
            } finally {
                session.close();
            }
        }

        private FullTextSession openFullTextSession() {
            return Search.getFullTextSession(entityManagerFactory
                    .createEntityManager().unwrap(Session.class));
        }

        /**
         * Returns the number of total operations to perform
         */
        private int getTotalOperations(FullTextSession session) {
            // set up progress counter
            int totalOperations = 0;
            for (Class<?> clazz : indexables) {
                ReindexClassOptions opts = indexingOptions.get(clazz);
                if (opts.isPurge()) {
                    totalOperations++;
                }

                if (opts.isReindex()) {
                    totalOperations += getIndexer(clazz).getEntityCount(session);
                }

                if (opts.isOptimize()) {
                    totalOperations++;
                }
            }
            return totalOperations;
        }

        private <T> ClassIndexer<T> getIndexer(Class<T> clazz) {
            AbstractIndexingStrategy<T> strategy;
            // TODO add a strategy which uses TransMemoryStreamingDAO
            if (clazz.equals(HTextFlowTarget.class)) {
                strategy =
                        (AbstractIndexingStrategy<T>) new HTextFlowTargetIndexingStrategy();
            } else {
                strategy = new SimpleClassIndexingStrategy<T>(clazz);
            }
            return new ClassIndexer<T>(handle, clazz, strategy);
        }

        @Override
        public Void call() throws Exception {
            FullTextSession session = openFullTextSession();
            try {
                handle.setMaxProgress(getTotalOperations(session));
                // TODO this is necessary because isInProgress checks number of
                // operations, which may be 0
                // look at updating isInProgress not to care about count
                if (getHandle().getMaxProgress() == 0) {
                    log.info("Reindexing aborted because there are no actions "
                            + "to perform (may be indexing an empty table)");
                    return null;
                }
                getHandle().startTiming();
                for (Class<?> clazz : indexables) {
                    if (!getHandle().isCancelled()
                            && indexingOptions.get(clazz).isPurge()) {
                        log.info("purging index for {}", clazz);
                        currentClass = clazz;
                        session.purgeAll(clazz);
                        getHandle().increaseProgress(1);
                    }
                    if (!getHandle().isCancelled()
                            && indexingOptions.get(clazz).isReindex()) {
                        log.info("reindexing {}", clazz);
                        currentClass = clazz;
                        getIndexer(clazz).index(session);
                    }
                    if (!getHandle().isCancelled()
                            && indexingOptions.get(clazz).isOptimize()) {
                        log.info("optimizing {}", clazz);
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
                                "Did not reindex the expected number of "
                                        + "objects. Counted {} but indexed {}. "
                                        + "The index may be out-of-sync. "
                                        + "This may be caused by lack of "
                                        + "sufficient memory, or by database "
                                        + "activity during reindexing.",
                                getHandle().getMaxProgress(), getHandle()
                                        .getCurrentProgress());
                        // @formatter: on
                    }

                    log.info("Re-indexing finished");
                }
                getHandle().finishTiming();
                return null;
            } finally {
                session.close();
            }
        }
    }
}
