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
package net.openl10n.flies.util;

import java.net.URLDecoder;
import java.net.URLEncoder;

import net.openl10n.flies.exception.FliesServiceException;

import org.apache.commons.codec.binary.Base64;

public class Base64UrlSafe
{
   public static String encode(String var) throws FliesServiceException
   {
      try
      {
         Base64 en = new Base64();
         String enVar = new String(en.encode(var.getBytes()), "UTF-8");
         String result = URLEncoder.encode(enVar, "UTF-8");
         return result;
      }
      catch (Exception e)
      {
         throw new FliesServiceException(e.getMessage());
      }
   }

   public static String decode(String var) throws FliesServiceException
   {
      try
      {
         String deVar = URLDecoder.decode(var, "UTF-8");
         Base64 en = new Base64();
         String result = new String(en.decode(deVar.getBytes()), "UTF-8");
         return result;
      }
      catch (Exception e)
      {
         throw new FliesServiceException(e.getMessage());
      }

   }

}
