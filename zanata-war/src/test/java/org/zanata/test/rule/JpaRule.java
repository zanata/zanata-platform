/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.test.rule;

import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.Map;

/**
 * A JUnit rule which provides commonly required JPA artifacts such as: an
 * Entity Manager Factory, an Entity manager, a hibernate Session.
 *
 * This rule must be used as both a Class and method level rule. The entity
 * manager factory will get initialized at the class level, while the entity
 * manager will get refreshed before each test method.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class JpaRule extends FunctionalTestRule {

    private static final Logger log = LoggerFactory.getLogger(JpaRule.class);
    private static final String PERSIST_NAME = "zanataDatasourcePU";

    private static EntityManagerFactory emf;

    protected EntityManager em;

    public JpaRule() {
        beforeClass(() -> initializeEMF());
        afterClass(() -> shutdownEMF());

        before(() -> setupEM());
        after(() -> shutdownEM());
    }

    public static void initializeEMF() {
        log.debug("Initializing EMF");
        emf =
                Persistence.createEntityManagerFactory(PERSIST_NAME,
                        createPropertiesMap());
    }

    protected static Map<?, ?> createPropertiesMap() {
        return null;
    }

    public static void shutdownEMF() {
        log.debug("Shutting down EMF");
        emf.close();
        emf = null;
    }

    public void setupEM() {
        log.debug("Setting up EM");
        emf.getCache().evictAll();
        em = emf.createEntityManager();
        em.getTransaction().begin();
    }

    public void shutdownEM() {
        log.debug("Shutting down EM");
        em.getTransaction().rollback();
        if (em.isOpen()) {
            em.close();
        }
        em = null;
        emf.getCache().evictAll();
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public EntityManager getEntityManager() {
        return em;
    }

    public Session getSession() {
        return em.unwrap(Session.class);
    }
}
