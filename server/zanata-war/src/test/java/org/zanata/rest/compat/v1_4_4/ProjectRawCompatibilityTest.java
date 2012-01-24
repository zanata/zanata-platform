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
package org.zanata.rest.compat.v1_4_4;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataCompatibilityTest;
import org.zanata.rest.MediaTypes;
import org.zanata.v1_4_4.rest.dto.Project;
import org.zanata.v1_4_4.rest.dto.ProjectType;

/**
 * Compatibility tests for the Project REST resource endpoints not exposed over the Resteasy client.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 */
public class ProjectRawCompatibilityTest extends ZanataCompatibilityTest
{
   
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }

   @Test
   public void getProjectJson() throws Exception
   {
      // No client method for Json Get, so testing raw compatibility
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
            
            // Assert correct parsing of all properties
            assertThat(project.getId(), is("sample-project"));
            assertThat(project.getName(), is("Sample Project"));
            assertThat(project.getDescription(), is("An example Project"));
            assertThat(project.getIterations().size(), is(2));
            assertThat(project.getType(), is( ProjectType.IterationProject ));
         }
      }.run();
   }
   
   
   public void getAllProjectsJson() throws Exception
   {
      // No client method for Json Get, so testing raw compatibility
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/")
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
            List<Project> projects = jsonParse(response);
            Project sampleProject = null;
            
            // find sample project
            for( Project p : projects )
            {
               if( p.getId().equals("sample-project") )
               {
                  sampleProject = p;
               }
            }
            
            // Assertions on individual project
            assertThat(sampleProject, notNullValue());
            assertThat(sampleProject.getId(), is("sample-project"));
            assertThat(sampleProject.getName(), is("Sample Project"));
            assertThat(sampleProject.getLinks().size(), is(1));
            assertThat(sampleProject.getType(), is( ProjectType.IterationProject ));
         }
      }.run();
   }
   
   private List<Project> jsonParse(EnhancedMockHttpServletResponse response)
   {
      ObjectMapper mapper = new ObjectMapper();
      try
      {
         return mapper.readValue( response.getContentAsString(), new TypeReference<List<Project>>(){});
      }
      catch (JsonParseException e)
      {
         throw new AssertionError(e);
      }
      catch (JsonMappingException e)
      {
         throw new AssertionError(e);
      }
      catch (IllegalStateException e)
      {
         throw new AssertionError(e);
      }
      catch (IOException e)
      {
         throw new AssertionError(e);
      }
   }
   
}
