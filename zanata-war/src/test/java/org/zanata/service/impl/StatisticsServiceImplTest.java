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
package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import org.dbunit.operation.DatabaseOperation;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.seam.SeamAutowire;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = { "business-tests" })
public class StatisticsServiceImplTest extends ZanataDbunitJpaTest
{
   private SeamAutowire seam = SeamAutowire.instance();
   
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @BeforeMethod
   public void initializeSeam()
   {
      seam.reset()
          .use("entityManager", getEm())
          .use("session", getSession())
          .ignoreNonResolvable();
   }

   @Test
   public void getSimpleIterationStatisticsForAllLocales()
   {
      StatisticsServiceImpl statisticsService = seam.autowire(StatisticsServiceImpl.class);
      ContainerTranslationStatistics stats =
            statisticsService.getStatistics("sample-project", "1.0", false, false, new String[]{});

      // Make sure the id matches
      assertThat(stats.getId(), is("1.0"));
      // Make sure there are links
      assertThat(stats.getRefs().size(), greaterThan(0));

      // No detailed stats
      assertThat(stats.getDetailedStats(), nullValue());

      assertThat(stats.getStats().get(0).getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));

      for( TranslationStatistics transStat : stats.getStats() )
      {
         // Check that there are no word level stats
         assertThat(transStat.getUnit(), not(TranslationStatistics.StatUnit.WORD));

         // make sure counts are sane
         assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
      }
   }

   @Test
   public void getWordIterationStatisticsForAllLocales()
   {
      StatisticsServiceImpl statisticsService = seam.autowire(StatisticsServiceImpl.class);
      ContainerTranslationStatistics stats =
            statisticsService.getStatistics("sample-project", "1.0", false, true, new String[]{});

      // Make sure the id matches
      assertThat(stats.getId(), is("1.0"));
      // Make sure there are links
      assertThat(stats.getRefs().size(), greaterThan(0));

      // No detailed stats
      assertThat(stats.getDetailedStats(), nullValue());

      // Word level AND message level stats
      int wordLevel = 0;
      int mssgLevel = 0;
      for( TranslationStatistics transStat : stats.getStats() )
      {
         if( transStat.getUnit() == TranslationStatistics.StatUnit.MESSAGE )
         {
            mssgLevel++;
         }
         else if( transStat.getUnit() == TranslationStatistics.StatUnit.WORD )
         {
            wordLevel++;
         }

         // make sure counts are sane
         assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
      }

      // make sure word and message level counts are the same and > 0
      assertThat(wordLevel, greaterThan(0));
      assertThat(wordLevel, is(mssgLevel));
   }

   @Test
   public void getDetailedIterationStatisticsForSpecificLocales()
   {
      String[] locales = new String[]{"en-US", "es", "as"};

      StatisticsServiceImpl statisticsService = seam.autowire(StatisticsServiceImpl.class);
      ContainerTranslationStatistics stats =
            statisticsService.getStatistics("sample-project", "1.0", true, true, locales);

      // Make sure the id matches
      assertThat(stats.getId(), is("1.0"));
      // Make sure there are links
      assertThat(stats.getRefs().size(), greaterThan(0));

      // Detailed Stats
      assertThat(stats.getDetailedStats().size(), greaterThan(0));

      // Results returned only for specified locales
      for( TranslationStatistics transStat : stats.getStats() )
      {
         assertThat(Arrays.asList(locales), hasItem( transStat.getLocale() ));
         // make sure counts are sane
         assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
      }
   }

   @Test
   public void getSimpleDocumentStatisticsForAllLocales()
   {
      StatisticsServiceImpl statisticsService = seam.autowire(StatisticsServiceImpl.class);
      ContainerTranslationStatistics stats =
            statisticsService.getStatistics("sample-project", "1.0", "my/path/document.txt", false, new String[]{});

      // Make sure the id matches
      assertThat(stats.getId(), is("my/path/document.txt"));
      // Make sure there are links
      assertThat(stats.getRefs().size(), greaterThan(0));

      // No detailed stats
      assertThat(stats.getDetailedStats(), nullValue());

      assertThat(stats.getStats().get(0).getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));


      for( TranslationStatistics transStat : stats.getStats() )
      {
         // Check that there are no word level stats
         assertThat(transStat.getUnit(), not(TranslationStatistics.StatUnit.WORD));

         // make sure counts are sane
         assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
      }
   }

   @Test
   public void getDetailedDocumentStatisticsForSpecificLocales()
   {
      String[] locales = new String[]{"en-US", "es", "as"};

      StatisticsServiceImpl statisticsService = seam.autowire(StatisticsServiceImpl.class);
      ContainerTranslationStatistics stats =
            statisticsService.getStatistics("sample-project", "1.0", "my/path/document.txt", true, locales);

      // Make sure the id matches
      assertThat(stats.getId(), is("my/path/document.txt"));
      // Make sure there are links
      assertThat(stats.getRefs().size(), greaterThan(0));

      // No Detailed Stats
      assertThat(stats.getDetailedStats(), nullValue());

      // Results returned only for specified locales
      for( TranslationStatistics transStat : stats.getStats() )
      {
         assertThat(Arrays.asList(locales), hasItem( transStat.getLocale() ));
         // make sure counts are sane
         assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
      }
   }

}
