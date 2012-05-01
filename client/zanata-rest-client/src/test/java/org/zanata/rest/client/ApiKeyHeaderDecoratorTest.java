/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.client;

import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.testng.annotations.Test;
import org.zanata.rest.RestConstant;

import static org.testng.AssertJUnit.*;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
@Test(groups = { "unit-tests" })
public class ApiKeyHeaderDecoratorTest
{
   @Test
   public void testHeaders() throws Exception
   {
      String username = "username";
      String apiKey = "apiKey";
      String ver = "ver";
      ApiKeyHeaderDecorator decorator = new ApiKeyHeaderDecorator(username, apiKey, ver);

      final ClientRequest mockRequest = new ClientRequest("http://uri.example.com/");
      ClientExecutionContext mockCtx = new ClientExecutionContext()
      {

         @SuppressWarnings("rawtypes")
         @Override
         public ClientResponse proceed() throws Exception
         {
            return null;
         }

         @Override
         public ClientRequest getRequest()
         {
            return mockRequest;
         }
      };
      decorator.execute(mockCtx);
      MultivaluedMap<String, String> headers = mockRequest.getHeaders();
      assertEquals(username, headers.getFirst(RestConstant.HEADER_USERNAME));
      assertEquals(apiKey, headers.getFirst(RestConstant.HEADER_API_KEY));
      assertEquals(ver, headers.getFirst(RestConstant.HEADER_VERSION_NO));
   }
}
