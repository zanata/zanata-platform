/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.service.impl;

import static org.zanata.service.impl.EntityListenerUtil.getFieldIndex;

import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.common.EntityStatus;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.service.IndexingService;
import org.zanata.util.ServiceLocator;

/**
 * This class is a hibernate event listener which listens on post commit events
 * for HProject and HProjectIteration. If it detects a status change from/to
 * obsolete, it will perform re-indexing for all HTextFlowTargets under that
 * project/version.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see IndexingServiceImpl
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReindexChildrenEntityEventListener
        implements PostCommitUpdateEventListener {
    private static final Logger log =
            LoggerFactory.getLogger(ReindexChildrenEntityEventListener.class);
    private static final long serialVersionUID = 771080602386793595L;
    private Integer statusFieldIndexInProject;
    private Integer statusFieldIndexInIteration;

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        // We must return true otherwise hibernate will not treat this as post commit event
        return true;
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Object entity = event.getEntity();
        if (entity instanceof HProject) {
            HProject project = (HProject) entity;

            statusFieldIndexInProject =
                    getFieldIndex(statusFieldIndexInProject, event, "status");

            EntityStatus oldStatus = (EntityStatus) event
                    .getOldState()[statusFieldIndexInProject];

            reindexIfStatusChangeInvolvesObsolete(project, oldStatus);

        } else if (entity instanceof HProjectIteration) {
            HProjectIteration iteration = (HProjectIteration) entity;

            statusFieldIndexInIteration =
                    getFieldIndex(statusFieldIndexInIteration, event, "status");

            EntityStatus oldStatus = (EntityStatus) event
                    .getOldState()[statusFieldIndexInIteration];

            reindexIfStatusChangeInvolvesObsolete(iteration, oldStatus);
        }
    }

    private void reindexIfStatusChangeInvolvesObsolete(HProject project,
            EntityStatus oldStatus) {
        if (project.getStatus() == EntityStatus.OBSOLETE
                || oldStatus == EntityStatus.OBSOLETE) {
            log.debug("HProject [{}] status changed from/to obsolete");
            AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
            getAsyncTaskHandleManager().registerTaskHandle(handle);
            try {
                getIndexingServiceImpl()
                        .reindexHTextFlowTargetsForProject(project, handle);
            } catch (Exception e) {
                log.error("exception happen in async framework", e);
            }
        }
    }

    private void reindexIfStatusChangeInvolvesObsolete(
            HProjectIteration iteration, EntityStatus oldStatus) {
        if (iteration.getStatus() == EntityStatus.OBSOLETE
                || oldStatus == EntityStatus.OBSOLETE) {
            log.debug("HProjectIteration [{}] changed from/to obsolete");
            AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
            getAsyncTaskHandleManager().registerTaskHandle(handle);
            try {
                getIndexingServiceImpl()
                        .reindexHTextFlowTargetsForProjectIteration(iteration,
                                handle);
            } catch (Exception e) {
                log.error("exception happen in async framework", e);
            }
        }
    }

    public AsyncTaskHandleManager getAsyncTaskHandleManager() {
        return ServiceLocator.instance()
                .getInstance(AsyncTaskHandleManager.class);
    }

    public IndexingService getIndexingServiceImpl() {
        return ServiceLocator.instance().getInstance(IndexingService.class);
    }

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent event) {
        // nothing
    }
}
