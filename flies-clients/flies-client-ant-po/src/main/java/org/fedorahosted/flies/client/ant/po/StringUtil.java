package org.fedorahosted.flies.client.ant.po;

class StringUtil
{
   static String removeFileExtension(String filename, String extension)
   {
      if (!filename.endsWith(extension))
         throw new IllegalArgumentException("Filename '" + filename + "' should have extension '" + extension + "'");
      String basename = filename.substring(0, filename.length() - extension.length());
      return basename;
   }

}
