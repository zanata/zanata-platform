/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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
package org.zanata.jpa;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.event.spi.EventSource;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.Search;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.Zanata;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@ApplicationScoped
public class EntityManagerProducer {
    private static final Logger log =
            LoggerFactory.getLogger(EntityManagerProducer.class);

    @Inject
    @Zanata
    private EntityManagerFactory entityManagerFactory;

    @Produces
    @RequestScoped
    @Default
    // NB: This was conversation scoped before, so keep an eye out for it
    protected EntityManager createEntityManager() {
        return entityManagerFactory.createEntityManager();
    }

    protected void closeEntityManager(@Disposes EntityManager entityManager) {
        if (entityManager.isOpen()) {
            Session session = entityManager.unwrap(Session.class);
            // sometimes EntityManager.isOpen() returns true when the Session
            // is actually closed, so we ask the Session too
            if (session.isOpen()) {
                log.debug("___________ closing EntityManager: {}", entityManager);
                entityManager.close();
            } else {
                log.debug("Session is not open");
            }
        } else {
            log.debug("EntityManager is not open");
        }
    }

    @Produces
    @FullText
    @RequestScoped
    protected FullTextEntityManager createFTEntityManager(EntityManager entityManager) {
        return org.hibernate.search.jpa.Search
                .getFullTextEntityManager(entityManager);
    }

    protected void closeFTEntityManager(@Disposes @FullText FullTextEntityManager entityManager) {
        if (entityManager.isOpen()) {
            log.debug("___________ closing FullTextEntityManager: {}", entityManager);
            entityManager.close();
        }
    }

    @Produces
    @Default
    @RequestScoped
    protected Session getSession(EntityManager entityManager) {
        return entityManager.unwrap(Session.class);
    }

    protected void closeSession(@Disposes Session session) {
        if (session.isOpen()) {
            log.debug("___________ closing session: {}", session);
            session.close();
        }
    }

    @Produces
    @FullText
    @RequestScoped
    protected FullTextSession getFullTextSession(EntityManager entityManager) {
        // here we can't inject session directly because then it will be a proxy and in FullTextSessionImpl constructor, it will try to cast it to EventSource
        Session session = entityManager.unwrap(Session.class);
        return Search.getFullTextSession(session);
    }

    protected void closeFullTextSession(@Disposes @FullText FullTextSession fullTextSession) {
        if (fullTextSession.isOpen()) {
            log.debug("___________ closing FullTextSession: {}", fullTextSession);
            fullTextSession.close();
        }
    }
}
