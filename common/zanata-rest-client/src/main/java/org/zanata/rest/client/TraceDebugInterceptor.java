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
package org.zanata.rest.client;

import java.io.IOException;
import java.util.Scanner;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.jboss.resteasy.spi.interception.MessageBodyReaderContext;
import org.jboss.resteasy.spi.interception.MessageBodyReaderInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs logging of Resteasy Requests on the client side. This interceptor logs only
 * at the Trace debug level. 
 * 
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 */
@Provider
@ClientInterceptor
public class TraceDebugInterceptor implements MessageBodyReaderInterceptor, ClientExecutionInterceptor
{

   private static final Logger log = LoggerFactory.getLogger(TraceDebugInterceptor.class);
   
   private boolean isVerbose;
   
   public TraceDebugInterceptor(boolean isVerbose)
   {
      this.isVerbose = isVerbose;
   }
   
   @Override
   public Object read(MessageBodyReaderContext context) throws IOException, WebApplicationException
   {
      if( log.isTraceEnabled() && this.isVerbose )
      {
         // Log after reading a response
         for( String key : context.getHeaders().keySet() )
         {
            log.trace("Header:   " + key.toString());
            log.trace("Value(s): " + context.getHeaders().get(key).toString());
         }
         // mark the input stream so it can be reset later (the input stream can only be read once)
         context.getInputStream().mark(Integer.MAX_VALUE);
         log.trace("Body:" + new Scanner(context.getInputStream()).useDelimiter("\\A").next());
         // reset the input stream so the entity can be read by the client
         context.getInputStream().reset();
      }
      
      return context.proceed();
   }
   
   
   @SuppressWarnings("rawtypes")
   @Override
   public ClientResponse execute(ClientExecutionContext ctx) throws Exception
   {
      if( log.isTraceEnabled() )
      {
         log.trace( "Zanata Rest Request: " + ctx.getRequest().getHttpMethod() + " => " + ctx.getRequest().getUri() );
         
         if( this.isVerbose )
         {
            // Log before sending a request
            for( String key : ctx.getRequest().getHeaders().keySet() )
            {
               log.trace("Header:   " + key.toString());
               log.trace("Value(s): " + ctx.getRequest().getHeaders().get(key).toString());
            }
            log.trace("Body:" + ctx.getRequest().getBody() );
         }
      }
      
      return ctx.proceed();
   }

}
