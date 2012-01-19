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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.resource.Resource;

public class TranslationsRestTest extends ZanataRawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/DocumentsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @Test
   public void xmlPutResource() throws Exception
   {
      // Put the Translations
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/projects/p/sample-project/iterations/i/1.0/r/zanata,test,rest,resource,document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setQueryString(""); // Bug: will throw NPE when adding query params unless this is called 
            request.addQueryParameter("ext", PoHeader.ID);
            request.setContentType(MediaType.APPLICATION_XML);
            request.setContent(getResourceAsString("rest/translations/putResource-1.4.xml").getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode())); // 201
         }
      }.run();
      
      // Try to read them now
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0/r/zanata,test,rest,resource,document.txt")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setQueryString(""); // Bug: will throw NPE when adding query params unless this is called 
            request.addQueryParameter("ext", PoHeader.ID);
            request.setContentType(MediaType.APPLICATION_XML);
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
            assertContentSameAsResource(response.getContentAsString(), "rest/translations/putResource-1.4-expected.xml");
            assertJaxbUnmarshal(response, Resource.class);
         }
      }.run();

   }
   
   @Test
   public void xmlGetTranslations() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/projects/p/sample-project/iterations/i/1.0/r/my,path,document-2.txt/translations/en-US")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setQueryString(""); // Bug: will throw NPE when adding query params unless this is called 
            request.addQueryParameter("ext", SimpleComment.ID);
            request.addHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
         }
         
         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
            assertContentSameAsResource(response.getContentAsString(), "rest/translations/getTranslations.xml");
         }
      }.run();
   }

}
