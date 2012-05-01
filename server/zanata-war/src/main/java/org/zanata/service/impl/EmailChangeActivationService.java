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
package org.zanata.service.impl;

import java.util.Random;

import org.zanata.util.Base64UrlSafe;


public class EmailChangeActivationService
{
   public static class KeyParameter
   {
      private String id;
      private String email;

      public KeyParameter(String id, String email)
      {
         this.id = id;
         this.email = email;
      }

      public String getId()
      {
         return id;
      }

      public String getEmail()
      {
         return email;
      }
   }

   public static String generateActivationKey(String id, String email)
   {
      Random ran = new Random();
      String var = id + ";" + ran.nextInt() + ";" + email;
      return Base64UrlSafe.encode(var);
   }

   public static KeyParameter parseKey(String key)
   {
      String var = Base64UrlSafe.decode(key);
      String[] array = var.split(";");
      String id = array[0];
      String email = array[2];
      return new KeyParameter(id, email);
   }

}
