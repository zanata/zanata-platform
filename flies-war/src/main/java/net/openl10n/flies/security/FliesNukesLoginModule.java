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
package net.openl10n.flies.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.security.auth.login.LoginException;

import org.jboss.security.auth.spi.DatabaseServerLoginModule;

public class FliesNukesLoginModule extends DatabaseServerLoginModule
{
   // private static final LogProvider log =
   // Logging.getLogProvider(FliesNukesLoginModule.class);
   public static final String AUTHENTICATED_NUKE_USER = "authenticated.nuke.user";
   public boolean validatePassword(String inputPassword, String expectedPassword)
   {
      inputPassword = toHexString(md5(inputPassword));
      boolean result = super.validatePassword(inputPassword, expectedPassword);
      return result;
   }

    public boolean login() throws LoginException
    {
    boolean success = super.login();
    return success;
   
    }


   private String toHexString(byte[] bytes)
   {
      if (bytes == null)
      {
         throw new IllegalArgumentException("byte array must not be null");
      }
      StringBuffer hex = new StringBuffer(bytes.length * 2);
      for (byte aByte : bytes)
      {
         hex.append(Character.forDigit((aByte & 0XF0) >> 4, 16));
         hex.append(Character.forDigit((aByte & 0X0F), 16));
      }
      return hex.toString();
   }


   private byte[] md5(String text)
   {
      // arguments check
      if (text == null)
      {
         throw new NullPointerException("null text");
      }

      try
      {
         MessageDigest md = MessageDigest.getInstance("MD5");
         md.update(text.getBytes());
         return md.digest();
      }
      catch (NoSuchAlgorithmException e)
      {
         throw new RuntimeException("Cannot find MD5 algorithm");
      }
   }

}
