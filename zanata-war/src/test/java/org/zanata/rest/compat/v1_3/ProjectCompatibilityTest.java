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
package org.zanata.rest.compat.v1_3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.Test;
import org.zanata.ZanataCompatibilityTest;
import org.zanata.v1_3.rest.client.IProjectResource;
import org.zanata.v1_3.rest.dto.Project;
import org.zanata.v1_3.rest.dto.ProjectType;

/**
 * Compatibility Tests For the Project Client Resource.
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 */
@Test(groups = {"compatibility-tests", "seam-tests"} )
public class ProjectCompatibilityTest extends ZanataCompatibilityTest
{
   
   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }   

   @Test
   public void getProjectXml() throws Exception
   {
      IProjectResource projectClient = super.createProxy(IProjectResource.class, "/projects/p/sample-project");
      ClientResponse<Project> projectResponse = projectClient.get();
      Project project = projectResponse.getEntity();
      
      // Assert correct parsing of all properties
      assertThat(project.getId(), is("sample-project"));
      assertThat(project.getName(), is("Sample Project"));
      assertThat(project.getDescription(), is("An example Project"));
      assertThat(project.getIterations().size(), is(2));
      assertThat(project.getType(), is( ProjectType.IterationProject ));
   }
   
   @Test
   public void putProjectXml() throws Exception
   {
      // New Project
      Project p = new Project("new-project", "New Project", ProjectType.IterationProject, "This is a New Sample Project");
      
      IProjectResource projectClient = super.createProxy(IProjectResource.class, "/projects/p/new-project");
      ClientResponse putResponse = projectClient.put( p );
      
      // Assert initial put
      assertThat(putResponse.getStatus(), is(Status.CREATED.getStatusCode()));
      
      // Modified Project
      p.setDescription("This is an updated project");
      putResponse = projectClient.put( p );
      
      // Assert modification
      assertThat(putResponse.getStatus(), is(Status.OK.getStatusCode()));
      
      // Retrieve again
      Project p2 = projectClient.get().getEntity();
      assertThat(p2.getId(), is(p.getId()));
      assertThat(p2.getName(), is(p.getName()));
      assertThat(p2.getDescription(), is(p.getDescription()));
      assertThat(p2.getIterations(), nullValue());
      assertThat(p2.getType(), is(p.getType()));
   }
}
