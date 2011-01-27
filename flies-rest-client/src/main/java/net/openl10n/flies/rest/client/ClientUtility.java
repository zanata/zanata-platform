package net.openl10n.flies.rest.client;

import java.net.URI;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;

public class ClientUtility
{
   public static void checkResult(ClientResponse<?> response, URI uri)
   {
      if (response.getStatus() >= 399)
      {
         String annotString = "";
         String uriString = "";
         String entity = "";
         if (response instanceof BaseClientResponse)
         {
            BaseClientResponse<?> resp = (BaseClientResponse<?>) response;
            annotString = ", annotations: " + Arrays.asList(resp.getAnnotations()).toString();
         }
         // TODO if this works, remove uri parameter
         if (response.getLocation() != null)
         {
            uriString = ", location: " + response.getLocation();
         }
         if (uri != null)
         {
            uriString = ", uri: " + uri;
         }
         try
         {
            entity = ": " + response.getEntity(String.class);
         }
         finally
         {
            // ignore
         }
         String msg = "operation returned " + response.getStatus() + " (" + Response.Status.fromStatusCode(response.getStatus()) + ")" + entity + uriString + annotString;
         throw new RuntimeException(msg);
      }
   }
}
