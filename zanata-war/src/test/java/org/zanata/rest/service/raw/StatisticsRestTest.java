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
package org.zanata.rest.service.raw;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import java.util.Arrays;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.common.TranslationStatistics;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Test(groups = {"seam-tests"})
public class StatisticsRestTest extends ZanataRawRestTest
{
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void getIterationStatisticsXml() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jaxbUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("1.0"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats(), nullValue()); // No detailed stats
            assertThat(stats.getStats().size(), greaterThan(0));

            // No word level stats
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(transStat.getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getIterationStatisticsXmlWithDetails() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            request.setQueryString("word=true&detail=true&locale=en-US&locale=as&locale=es");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jaxbUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("1.0"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats().size(), greaterThan(0));
            assertThat(stats.getStats().size(), greaterThan(0));

            for( TranslationStatistics transStat : stats.getStats() )
            {
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }

            // Results returned only for specified locales
            String[] expectedLocales = new String[]{"en-US", "as", "es"};
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(Arrays.asList(expectedLocales), hasItem( transStat.getLocale() ));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getDocumentStatisticsXml() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jaxbUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("my/path/document.txt"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats(), nullValue()); // No detailed stats
            assertThat(stats.getStats().size(), greaterThan(0));

            // No word level stats
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(transStat.getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getDocumentStatisticsXmlWithDetails() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            request.setQueryString("word=true&detail=true&locale=en-US&locale=as&locale=es");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jaxbUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("my/path/document.txt"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            //assertThat(stats.getDetailedStats().size(), greaterThan(0)); // No detailed stats (maybe later)
            assertThat(stats.getStats().size(), greaterThan(0));

            for( TranslationStatistics transStat : stats.getStats() )
            {
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }

            // Results returned only for specified locales
            String[] expectedLocales = new String[]{"en-US", "as", "es"};
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(Arrays.asList(expectedLocales), hasItem( transStat.getLocale() ));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getIterationStatisticsJson() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jsonUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("1.0"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats(), nullValue()); // No detailed stats
            assertThat(stats.getStats().size(), greaterThan(0));

            // No word level stats
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(transStat.getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getIterationStatisticsJsonWithDetails() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            request.setQueryString("word=true&detail=true&locale=en-US&locale=as&locale=es");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jsonUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("1.0"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats().size(), greaterThan(0));
            assertThat(stats.getStats().size(), greaterThan(0));

            for( TranslationStatistics transStat : stats.getStats() )
            {
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }

            // Results returned only for specified locales
            String[] expectedLocales = new String[]{"en-US", "as", "es"};
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(Arrays.asList(expectedLocales), hasItem( transStat.getLocale() ));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getDocumentStatisticsJson() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jsonUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("my/path/document.txt"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            assertThat(stats.getDetailedStats(), nullValue()); // No detailed stats
            assertThat(stats.getStats().size(), greaterThan(0));

            // No word level stats
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(transStat.getUnit(), is(TranslationStatistics.StatUnit.MESSAGE));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }

   @Test
   public void getDocumentStatisticsJsonWithDetails() throws Exception
   {
      new ResourceRequestEnvironment.ResourceRequest(unauthorizedEnvironment, ResourceRequestEnvironment.Method.GET,
            "/restv1/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            request.setQueryString("word=true&detail=true&locale=en-US&locale=as&locale=es");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);

            ContainerTranslationStatistics stats = jsonUnmarshal(response, ContainerTranslationStatistics.class);
            assertThat(stats.getId(), is("my/path/document.txt"));
            assertThat(stats.getRefs().size(), greaterThan(0));
            //assertThat(stats.getDetailedStats().size(), greaterThan(0)); // No detailed stats (maybe later)
            assertThat(stats.getStats().size(), greaterThan(0));

            for( TranslationStatistics transStat : stats.getStats() )
            {
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }

            // Results returned only for specified locales
            String[] expectedLocales = new String[]{"en-US", "as", "es"};
            for( TranslationStatistics transStat : stats.getStats() )
            {
               assertThat(Arrays.asList(expectedLocales), hasItem( transStat.getLocale() ));
               // make sure counts are sane
               assertThat(transStat.getNeedReview() + transStat.getTranslated() + transStat.getUntranslated(), equalTo( transStat.getTotal() ));
            }
         }

      }.run();
   }
}
