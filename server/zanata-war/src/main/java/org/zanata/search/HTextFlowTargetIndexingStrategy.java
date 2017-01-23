/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.search;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.search.FullTextSession;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.dao.HTextFlowTargetStreamingDAO;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;

/**
 * Indexing strategy specific to HTextFlowTargets. This indexing strategy
 * eagerly loads all of HTextFlowTarget's indexable relationships and fetches
 * its results in a memory-efficient manner.
 *
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class HTextFlowTargetIndexingStrategy
        extends AbstractIndexingStrategy<HTextFlowTarget> {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(HTextFlowTargetIndexingStrategy.class);

    public HTextFlowTargetIndexingStrategy() {
        super(HTextFlowTarget.class);
    }

    @Override
    protected void onEntityIndexed(int n, FullTextSession session) {
        // Nothing to do
    }

    @Override
    protected ScrollableResults queryResults(int ignoredOffset,
            FullTextSession session) {
        // TODO move this query into something like HTextFlowTargetStreamingDAO
        Query query = session.createQuery(
                "from HTextFlowTarget tft join fetch tft.locale join fetch tft.textFlow join fetch tft.textFlow.document join fetch tft.textFlow.document.locale join fetch tft.textFlow.document.projectIteration join fetch tft.textFlow.document.projectIteration.project");
        query.setFetchSize(Integer.MIN_VALUE);
        return query.scroll(ScrollMode.FORWARD_ONLY);
    }

    public void reindexForProject(HProject project, FullTextSession session,
            AsyncTaskHandle handle) {
        // it must use the same session in the DAO and to do the indexing
        HTextFlowTargetStreamingDAO dao =
                new HTextFlowTargetStreamingDAO(HTextFlowTarget.class, session);
        ScrollableResults scrollableResults =
                dao.getTargetsWithAllFieldsEagerlyFetchedForProject(project);
        reindexScrollableResultSet(session, scrollableResults, handle);
    }

    private static void reindexScrollableResultSet(FullTextSession session,
            ScrollableResults scrollableResults, AsyncTaskHandle handle) {
        session.setFlushMode(FlushMode.MANUAL);
        session.setCacheMode(CacheMode.IGNORE);
        int rowNum = 0;
        try {
            while (scrollableResults.next()) {
                rowNum++;
                HTextFlowTarget entity =
                        (HTextFlowTarget) scrollableResults.get(0);
                // TODO pahuang do I need to purge first then reindex?
                session.index(entity);
                if (handle != null) {
                    handle.increaseProgress(1);
                }
                if (rowNum % sessionClearBatchSize == 0) {
                    log.info(
                            "periodic flush and clear for HTextFlowTarget (n={})",
                            rowNum);
                    session.flushToIndexes(); // apply changes to indexes
                    session.clear(); // clear since the queue is processed
                }
            }
        } finally {
            if (scrollableResults != null) {
                scrollableResults.close();
            }
        }
        session.flushToIndexes(); // apply changes to indexes
        session.clear(); // clear since the queue is processed
    }

    public void reindexForProjectVersion(HProjectIteration projectIteration,
            FullTextSession session, AsyncTaskHandle<Void> handle) {
        // it must use the same session in the DAO and to do the indexing
        HTextFlowTargetStreamingDAO dao =
                new HTextFlowTargetStreamingDAO(HTextFlowTarget.class, session);
        ScrollableResults scrollableResults =
                dao.getTargetsWithAllFieldsEagerlyFetchedForProjectIteration(
                        projectIteration);
        reindexScrollableResultSet(session, scrollableResults, handle);
    }
}
