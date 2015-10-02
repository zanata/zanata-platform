package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SlugEntityBase;
import org.zanata.service.IndexingService;
import javax.enterprise.event.Event;
import org.zanata.util.ServiceLocator;
import com.google.common.collect.Lists;

/**
 * This class is a hibernate event listener which listens on post commit events
 * for HProject and HProjectIteration. If it detects a HProject slug change, it
 * will perform re-indexing for all HTextFlowTargets under that project. It will
 * also fire update event for HProject and HProjectIteration with their old slug
 * in payload.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see org.zanata.webtrans.server.TranslationWorkspaceManagerImpl
 * @see IndexingServiceImpl
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class SlugEntityUpdatedListener implements PostUpdateEventListener {
    private static final long serialVersionUID = -1L;

    private static Integer slugFieldIndexInProject;
    private static Integer slugFieldIndexInIteration;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        if (!entityClass.equals(HProject.class) && !entityClass.equals(
                HProjectIteration.class)) {
            return;
        }

        SlugEntityBase slugEntityBase = SlugEntityBase.class.cast(
                event.getEntity());

        if (slugEntityBase instanceof HProject) {
            HProject project = (HProject) slugEntityBase;
            slugFieldIndexInProject = getSlugFieldIndex(slugFieldIndexInProject, event);
            String oldSlug = event.getOldState()[slugFieldIndexInProject].toString();
            String newSlug = event.getState()[slugFieldIndexInProject].toString();
            getProjectUpdateEvent().fire(new ProjectUpdate(project, oldSlug));
            reindexIfProjectSlugHasChanged(oldSlug, newSlug, project);

        } else if (slugEntityBase instanceof HProjectIteration) {
            HProjectIteration iteration =
                    (HProjectIteration) slugEntityBase;
            slugFieldIndexInIteration = getSlugFieldIndex(slugFieldIndexInIteration, event);
            String oldSlug = event.getOldState()[slugFieldIndexInIteration].toString();
            getProjectIterationUpdateEvent().fire(new ProjectIterationUpdate(
                    iteration, oldSlug));
        }
    }

    public void reindexIfProjectSlugHasChanged(String oldSlug, String newSlug,
            HProject project) {
        if (!oldSlug.equals(newSlug)) {
            log.debug("HProject [{}] changed slug. old slug: {}, new slug: {}",
                    project, oldSlug, newSlug);
            AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
            getAsyncTaskHandleManager().registerTaskHandle(handle);
            try {
                getIndexingServiceImpl().reindexHTextFlowTargetsForProject(
                        project, handle);
            } catch (Exception e) {
                log.error("exception happen in async framework", e);
            }
        }
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
    private static Integer getSlugFieldIndex(Integer slugFieldIndex, PostUpdateEvent event) {
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
        return ServiceLocator.instance().getInstance(AsyncTaskHandleManager.class);
    }

    public IndexingService getIndexingServiceImpl() {
        return ServiceLocator.instance().getInstance("indexingServiceImpl",
                IndexingService.class);
    }

    public Event<ProjectUpdate> getProjectUpdateEvent() {
        return ServiceLocator.instance().getInstance("event", Event.class);
    }

    public Event<ProjectIterationUpdate> getProjectIterationUpdateEvent() {
        return ServiceLocator.instance().getInstance("event", Event.class);
    }
}
