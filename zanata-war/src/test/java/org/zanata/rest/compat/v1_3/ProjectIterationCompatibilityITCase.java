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

import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.v1_3.rest.client.IProjectIterationResource;
import org.zanata.v1_3.rest.dto.ProjectIteration;

public class ProjectIterationCompatibilityITCase extends RestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @Test
   @RunAsClient
   public void getXmlProjectIteration() throws Exception
   {
      IProjectIterationResource iterationClient = super.createProxy(createClientProxyFactory(TRANSLATOR, TRANSLATOR_KEY),
            IProjectIterationResource.class, "/projects/p/sample-project/iterations/i/1.0");
      ClientResponse<ProjectIteration> response = iterationClient.get();
      
      assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
      
      ProjectIteration it = response.getEntity();
      assertThat(it.getId(), is("1.0"));
   }
   
   @Test
   @RunAsClient
   public void putXmlProjectIteration() throws Exception
   {
      ProjectIteration newIteration = new ProjectIteration("new-iteration");
      
      IProjectIterationResource iterationClient = super.createProxy(createClientProxyFactory(ADMIN, ADMIN_KEY),
            IProjectIterationResource.class, "/projects/p/sample-project/iterations/i/" + newIteration.getId());
      ClientResponse response = iterationClient.put(newIteration);

      assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
      response.releaseConnection();
      
      // Retreive it again
      ClientResponse<ProjectIteration> getResponse = iterationClient.get();
      
      assertThat(getResponse.getStatus(), is(Status.OK.getStatusCode())); // 200
      
      ProjectIteration it = getResponse.getEntity();
      assertThat(it.getId(), is("new-iteration"));
   }

}
