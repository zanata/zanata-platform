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
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;

import javax.ws.rs.core.HttpHeaders;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RawRestTest;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.VersionInfo;

public class VersionRestTest extends RawRestTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @Test
   @RunAsClient
   public void getJson() throws Exception
   {
      new ResourceRequest(getDeployedUrl("/version"), "GET")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_VERSION_JSON);
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJsonUnmarshal(response, VersionInfo.class);
         }
      }.run();
   }
   
   @Test
   @RunAsClient
   public void getXml() throws Exception
   {
      new ResourceRequest(getDeployedUrl("/version"), "GET")
      {
         @Override
         protected void prepareRequest(ClientRequest request)
         {
            request.header(HttpHeaders.ACCEPT, MediaTypes.APPLICATION_ZANATA_VERSION_XML);
         }

         @Override
         protected void onResponse(ClientResponse response)
         {
            assertThat(response.getStatus(), is(200)); // Ok
            assertJaxbUnmarshal(response, VersionInfo.class);
         }
      }.run();
   }

}
