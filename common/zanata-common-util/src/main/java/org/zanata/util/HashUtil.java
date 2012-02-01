package org.zanata.util;

import java.security.MessageDigest;

import org.apache.commons.codec.binary.Hex;

public class HashUtil
{

   public static String generateHash(String key)
   {
      try
      {
         MessageDigest md5 = MessageDigest.getInstance("MD5");
         md5.reset();
         return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
      }
      catch (Exception exc)
      {
         throw new RuntimeException(exc);
      }
   }

}
