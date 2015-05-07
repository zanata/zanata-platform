package org.zanata.webtrans.server;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jboss.seam.contexts.Contexts;
import lombok.extern.slf4j.Slf4j;
import org.zanata.service.impl.SlugEntityUpdatedListener;
import org.zanata.util.ServiceLocator;

/**
 * Hibernate SPI. Register event listener for entity lifecycle events.
 *
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
                    ServiceLocator.instance().getInstance(
                            TranslationUpdateListener.class);
            log.info("register event listener: {}", updateListener);
            // We have to use POST_UPDATE not POST_UPDATE_COMMIT. Because we
            // still need to access some other entities to make transunit. After
            // commit the transaction is closed.
            eventListenerRegistry.appendListeners(EventType.POST_UPDATE,
                    updateListener);
            eventListenerRegistry.appendListeners(EventType.POST_INSERT,
                    updateListener);
            eventListenerRegistry.appendListeners(EventType.POST_COMMIT_UPDATE,
                    new SlugEntityUpdatedListener());
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
