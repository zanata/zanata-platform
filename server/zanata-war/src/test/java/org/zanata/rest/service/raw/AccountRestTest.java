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
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;
import org.testng.annotations.Test;
import org.zanata.ZanataRawRestTest;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Account;

public class AccountRestTest extends ZanataRawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @Test
   public void xmlGetUnavailable() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/accounts/u/NOT_AVAILABLE")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   public void xmlGet() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/accounts/u/admin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200));
            assertJaxbUnmarshal(response, Account.class);
            
            Account account = jaxbUnmarshal(response, Account.class);
            assertThat(account.getUsername(), is("admin"));
            assertThat(account.getPasswordHash(), is("Eyox7xbNQ09MkIfRyH+rjg=="));
            assertThat(account.getEmail(), is("root@localhost"));
            assertThat(account.getApiKey(), is("b6d7044e9ee3b2447c28fb7c50d86d98"));
            assertThat(account.getRoles().size(), is(1)); // 1 roles
         }
      }.run();
   }
   
   @Test
   public void jsonGet() throws Exception
   {
      new ResourceRequest(sharedEnvironment, Method.GET, "/restv1/accounts/u/admin")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.addHeader(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(200));
            assertJsonUnmarshal(response, Account.class);
            
            Account account = jsonUnmarshal(response, Account.class);
            assertThat(account.getUsername(), is("admin"));
            assertThat(account.getPasswordHash(), is("Eyox7xbNQ09MkIfRyH+rjg=="));
            assertThat(account.getEmail(), is("root@localhost"));
            assertThat(account.getApiKey(), is("b6d7044e9ee3b2447c28fb7c50d86d98"));
            assertThat(account.getRoles().size(), is(1)); // 1 role
         }
      }.run();
   }
   
   @Test
   public void xmlPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/accounts/u/testuser")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            request.setContent( jaxbMarhsal(account).getBytes() );
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   public void jsonPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/accounts/u/testuser")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            request.setContent(jsonMarshal(account).getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   public void unauthorizedPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(unauthorizedEnvironment, Method.PUT, "/restv1/accounts/u/testuser")
      {
         @Override
         protected void prepareRequest(EnhancedMockHttpServletRequest request)
         {
            request.setContentType(MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            request.setContent(jsonMarshal(account).getBytes());
         }

         @Override
         protected void onResponse(EnhancedMockHttpServletResponse response)
         {
            assertThat(response.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
         }
      }.run();
   }

}
