package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.async.AsyncTaskHandle;
import org.zanata.async.AsyncTaskHandleManager;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SlugEntityBase;
import org.zanata.service.IndexingService;
import org.zanata.util.Event;

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
@Name("slugEntityUpdatedListener")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class SlugEntityUpdatedListener implements PostUpdateEventListener {
    private static final long serialVersionUID = -1L;
    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @In
    private IndexingService indexingServiceImpl;

    @In("event")
    private Event<ProjectUpdate> projectUpdateEvent;

    @In("event")
    private Event<ProjectIterationUpdate> projectIterationUpdateEvent;

    private Integer index;

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Class<?> entityClass = event.getEntity().getClass();
        if (!entityClass.equals(HProject.class) && !entityClass.equals(
                HProjectIteration.class)) {
            return;
        }

        int index = getSlugFieldIndex(event);
        String oldSlug = event.getOldState()[index].toString();
        String newSlug = event.getState()[index].toString();

        SlugEntityBase slugEntityBase = SlugEntityBase.class.cast(
                event.getEntity());

        if (slugEntityBase instanceof HProject) {
            HProject project = (HProject) slugEntityBase;
            projectUpdateEvent.fire(new ProjectUpdate(project, oldSlug));
            reindexIfProjectSlugHasChanged(oldSlug, newSlug, project);

        } else if (slugEntityBase instanceof HProjectIteration) {
            HProjectIteration iteration =
                    (HProjectIteration) slugEntityBase;
            projectIterationUpdateEvent.fire(new ProjectIterationUpdate(
                    iteration, oldSlug));
        }
    }

    public void reindexIfProjectSlugHasChanged(String oldSlug, String newSlug,
            HProject project) {
        if (!oldSlug.equals(newSlug)) {
            log.debug("HProject [{}] changed slug. old slug: {}, new slug: {}",
                    project, oldSlug, newSlug);
            AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
            asyncTaskHandleManager.registerTaskHandle(handle);
            try {
                indexingServiceImpl.reindexHTextFlowTargetssForProject(
                        project, handle);
            } catch (Exception e) {
                log.error("exception happen in async framework", e);
            }
        }
    }

    private Integer getSlugFieldIndex(PostUpdateEvent event) {
        if (index != null) {
            return index;
        }
        String[] propertyNames = event.getPersister().getPropertyNames();
        int i;
        for (i = 0; i < propertyNames.length; i++) {
            String propertyName = propertyNames[i];
            if (propertyName.equals("slug")) {
                index = i;
                return index;
            }
        }
        throw new IllegalStateException("can not find slug index in property");
    }
}
