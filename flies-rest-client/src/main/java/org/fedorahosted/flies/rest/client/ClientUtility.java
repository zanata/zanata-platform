package org.fedorahosted.flies.rest.client;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.core.BaseClientResponse;

public class ClientUtility
{
   public static void checkResult(Response response, URI uri)
   {
      if (response.getStatus() >= 399)
      {
         String annotString = "";
         String uriString = "";
         if (response instanceof BaseClientResponse)
         {
            BaseClientResponse<?> resp = (BaseClientResponse<?>) response;
            annotString = ", annotations: " + Arrays.asList(resp.getAnnotations()).toString();
         }
         if (uri != null)
         {
            uriString = ", uri: " + uri;
         }
         String msg = "operation returned " + response.getStatus() + ": " + Response.Status.fromStatusCode(response.getStatus()) + uriString + annotString;
         throw new RuntimeException(msg);
      }
   }
}
