package org.zanata.search;

import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;
import org.zanata.async.AsyncTaskHandle;

/**
 * Base indexing strategy.
 *
 * @param <T>
 *            The type of object that this indexing strategy handles.
 */
public abstract class AbstractIndexingStrategy<T> {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AbstractIndexingStrategy.class);
    protected static final int sessionClearBatchSize = 1000;
    private ScrollableResults scrollableResults;
    private final Class<T> entityType;

    /**
     * @param entityType
     *            The type of entity to be returned by the Scrollable results
     */
    public AbstractIndexingStrategy(Class<T> entityType) {
        this.entityType = entityType;
    }

    /**
     * Performs the indexing.
     */
    public void invoke(AsyncTaskHandle handle, FullTextSession session) {
        int rowNum = 0;
        scrollableResults = queryResults(rowNum, session);
        try {
            while (scrollableResults.next()) {
                if (handle != null && handle.isCancelled()) {
                    break;
                }
                rowNum++;
                T entity = (T) scrollableResults.get(0);
                session.index(entity);
                if (handle != null) {
                    handle.increaseProgress(1);
                }
                if (rowNum % sessionClearBatchSize == 0) {
                    log.info("periodic flush and clear for {} (n={})",
                            entityType, rowNum);
                    session.flushToIndexes(); // apply changes to indexes
                    session.clear(); // clear since the queue is processed
                }
                onEntityIndexed(rowNum, session);
            }
        } finally {
            if (scrollableResults != null) {
                scrollableResults.close();
            }
        }
    }

    /**
     * Callback method that is called every time an entity is indexed.
     *
     * @param n
     *            The entity number that was indexed.
     */
    protected abstract void onEntityIndexed(int n, FullTextSession session);

    /**
     * Returns the Scrollable results for instances of clazz
     *
     * @param offset
     * @return
     */
    protected abstract ScrollableResults queryResults(int offset,
            FullTextSession session);

    Class<T> getEntityType() {
        return entityType;
    }

    ScrollableResults getScrollableResults() {
        return scrollableResults;
    }

    void setScrollableResults(ScrollableResults scrollableResults) {
        this.scrollableResults = scrollableResults;
    }
}
