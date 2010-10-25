package net.openl10n.flies.rest;

public class RestUtil
{

   public static String convertToDocumentURIId(String id)
   {
      // NB this currently prevents us from allowing ',' in file names
      if (id.startsWith("/"))
      {
         return id.substring(1).replace('/', ',');
      }
      return id.replace('/', ',');
   }

}
