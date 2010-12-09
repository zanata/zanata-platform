/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package net.openl10n.flies.security;

import java.io.Serializable;
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
public class UserRedirectBean implements Serializable
{

   /**
    * 
    */
   private static final long serialVersionUID = 1L;
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
