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
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.SlugEntityBase;
import org.zanata.service.IndexingService;
import org.zanata.webtrans.server.TranslationWorkspaceManager;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import com.google.common.base.Throwables;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("slugUpdatedListener")
@Scope(ScopeType.APPLICATION)
@AutoCreate
@Slf4j
public class SlugUpdatedListener implements PostUpdateEventListener {
    private static final long serialVersionUID = -1L;
    @In
    private AsyncTaskHandleManager asyncTaskHandleManager;

    @In
    private IndexingService indexingServiceImpl;

    @In
    private TranslationWorkspaceManager translationWorkspaceManager;

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
        if (oldSlug.equals(newSlug)) {
            return;
        }
        SlugEntityBase slugEntityBase = SlugEntityBase.class.cast(
                event.getEntity());
        log.debug("Slug entity [{}] changed slug. old slug: {}, new slug: {}",
                slugEntityBase, oldSlug, newSlug);

        AsyncTaskHandle<Void> handle = new AsyncTaskHandle<>();
        asyncTaskHandleManager.registerTaskHandle(handle);

        try {
            indexingServiceImpl.reindexSlugEntity(slugEntityBase, handle);
        }
        catch (Exception e) {
            log.error("exception happen in async framework", e);
        }
        // TODO pahuang need to send workspace update event to all open workspace and let user redirect to new url
        // translationWorkspaceManager.tryGetWorkspace(new WorkspaceId(new ProjectIterationId(oldSlug, null, )))
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
