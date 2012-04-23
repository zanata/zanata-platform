package org.zanata.rest.client;

import javax.ws.rs.ext.Provider;

import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.rest.RestConstant;

@Provider
@ClientInterceptor
public class ApiKeyHeaderDecorator implements ClientExecutionInterceptor
{
   private static Logger log = LoggerFactory.getLogger(ApiKeyHeaderDecorator.class);
   private String apiKey;
   private String username;
   private String ver;

   public ApiKeyHeaderDecorator()
   {
   }

   public ApiKeyHeaderDecorator(String username, String apiKey, String ver)
   {
      this.username = username;
      this.apiKey = apiKey;
      this.ver = ver;
   }

   @SuppressWarnings("rawtypes")
   @Override
   public ClientResponse execute(ClientExecutionContext ctx) throws Exception
   {
      ctx.getRequest().getHeaders().add(RestConstant.HEADER_USERNAME, username);
      ctx.getRequest().getHeaders().add(RestConstant.HEADER_API_KEY, apiKey);
      ctx.getRequest().getHeaders().add(RestConstant.HEADER_VERSION_NO, ver);
      try
      {
         return ctx.proceed();
      }
      catch (Error e)
      {
         // NB Seam/RestEasy doesn't log these exceptions fully for some reason 
         log.warn("error processing request", e);
         throw e;
      }
      catch (Exception e)
      {
         // NB Seam/RestEasy doesn't log these exceptions fully for some reason 
         log.warn("exception processing request", e);
         throw e;
      }
   }

   public String getApiKey()
   {
      return apiKey;
   }

   public void setApiKey(String apiKey)
   {
      this.apiKey = apiKey;
   }

   public String getUsername()
   {
      return username;
   }

   public void setUsername(String username)
   {
      this.username = username;
   }
}