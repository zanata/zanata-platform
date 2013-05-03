/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.provider;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.Session;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

/**
 * Provides JPA facilities.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class JPAProvider
{
   private static final String PERSIST_NAME = "zanataTestDatasourcePU";

   private static EntityManagerFactory emf;

   protected EntityManager em;

   Log log = Logging.getLog(JPAProvider.class);

   public void setupEM()
   {
      log.debug("Setting up EM");
      em = emf.createEntityManager();
      em.getTransaction().begin();
   }

   public void shutdownEM()
   {
      log.debug("Shutting down EM");
      clearHibernateSecondLevelCache();
      em.getTransaction().rollback();
      em = null;
   }

   public EntityManager getEm()
   {
      return em;
   }

   public Session getSession()
   {
      return (Session) em.getDelegate();
   }

   public void initializeEMF()
   {
      log.debug("Initializing EMF");
      emf = Persistence.createEntityManagerFactory(PERSIST_NAME);
   }

   public void shutDownEMF()
   {
      log.debug("Shutting down EMF");
      emf.close();
      emf = null;
   }

   /**
    * Commits the changes on the current session and starts a new one.
    * This method is useful whenever multi-session tests are needed.
    *
    * @return The newly started session
    */
   public Session newSession()
   {
      em.getTransaction().commit();
      setupEM();
      return getSession();
   }

   /**
    * This method is used to test multiple Entity Managers (or hibernate sessions)
    * working together simultaneously. Use {@link org.zanata.ZanataJpaTest#getEm()}
    * for all other tests.
    *
    * @return A new instance of an entity manager.
    */
   public EntityManager newEntityManagerInstance()
   {
      return emf.createEntityManager();
   }

   /**
    * Clears the Hibernate Second Level cache.
    */
   public void clearHibernateSecondLevelCache()
   {
      /*SessionFactory sessionFactory = ((Session)em.getDelegate()).getSessionFactory();
      try
      {
         sessionFactory.getCache().evictEntityRegions();
         sessionFactory.getCache().evictCollectionRegions();
      }
      catch (Exception e)
      {
         System.out.println(" *** Cache Exception "+ e.getMessage());
      }*/
   }
}
