package org.zanata.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.util.Synchronized;
import org.zanata.ServerConstants;
import org.zanata.action.ReindexClassOptions;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
// not @Transactional, because the DB work happens in other threads

@Named("searchIndexManager")
@ApplicationScoped
@Synchronized(timeout = ServerConstants.DEFAULT_TIMEOUT)
public class SearchIndexManager implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SearchIndexManager.class);

    private static final long serialVersionUID = 1L;
    @Inject
    @SuppressFBWarnings(value = "SE_BAD_FIELD",
            justification = "CDI proxies are Serializable")
    private AsyncTaskHandleManager asyncTaskHandleManager;
    @Inject
    private IndexingService indexingServiceImpl;
    // we use a list to ensure predictable order
    private final List<Class<?>> indexables = new ArrayList<Class<?>>();
    private final LinkedHashMap<Class<?>, ReindexClassOptions> indexingOptions =
            new LinkedHashMap<Class<?>, ReindexClassOptions>();
    private Class<?> currentClass;
    private AsyncTaskHandle<Void> handle;

    @PostConstruct
    public void create() {
        // TODO get the list of classes from Hibernate Search
        // ie
        // FullTextSession.getSearchFactory().getStatistics().getIndexedClassNames()
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

    public AsyncTaskHandle<Void> getProcessHandle() {
        return handle;
    }
    // TODO: current class is not implemented now, should be getting the value
    // from indexingServiceImpl

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
        this.handle = new AsyncTaskHandle<Void>();
        asyncTaskHandleManager.registerTaskHandle(handle);
        try {
            indexingServiceImpl.startIndexing(indexingOptions, handle);
        } catch (Exception e) {
            // If this happens, it's because of a problem with the async
            // framework
            throw new RuntimeException(e);
        }
    }

    public void reindex(boolean purge, boolean reindex, boolean optimize)
            throws Exception {
        setOptions(purge, reindex, optimize);
        startProcess();
    }
}
