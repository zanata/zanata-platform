package org.zanata.webtrans.server;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jboss.seam.Component;
import org.jboss.seam.contexts.Contexts;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class HibernateIntegrator implements Integrator {
    @Override
    public void integrate(Configuration configuration,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
        if (Contexts.isApplicationContextActive()) {
            final EventListenerRegistry eventListenerRegistry =
                    serviceRegistry.getService(EventListenerRegistry.class);
            TranslationUpdateListener updateListener =
                    (TranslationUpdateListener) Component
                            .getInstance(TranslationUpdateListener.class);
            log.info("register event listener: {}", updateListener);
            eventListenerRegistry.appendListeners(EventType.POST_UPDATE,
                    updateListener);
            eventListenerRegistry.appendListeners(EventType.POST_INSERT,
                    updateListener);
        }
    }

    @Override
    public void integrate(MetadataImplementor metadata,
            SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    }

    @Override
    public void disintegrate(SessionFactoryImplementor sessionFactory,
            SessionFactoryServiceRegistry serviceRegistry) {
    }
}
