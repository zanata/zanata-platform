package net.openl10n.flies.client.commands;

/**
 * 
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * 
 */
public class StringUtil
{
   public static String removeFileExtension(String filename, String extension)
   {
      if (!filename.endsWith(extension))
         throw new IllegalArgumentException("Filename '" + filename + "' should have extension '" + extension + "'");
      String basename = filename.substring(0, filename.length() - extension.length());
      return basename;
   }

}
