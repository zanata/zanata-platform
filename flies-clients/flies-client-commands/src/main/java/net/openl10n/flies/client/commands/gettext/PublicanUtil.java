package net.openl10n.flies.client.commands.gettext;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * 
 * @author Sean Flanigan &lt;<a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>&gt;
 * 
 */
public class PublicanUtil
{
   private PublicanUtil()
   {
   }

   public static File[] findPotFiles(File potDir) throws IOException
   {
      // scan the directory for pot files
      File[] potFiles = potDir.listFiles(new FileFilter()
      {
         @Override
         public boolean accept(File pathname)
         {
            return pathname.isFile() && pathname.getName().endsWith(".pot");
         }
      });
   
      if (potFiles == null)
      {
         throw new IOException("Unable to read directory \"pot\" - have you run \"publican update_pot\"?");
      }
      return potFiles;
   }

   public static File[] findLocaleDirs(File srcDir)
   {
      File[] localeDirs;
      localeDirs = srcDir.listFiles(new FileFilter()
      {
   
         @Override
         public boolean accept(File f)
         {
            return f.isDirectory() && !f.getName().equals("pot");
         }
      });
      return localeDirs;
   }

}
