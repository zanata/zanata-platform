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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RawRestTest;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class AccountRawRestITCase extends RawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
   }
   
   @Test
   @RunAsClient
   public void xmlGetUnavailable() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/NOT_AVAILABLE"), "GET")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(Status.NOT_FOUND.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   @RunAsClient
   public void xmlGet() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("accounts/u/admin"), "GET")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
         }

         @Override
         protected void onResponse(ClientResponse response)
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
   @RunAsClient
   public void jsonGet() throws Exception
   {
      new ResourceRequest(getRestEndpointUrl("/accounts/u/admin"), "GET")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
         }

         @Override
         protected void onResponse(ClientResponse response)
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
   @RunAsClient
   public void xmlPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT", getAuthorizedEnvironment())
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.body(MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML, jaxbMarhsal(account).getBytes());
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   @RunAsClient
   public void jsonPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT", getAuthorizedEnvironment())
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.body(MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON, jsonMarshal(account).getBytes());
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(Status.CREATED.getStatusCode()));
         }
      }.run();
   }
   
   @Test
   @RunAsClient
   public void unauthorizedPut() throws Exception
   {
      final Account account = new Account("test@testing.com", "Test Account", "testuser", "Eyox7xbNQ09MkIfRyH+rjg==");
      account.setEnabled(false);
      
      new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.body(MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON, jsonMarshal(account).getBytes());
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
         }
      }.run();
   }

}
