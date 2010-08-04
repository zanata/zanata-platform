package org.fedorahosted.flies.rest.client;

import java.net.URL;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.core.BaseClientResponse;

public class ClientUtility
{
   public static void checkResult(Response response, URL url)
   {
      if (response.getStatus() >= 399)
      {
         String annotString = "";
         String urlString = "";
         if (response instanceof BaseClientResponse)
         {
            BaseClientResponse<?> resp = (BaseClientResponse<?>) response;
            annotString = ", annotations: " + Arrays.asList(resp.getAnnotations()).toString();
         }
         if (url != null)
         {
            urlString = ", url: " + url;
         }
         String msg = "operation returned " + response.getStatus() + ": " + Response.Status.fromStatusCode(response.getStatus()) + urlString + annotString;
         throw new RuntimeException(msg);
      }
   }
}
