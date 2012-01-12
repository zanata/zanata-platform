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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.testng.Assert;
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
   
   protected static String getResourceAsString(String resource)
   {
      // Read the body file into memory
      InputStream st = ZanataRawRestTest.class.getClassLoader().getResourceAsStream( resource );
      BufferedReader reader = new BufferedReader( new InputStreamReader(st) );
      StringBuilder body = new StringBuilder();
      String line;
      try
      {
         while( (line = reader.readLine()) != null )
         {
            body.append(line);
         }
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
      
      return body.toString();
   }
   
   protected static void assertContentSameAsResource(String content, String resource)
   {
      String resourceXml = getResourceAsString(resource);
      
      // Turn both to single lines and compare
      try
      {
         Assert.assertEquals( toSingleLine(content) , toSingleLine(resourceXml));
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
   
   private static String toSingleLine( String multiLineString ) throws IOException
   {
      BufferedReader br = new BufferedReader(new StringReader(multiLineString));
      String line;
      StringBuilder multiLine = new StringBuilder();

      while((line=br.readLine())!= null){
         multiLine.append(line.trim().replaceAll("\n", "").replaceAll("\t", ""));
      }
      
      return multiLine.toString();
   }
   
}
