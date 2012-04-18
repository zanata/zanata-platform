package org.zanata.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class StringUtil
{
   private StringUtil()
   {
   }

   public static String concat(Iterable<String> strings, char delim)
   {
      StringBuilder sb = new StringBuilder();
      for (Iterator<String> it = strings.iterator(); it.hasNext();)
      {
         sb.append(it.next());
         if (it.hasNext())
            sb.append(delim);
      }
      return sb.toString();
   }

   public static List<String> split(String s, String delim)
   {
      return Arrays.asList(s.split(","));
   }

   public static boolean allNonEmpty(Collection<String> strings)
   {
      for (String s : strings)
      {
         if (s == null || s.isEmpty())
         {
            return false;
         }
      }
      return true;
   }

   public static boolean allEmpty(Collection<String> strings)
   {
      for (String s : strings)
      {
         if (s != null && !s.isEmpty())
         {
            return false;
         }
      }
      return true;
   }

}
