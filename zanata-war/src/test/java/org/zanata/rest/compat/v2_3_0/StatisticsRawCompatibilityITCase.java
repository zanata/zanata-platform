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
package org.zanata.rest.compat.v2_3_0;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;
import org.zanata.v2_3_0.rest.dto.stats.ContainerTranslationStatistics;

import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class StatisticsRawCompatibilityITCase extends RestTest
{
   @Override
   protected void prepareDBUnitOperations()
   {
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/DocumentsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      addBeforeTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));

      addAfterTestOperation(new DBUnitProvider.DataSetOperation("org/zanata/test/model/HistoryTestData.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }

   @Test
   @RunAsClient
   public void getStatisticsForIterationXml() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/stats/proj/{projectId}/iter/{iterationId}"), "GET")
      {

         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                   .queryParameter("detail", true)
                   .queryParameter("word", true)
                   .pathParameter("projectId", "sample-project")
                   .pathParameter("iterationId", "1.0");;
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);
         }
      }.run();
   }

   @Test
   @RunAsClient
   public void getStatisticsForIterationJson() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/stats/proj/{projectId}/iter/{iterationId}"), "GET")
      {

         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                  .queryParameter("detail", true)
                  .queryParameter("word", true)
                  .pathParameter("projectId", "sample-project")
                  .pathParameter("iterationId", "1.0");
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);
         }
      }.run();
   }

   @Test
   @RunAsClient
   public void getStatisticsForDocumentXml() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/stats/proj/{projectId}/iter/{iterationId}/doc/{docId}"), "GET")
      {

         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
                  .queryParameter("detail", true)
                  .queryParameter("word", true)
                  .pathParameter("projectId", "sample-project")
                  .pathParameter("iterationId", "1.0")
                  .pathParameter("docId", "my/path/document.txt");
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertJaxbUnmarshal(response, ContainerTranslationStatistics.class);
         }
      }.run();
   }

   @Test
   @RunAsClient
   public void getStatisticsForDocumentJson() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/stats/proj/{projectId}/iter/{iterationId}/doc/{docId}"), "GET")
      {

         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON)
                  .queryParameter("detail", true)
                  .queryParameter("word", true)
                  .pathParameter("projectId", "sample-project")
                  .pathParameter("iterationId", "1.0")
                  .pathParameter("docId", "my/path/document.txt");
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertJsonUnmarshal(response, ContainerTranslationStatistics.class);
         }
      }.run();
   }
}
