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

import java.io.IOException;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.rest.client.TestProxyFactory;
import org.zanata.rest.service.SeamMockClientExecutor;

@Test(groups="compatibility-tests")
public abstract class ZanataCompatibilityTest extends ZanataRawRestTest
{
   
   private TestProxyFactory clientRequestFactory;
   
   @BeforeMethod
   public void setup() throws Exception
   {
      this.clientRequestFactory = new TestProxyFactory(new SeamMockClientExecutor(this));
   }
   
   protected <T> T createProxy( Class<T> clientClass, String baseUri )
   {
      try
      {
         return this.clientRequestFactory.createProxy(clientClass, new URI("/restv1" + baseUri));
      }
      catch (URISyntaxException e)
      {
         throw new RuntimeException(e);
      }
   }
   
   protected static String jaxbMarhsal( Object jaxbObject )
   {
      JAXBContext jc;
      try
      {
         jc = JAXBContext.newInstance(jaxbObject.getClass());
         Marshaller m = jc.createMarshaller();
         //m.setEventHandler( new javax.xml.bind.helpers.DefaultValidationEventHandler() );
         StringWriter sw = new StringWriter();
         m.marshal(jaxbObject, sw);
         return sw.toString();
      }
      catch (JAXBException e)
      {
         throw new AssertionError(e);
      }
   }
   
   protected static String jsonMarshal( Object jsonObject )
   {
      ObjectMapper mapper = new ObjectMapper();
      try
      {
         return mapper.writeValueAsString( jsonObject );
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
