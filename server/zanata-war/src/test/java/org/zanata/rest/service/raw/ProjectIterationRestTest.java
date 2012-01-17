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

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.rest.MediaTypes;

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
            assertContentSameAsResource(response.getContentAsString(), "rest/iteration/get.xml");
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
            assertContentSameAsResource(response.getContentAsString(), "rest/iteration/get.json");
         }
      }.run();
   }
   
   /*@Test
   public void getWithInvalidContentType() throws Exception
   {
      new ResourceRequest(unauthorizedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, "INVALID-CONTENT-TYPE");
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(500)); // 500 (Should it be a 415. unsupported media type?)
         }
      }.run();
   }*/
   
   @Test
   public void putXml() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/test-iteration")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            request.setContent( getResourceAsString("rest/iteration/put.xml").getBytes() );
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
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/test-iteration-json")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            request.setContent( getResourceAsString("rest/iteration/put.json").getBytes() );
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(201)); // Created
         }
      }.run();
   }

}
