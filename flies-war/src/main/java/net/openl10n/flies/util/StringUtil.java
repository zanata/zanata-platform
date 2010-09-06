package net.openl10n.flies.util;

import java.util.Iterator;

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

}
