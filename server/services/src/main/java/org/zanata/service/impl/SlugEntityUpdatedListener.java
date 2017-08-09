package org.zanata.service.impl;

import static org.zanata.service.impl.EntityListenerUtil.getFieldIndex;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.hibernate.event.spi.PostCommitUpdateEventListener;
import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.zanata.events.ProjectIterationUpdate;
import org.zanata.events.ProjectUpdate;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

/**
 * This class is a hibernate event listener which listens on post commit events
 * for HProject and HProjectIteration. If it detects the slug has changed, it
 * will fire update event for HProject and HProjectIteration with their old slug
 * in payload.
 *
 * @see org.zanata.webtrans.server.HibernateIntegrator
 * @see org.zanata.webtrans.server.TranslationWorkspaceManagerImpl
 * @see IndexingServiceImpl
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SlugEntityUpdatedListener implements
        PostCommitUpdateEventListener {

    private static final long serialVersionUID = -1L;
    private Integer slugFieldIndexInProject;
    private Integer slugFieldIndexInIteration;


    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        Object entity = event.getEntity();

        if (entity instanceof HProject) {
            HProject project = (HProject) entity;
            slugFieldIndexInProject =
                    getFieldIndex(slugFieldIndexInProject, event, "slug");

            String oldSlug =
                    event.getOldState()[slugFieldIndexInProject].toString();


            fireProjectUpdateEvent(project, oldSlug);

        } else if (entity instanceof HProjectIteration) {
            HProjectIteration iteration = (HProjectIteration) entity;
            slugFieldIndexInIteration =
                    getFieldIndex(slugFieldIndexInIteration, event, "slug");

            String oldSlug =
                    event.getOldState()[slugFieldIndexInIteration].toString();


            fireProjectIterationUpdateEvent(iteration, oldSlug);
        }
    }

    @Override
    public boolean requiresPostCommitHanding(EntityPersister persister) {
        // We must return true otherwise hibernate will not treat this as post commit event
        return true;
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

    @Override
    public void onPostUpdateCommitFailed(PostUpdateEvent event) {
        // nothing
    }
}
