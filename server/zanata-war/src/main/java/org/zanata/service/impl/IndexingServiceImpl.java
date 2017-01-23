/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import java.util.Map;
import java.util.concurrent.Future;
import javax.enterprise.context.RequestScoped;
import javax.persistence.EntityManagerFactory;
import org.apache.deltaspike.jpa.api.entitymanager.PersistenceUnitName;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.action.ReindexClassOptions;
import org.zanata.async.Async;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskResult;
import org.zanata.dao.HTextFlowTargetStreamingDAO;
import org.zanata.model.HProject;
import org.zanata.model.HTextFlowTarget;
import org.zanata.search.AbstractIndexingStrategy;
import org.zanata.search.ClassIndexer;
import org.zanata.search.HTextFlowTargetIndexingStrategy;
import org.zanata.search.SimpleClassIndexingStrategy;
import org.zanata.service.IndexingService;
import org.zanata.util.Zanata;
// Not @Transactional, because we manage EntityManager directly

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("indexingServiceImpl")
@RequestScoped
public class IndexingServiceImpl implements IndexingService {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(IndexingServiceImpl.class);

    @Inject
    @Zanata
    private EntityManagerFactory entityManagerFactory;
    @Inject
    private HTextFlowTargetStreamingDAO hTextFlowTargetStreamingDAO;

    @Override
    @Async
    public Future<Void> startIndexing(
            Map<Class<?>, ReindexClassOptions> indexingOptions,
            AsyncTaskHandle<Void> handle) throws Exception {
        FullTextSession session = openFullTextSession();
        try {
            handle.setMaxProgress(
                    getTotalOperations(session, indexingOptions, handle));
            // TODO this is necessary because isInProgress checks number of
            // operations, which may be 0
            // look at updating isInProgress not to care about count
            if (handle.getMaxProgress() == 0) {
                log.info(
                        "Reindexing aborted because there are no actions to perform (may be indexing an empty table)");
                return AsyncTaskResult.taskResult();
            }
            for (Class<?> clazz : indexingOptions.keySet()) {
                if (!handle.isCancelled()
                        && indexingOptions.get(clazz).isPurge()) {
                    log.info("purging index for {}", clazz);
                    session.purgeAll(clazz);
                    handle.increaseProgress(1);
                }
                if (!handle.isCancelled()
                        && indexingOptions.get(clazz).isReindex()) {
                    log.info("reindexing {}", clazz);
                    // currentClass = clazz;
                    getIndexer(clazz, handle).index(session);
                }
                if (!handle.isCancelled()
                        && indexingOptions.get(clazz).isOptimize()) {
                    log.info("optimizing {}", clazz);
                    // currentClass = clazz;
                    session.getSearchFactory().optimize(clazz);
                    handle.increaseProgress(1);
                }
            }
            if (handle.isCancelled()) {
                log.info("index operation canceled by user");
            } else {
                if (handle.getCurrentProgress() != handle.getMaxProgress()) {
                    // @formatter: off
                    log.warn(
                            "Did not reindex the expected number of objects. Counted {} but indexed {}. The index may be out-of-sync. This may be caused by lack of sufficient memory, or by database activity during reindexing.",
                            handle.getMaxProgress(),
                            handle.getCurrentProgress());
                    // @formatter: on
                }
                log.info("Re-indexing finished");
            }
        } finally {
            session.close();
        }
        return AsyncTaskResult.taskResult();
    }

    private FullTextSession openFullTextSession() {
        return Search.getFullTextSession(entityManagerFactory
                .createEntityManager().unwrap(Session.class));
    }

    /**
     * Returns the number of total operations to perform
     */
    private int getTotalOperations(FullTextSession session,
            Map<Class<?>, ReindexClassOptions> indexingOptions,
            AsyncTaskHandle handle) {
        // set up progress counter
        int totalOperations = 0;
        for (Class<?> clazz : indexingOptions.keySet()) {
            ReindexClassOptions opts = indexingOptions.get(clazz);
            if (opts.isPurge()) {
                totalOperations++;
            }
            if (opts.isReindex()) {
                totalOperations +=
                        getIndexer(clazz, handle).getEntityCount(session);
            }
            if (opts.isOptimize()) {
                totalOperations++;
            }
        }
        return totalOperations;
    }

    private <T> ClassIndexer<T> getIndexer(Class<T> clazz,
            AsyncTaskHandle handle) {
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
    @Async
    public Future<Void> reindexHTextFlowTargetsForProject(HProject hProject,
            AsyncTaskHandle<Void> handle) throws Exception {
        FullTextSession session = openFullTextSession();
        try {
            Long entityCount =
                    getHTextFlowTargetCountForProject(hProject, session);
            handle.setMaxProgress(entityCount.intValue());
            // TODO this is necessary because isInProgress checks number of
            // operations, which may be 0
            // look at updating isInProgress not to care about count
            if (handle.getMaxProgress() == 0) {
                log.info(
                        "Reindexing aborted because there are no actions to perform (may be indexing an empty table)");
                return AsyncTaskResult.taskResult();
            }
            HTextFlowTargetIndexingStrategy indexingStrategy =
                    new HTextFlowTargetIndexingStrategy();
            indexingStrategy.reindexForProject(hProject, session, handle);
            if (handle.getCurrentProgress() != handle.getMaxProgress()) {
                // @formatter: off
                log.warn(
                        "Did not reindex the expected number of objects. Counted {} but indexed {}. The index may be out-of-sync. This may be caused by lack of sufficient memory, or by database activity during reindexing.",
                        handle.getMaxProgress(), handle.getCurrentProgress());
                // @formatter: on
            }
            log.info(
                    "Re-indexing HTextFlowTarget for slug change: [{}] finished",
                    hProject);
        } finally {
            session.close();
        }
        return AsyncTaskResult.taskResult();
    }

    private static Long getHTextFlowTargetCountForProject(HProject hProject,
            FullTextSession session) {
        return (Long) session
                .createQuery(
                        "select count(*) from HTextFlowTarget tft where tft.textFlow.document.projectIteration.project = :project")
                .setParameter("project", hProject).uniqueResult();
    }
}
