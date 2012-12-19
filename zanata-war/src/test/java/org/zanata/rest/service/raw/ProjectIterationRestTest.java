/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.service.raw;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.common.EntityStatus;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.ProjectIteration;

@Test(groups = {"seam-tests"})
public class ProjectIterationRestTest extends ZanataRawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @Test
   public void head() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.HEAD, "/restv1/projects/p/sample-project/iterations/i/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertHeaderPresent(response, HttpHeaders.ETAG);
         }
      }.run();
   }
   
   @Test 
   public void getXml() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, ProjectIteration.class);
            
            ProjectIteration iteration = jaxbUnmarshal(response, ProjectIteration.class);
            assertThat(iteration.getId(), is("1.0"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
         }
      }.run();
   }
   
   @Test 
   public void getJson() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, ProjectIteration.class);
            
            ProjectIteration iteration = jsonUnmarshal(response, ProjectIteration.class);
            assertThat(iteration.getId(), is("1.0"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
         }
      }.run();
   }
   
   @Test
   public void getCurrentIterationOnObsoleteProject() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/obsolete-project/iterations/i/obsolete-current")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode())); // Iteration not found because project is obsolete
         }
      }.run();
   }
   
   @Test
   public void getCurrentIterationOnRetiredProject() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/retired-project/iterations/i/retired-current")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Response.Status.OK.getStatusCode())); // 200 (Retired projects are readable)
         }
      }.run();
   }
   
   @Test
   public void getObsoleteIterationOnCurrentProject() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/current-project/iterations/i/current-obsolete")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Response.Status.NOT_FOUND.getStatusCode())); // 404
         }
      }.run();
   }
   
   @Test
   public void putXml() throws Exception
   {
      final ProjectIteration iteration = new ProjectIteration("test-iteration");
      iteration.setStatus(EntityStatus.ACTIVE);
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/test-iteration")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            request.setContent( jaxbMarhsal(iteration).getBytes() );
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(201)); // Created
         }
      }.run();
   }
   
   @Test
   public void putJson() throws Exception
   {
      final ProjectIteration iteration = new ProjectIteration("test-iteration");
      iteration.setStatus(EntityStatus.ACTIVE);
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/test-iteration-json")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            request.setContent( jsonMarshal(iteration).getBytes() );
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(201)); // Created
         }
      }.run();
   }

}
