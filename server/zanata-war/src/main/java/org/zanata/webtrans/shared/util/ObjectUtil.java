package org.zanata.webtrans.shared.util;

public class ObjectUtil
{
   public static boolean equals(Object object1, Object object2)
   {
      return (object1 == null ? object2 == null : object1.equals(object2));
   }
}
