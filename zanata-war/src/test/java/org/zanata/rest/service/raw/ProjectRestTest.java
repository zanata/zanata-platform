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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import javax.ws.rs.core.HttpHeaders;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.common.EntityStatus;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.ProjectType;

@Test(groups = {"seam-tests"})
public class ProjectRestTest extends ZanataRawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void head() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.HEAD, "/restv1/projects/p/sample-project")
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
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, Project.class);
            
            Project project = jaxbUnmarshal(response, Project.class);
            assertThat(project.getId(), is("sample-project"));
            assertThat(project.getDescription(), is("An example Project"));
            assertThat(project.getStatus(), is(EntityStatus.ACTIVE));
            assertThat(project.getName(), is("Sample Project"));
            assertThat(project.getType(), is(ProjectType.IterationProject));
            assertThat(project.getIterations().size(), is(2));
            
            // Iteration 1
            ProjectIteration iteration = project.getIterations().get(0);
            assertThat(iteration.getId(), is("1.0"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            
            // Iteration 2
            iteration = project.getIterations().get(1);
            assertThat(iteration.getId(), is("1.1"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
         }
      }.run();
   }
   
   @Test 
   public void getJson() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECT_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, Project.class);
            
            Project project = jsonUnmarshal(response, Project.class);
            assertThat(project.getId(), is("sample-project"));
            assertThat(project.getDescription(), is("An example Project"));
            assertThat(project.getStatus(), is(EntityStatus.ACTIVE));
            assertThat(project.getName(), is("Sample Project"));
            assertThat(project.getType(), is(ProjectType.IterationProject));
            assertThat(project.getIterations().size(), is(2));
            
            // Iteration 1
            ProjectIteration iteration = project.getIterations().get(0);
            assertThat(iteration.getId(), is("1.0"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            
            // Iteration 2
            iteration = project.getIterations().get(1);
            assertThat(iteration.getId(), is("1.1"));
            assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
         }
      }.run();
   }
   
   @Test
   public void putXml() throws Exception
   {
      final Project project = new Project("test-project", "Test Project", ProjectType.IterationProject, "This is a Test project");
      project.setStatus(EntityStatus.ACTIVE);
      project.getIterations(true).add( new ProjectIteration("test-1.0") );
      project.getIterations(true).add( new ProjectIteration("test-2.0") );
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/test-project")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
            request.setContent( jaxbMarhsal(project).getBytes() );
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
      final Project project = new Project("test-project", "Test Project", ProjectType.IterationProject, "This is a Test project");
      project.setStatus(EntityStatus.ACTIVE);
      project.getIterations(true).add( new ProjectIteration("test-1.0") );
      project.getIterations(true).add( new ProjectIteration("test-2.0") );
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/test-project-json")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_JSON);
            request.setContent( jsonMarshal(project).getBytes() );
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(201)); // Created
         }
      }.run();
   }
   
   @Test 
   public void getAllXml() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertThat(response.getContentAsString(), containsString("sample-project"));
            assertThat(response.getContentAsString(), containsString("retired-project"));
            assertThat(response.getContentAsString(), not(containsString("obsolete-project")));
         }
      }.run();
   }
   
   @Test 
   public void getAllJson() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertThat(response.getContentAsString(), containsString("sample-project"));
            assertThat(response.getContentAsString(), containsString("retired-project"));
            assertThat(response.getContentAsString(), not(containsString("obsolete-project")));
         }
      }.run();
   }
   
   
}
