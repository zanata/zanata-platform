package org.zanata.service.impl;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.common.EntityStatus;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SlugEntityBase;
import org.zanata.service.IndexingService;
import org.zanata.util.ServiceLocator;

import com.google.common.collect.Lists;

/**
 * This class is a hibernate event listener which listens on post commit events
 * for HProject and HProjectIteration. If it detects the status becoming
 * obsolete, it will perform re-indexing for all HTextFlowTargets under that
 * project/version. It will also fire update event for HProject and
 * HProjectIteration with their old slug in payload.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see org.zanata.webtrans.server.TranslationWorkspaceManagerImpl
 * @see IndexingServiceImpl
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SlugEntityUpdatedListener implements PostUpdateEventListener {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SlugEntityUpdatedListener.class);

    private static final long serialVersionUID = -1L;
    private Integer slugFieldIndexInProject;
    private Integer slugFieldIndexInIteration;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        if (!entityClass.equals(HProject.class)
                && !entityClass.equals(HProjectIteration.class)) {
            return;
        }
        SlugEntityBase slugEntityBase =
                SlugEntityBase.class.cast(event.getEntity());
        if (slugEntityBase instanceof HProject) {
            HProject project = (HProject) slugEntityBase;
            slugFieldIndexInProject =
                    getSlugFieldIndex(slugFieldIndexInProject, event);
            String oldSlug =
                    event.getOldState()[slugFieldIndexInProject].toString();

            fireProjectUpdateEvent(project, oldSlug);
            reindexIfStatusBecameObsolete(project);

        } else if (slugEntityBase instanceof HProjectIteration) {
            HProjectIteration iteration = (HProjectIteration) slugEntityBase;
            slugFieldIndexInIteration =
                    getSlugFieldIndex(slugFieldIndexInIteration, event);
            String oldSlug =
                    event.getOldState()[slugFieldIndexInIteration].toString();

            fireProjectIterationUpdateEvent(iteration, oldSlug);
            reindexIfStatusBecameObsolete(iteration);
        }
    }

    private void reindexIfStatusBecameObsolete(HProjectIteration iteration) {
        if (iteration.getStatus() == EntityStatus.OBSOLETE) {
            log.debug("HProjectIteration [{}] became obsolete");
            AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
            getAsyncTaskHandleManager().registerTaskHandle(handle);
            try {
                getIndexingServiceImpl()
                        .reindexHTextFlowTargetsForProjectIteration(iteration, handle);
            } catch (Exception e) {
                log.error("exception happen in async framework", e);
            }
        }
    }

    private void reindexIfStatusBecameObsolete(HProject project) {
        if (project.getStatus() == EntityStatus.OBSOLETE) {
            log.debug("HProject [{}] became obsolete");
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

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        // TODO um, no?
        return false;
    }

    private void fireProjectIterationUpdateEvent(HProjectIteration iteration,
            String oldSlug) {
        // TODO use Event.fire()
        BeanManagerProvider.getInstance().getBeanManager()
                .fireEvent(new ProjectIterationUpdate(iteration, oldSlug));
    }

    private void fireProjectUpdateEvent(HProject project, String oldSlug) {
        // TODO use Event.fire()
        BeanManagerProvider.getInstance().getBeanManager()
                .fireEvent(new ProjectUpdate(project, oldSlug));
    }

    /**
     * Try to locate index for field slug in the entity. We try to optimize a
     * bit here since the index should be consistent and only need to be looked
     * up once. If the given index is not null, it means it has been looked up
     * and set already so we just return that value. Otherwise it will look it
     * up in hibernate persister and return the index value.
     *
     * @param slugFieldIndex
     *            if not null it will be the index to use
     * @param event
     *            post update event for an entity
     * @return looked up index for slug field for the entity
     */
    private static Integer getSlugFieldIndex(Integer slugFieldIndex,
            PostUpdateEvent event) {
        if (slugFieldIndex != null) {
            return slugFieldIndex;
        }
        String[] propertyNames = event.getPersister().getPropertyNames();
        int i;
        for (i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if (propertyName.equals("slug")) {
                return i;
            }
        }
        log.error("can not find slug index in entity [{}] properties [{}]",
                event.getEntity(), Lists.newArrayList(propertyNames));
        throw new IllegalStateException(
                "can not find slug index in entity properties");
    }

    public AsyncTaskHandleManager getAsyncTaskHandleManager() {
        return ServiceLocator.instance()
                .getInstance(AsyncTaskHandleManager.class);
    }

    public IndexingService getIndexingServiceImpl() {
        return ServiceLocator.instance().getInstance(IndexingService.class);
    }
}
