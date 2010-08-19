package net.openl10n.flies.security;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

/**
 * This bean is used to redirect a user after login to a given url. Simply pass
 * the ?continue=URL to the login method to redirect after login
 */
@Name("userRedirect")
@Scope(ScopeType.PAGE)
@AutoCreate
public class UserRedirectBean
{

   private final static String ENCODING = "UTF-8";
   private String url;

   public void setUrl(String url)
   {
      this.url = url;
   }

   public String getUrl()
   {
      return url;
   }

   public String getEncodedUrl()
   {
      if (url == null)
         return null;
      try
      {
         return URLEncoder.encode(url, ENCODING);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public void setEncodedUrl(String encodedUrl)
   {
      if (encodedUrl == null || encodedUrl.isEmpty())
      {
         this.url = encodedUrl;
         return;
      }

      try
      {
         this.url = URLDecoder.decode(encodedUrl, ENCODING);
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public boolean isRedirect()
   {
      return url != null && !url.isEmpty();
   }
}
