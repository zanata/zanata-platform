package net.openl10n.flies.rest.client;

import javax.ws.rs.ext.Provider;
import net.openl10n.flies.rest.RestConstant;


import org.jboss.resteasy.annotations.interception.ClientInterceptor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.spi.interception.ClientExecutionContext;
import org.jboss.resteasy.spi.interception.ClientExecutionInterceptor;

@Provider
@ClientInterceptor
public class ApiKeyHeaderDecorator implements ClientExecutionInterceptor
{
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
      return ctx.proceed();
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