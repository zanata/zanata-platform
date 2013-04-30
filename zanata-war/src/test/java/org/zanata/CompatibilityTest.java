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
package org.zanata;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.Before;
import org.zanata.rest.client.TestProxyFactory;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.VersionInfo;

public abstract class CompatibilityTest extends RawRestTest
{

   private ZanataProxyFactory clientRequestFactory;

   @Before
   public void setup() throws Exception
   {
      // Use the default Apache Http 4 client executor and other default values
      this.clientRequestFactory = new TestProxyFactory(new URI(getRestEndpointUrl()),
                                                       ADMIN,
                                                       ADMIN_KEY,
                                                       null,
                                                       new VersionInfo("Test", "Test"))
      {
         // NB This should be a bit cleaner
         @Override
         protected URL getBaseUrl()
         {
            try
            {
               return new URL(getRestEndpointUrl());
            }
            catch (MalformedURLException e)
            {
               throw new RuntimeException(e);
            }
         }
      };
   }

   protected <T> T createProxy( Class<T> clientClass, String baseUri )
   {
      try
      {
         return this.clientRequestFactory.createProxy(clientClass, new URI( getRestEndpointUrl(baseUri) ));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }

}