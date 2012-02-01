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
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.annotations.BeforeMethod;

public abstract class ZanataRawRestTest extends ZanataDBUnitSeamTest
{

   protected ResourceRequestEnvironment unauthorizedEnvironment;
   protected ResourceRequestEnvironment sharedEnvironment;

   @BeforeMethod(firstTimeOnly = true)
   public void prepareSharedEnvironment() throws Exception
   {
      sharedEnvironment = new ResourceRequestEnvironment(this)
      {
         @SuppressWarnings("serial")
         @Override
         public Map<String, Object> getDefaultHeaders()
         {
            return new HashMap<String, Object>()
            {
               {
                  put("X-Auth-User", "admin");
                  put("X-Auth-Token", "b6d7044e9ee3b2447c28fb7c50d86d98");
               }
            };
         }
      };
      
      unauthorizedEnvironment = new ResourceRequestEnvironment(this)
      {
         @Override
         public Map<String, Object> getDefaultHeaders()
         {
            return new HashMap<String, Object>();
         }
      };
   }
   
   protected static void assertJaxbUnmarshal( EnhancedMockHttpServletResponse response, Class<?> jaxbType )
   {
      JAXBContext jc;
      try
      {
         jc = JAXBContext.newInstance(jaxbType);
         Unmarshaller um = jc.createUnmarshaller();
         um.unmarshal( new StringReader(response.getContentAsString()) );
      }
      catch (JAXBException e)
      {
         throw new AssertionError(e);
      }
   }
   
   protected static void assertJsonUnmarshal( EnhancedMockHttpServletResponse response, Class<?> jsonType )
   {
      ObjectMapper mapper = new ObjectMapper();
      try
      {
         mapper.readValue( response.getContentAsString(), jsonType);
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
   
   protected static void assertHeaderPresent(HttpServletResponse response, String headerName)
   {
      if( !response.containsHeader(headerName) )
      {
         throw new AssertionError("Expected Http header '" + headerName + "' in Response.");
      }
   }
   
   protected static void assertHeaderValue(EnhancedMockHttpServletResponse response, String headerName, String headerValue)
   {
      assertHeaderPresent(response, headerName);
      
      if( !response.getHeader(headerName).equals( headerValue ) )
      {
         throw new AssertionError("Expected header '" + headerName + "' to be '" + headerValue + "'; but instead got " +
         		"'" + response.getHeader(headerName) + "'");
      }
   }
   
   protected static <T> T jaxbUnmarshal( EnhancedMockHttpServletResponse response, Class<T> jaxbType )
   {
      JAXBContext jc;
      try
      {
         jc = JAXBContext.newInstance(jaxbType);
         Unmarshaller um = jc.createUnmarshaller();
         //um.setEventHandler( new javax.xml.bind.helpers.DefaultValidationEventHandler() );
         T result = (T)um.unmarshal( new StringReader(response.getContentAsString()) );
         return result;
      }
      catch (JAXBException e)
      {
         throw new AssertionError(e);
      }
   }
   
   protected static <T> T jsonUnmarshal( EnhancedMockHttpServletResponse response, Class<T> jsonType )
   {
      ObjectMapper mapper = new ObjectMapper();
      try
      {
         return mapper.readValue( response.getContentAsString(), jsonType);
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
