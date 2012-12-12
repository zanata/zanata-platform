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
package org.zanata.model;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.hibernate.Session;
import org.hibernate.stat.SecondLevelCacheStatistics;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.stat.Statistics;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CacheReliabilityTest extends ZanataDbunitJpaTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      afterTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }

   @Test
   public void secondLevelCacheAccessInSameTx() throws Exception
   {
      EntityManager em = super.newEntityManagerInstance();
      SessionStatistics sessionStats = getSessionStatistics(em);
      SecondLevelCacheStatistics cacheStats = getSecondLevelCacheStatistics(em, HPerson.class.getName());

      HPerson p = em.find(HPerson.class, 3L);
      assertThat(p.getName(), is("Bob Translator"));

      em.clear();

      p = em.find(HPerson.class, 3L);
      assertThat(p.getName(), is("Bob Translator")); // Should still be bob translator
   }

   @Test
   public void secondLevelCacheAccessAfterCommit() throws Exception
   {
      EntityManager em = super.newEntityManagerInstance();
      SessionStatistics sessionStats = getSessionStatistics(em);
      SecondLevelCacheStatistics cacheStats = getSecondLevelCacheStatistics(em, HPerson.class.getName());

      HPerson p = em.find(HPerson.class, 3L);
      assertThat(p.getName(), is("Bob Translator"));

      em.close();
      em = super.newEntityManagerInstance();
      sessionStats = getSessionStatistics(em);

      p = em.find(HPerson.class, 3L);
      assertThat(p.getName(), is("Bob Translator")); // Should still be bob translator
   }

   @Test
   public void readWriteCacheTest() throws Exception
   {
      EntityManager em1 = super.newEntityManagerInstance(),
                    em2 = super.newEntityManagerInstance();

      SecondLevelCacheStatistics stats = getSecondLevelCacheStatistics(em1, HPerson.class.getName());

      em1.getTransaction().begin();
      em2.getTransaction().begin();

      SessionStatistics sesStats1 = getSessionStatistics(em1),
                        sesStats2 = getSessionStatistics(em2);

      // EM 1
      HPerson bobT = em1.find(HPerson.class, 3L);
      assertThat( bobT.getName(), is("Bob Translator"));

      // EM 2
      HPerson bobTCopy = em2.find(HPerson.class, 3L);
      assertThat(bobTCopy.getName(), is("Bob Translator"));

      // EM 1
      bobT.setName("Bob Administrator");
      bobT = em1.merge(bobT);
      em1.flush();
      assertThat( bobT.getName(), is("Bob Administrator"));

      // EM2
      bobTCopy = em2.find(HPerson.class, 3L);
      assertThat( bobTCopy.getName(), is("Bob Translator")); // Still bob Translator (even after flush)

      // EM 1
      em1.getTransaction().commit();
      em1.close();

      // EM 2
      em2.clear();
      bobTCopy = em2.find(HPerson.class, 3L);
      assertThat( bobTCopy.getName(), is("Bob Administrator")); // Bob Administrator now

      // EM 2
      em2.getTransaction().commit();
      em2.close();
   }

   private SessionStatistics getSessionStatistics( EntityManager em )
   {
      return ((Session)em.getDelegate()).getStatistics();
   }

   private SecondLevelCacheStatistics getSecondLevelCacheStatistics( EntityManager em, String regionName )
   {
      Statistics sessFactoryStats = ((Session) em.getDelegate()).getSessionFactory().getStatistics();
      sessFactoryStats.setStatisticsEnabled(true);
      return sessFactoryStats.getSecondLevelCacheStatistics(regionName);
   }
   
   private void printStats(SecondLevelCacheStatistics stats, int step)
   {
      System.out.println("#" + step);
      if( stats == null )
      {
         System.out.println("null");
         return;
      }
      System.out.println(stats);
      System.out.println("Cache Keys: " + stats.getEntries().keySet());
      //System.out.println("Cache Values: " + stats.getEntries().values());
   }

   private void printStats(SessionStatistics stats)
   {
      System.out.println("Session Keys: " + stats.getEntityKeys());
   }

}
