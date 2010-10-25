package net.openl10n.flies.client.commands.gettext;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.openl10n.flies.client.config.LocaleList;
import net.openl10n.flies.client.config.LocaleMapping;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Sean Flanigan &lt;<a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>&gt;
 * 
 */
public class PublicanUtil
{
   private static final Logger log = LoggerFactory.getLogger(PublicanUtil.class);
   private static final String FILESEP = System.getProperty("file.separator");

   private PublicanUtil()
   {
   }

   public static String[] findPotFiles(File potDir) throws IOException
   {
      Collection<File> files = FileUtils.listFiles(potDir, new String[] { "pot" }, true);
      String[] potFiles = new String[files.size()];
      Iterator<File> iter = files.iterator();

      for (int i = 0; i < potFiles.length; i++)
      {
         File potFile = iter.next();
         String relativePath = getSubPath(potDir, potFile);
         potFiles[i] = relativePath;
      }
      return potFiles;
   }

   private static String getSubPath(File potDir, File potFile) throws IOException
   {
      String dirPath = potDir.getAbsolutePath();
      String filePath = potFile.getAbsolutePath();
      assert !dirPath.endsWith(FILESEP);
      assert filePath.startsWith(dirPath);
      return filePath.substring(dirPath.length() + FILESEP.length());
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

   public static List<LocaleMapping> findLocales(File srcDir)
   {
      File[] localeDirs = findLocaleDirs(srcDir);
      List<LocaleMapping> locales = new ArrayList<LocaleMapping>();
      for (File dir : localeDirs)
      {
         locales.add(new LocaleMapping(dir.getName()));
      }
      return locales;
   }

   public static List<LocaleMapping> findLocales(File srcDir, LocaleList locales)
   {
      List<LocaleMapping> localeDirs = new ArrayList<LocaleMapping>();

      for (LocaleMapping loc : locales)
      {
         File localeDir = new File(srcDir, loc.getLocalLocale());
         if (localeDir.isDirectory())
            localeDirs.add(loc);
         else
            log.warn("configured locale {} not found; directory {} does not exist", loc.getLocale(), loc.getLocalLocale());
      }

      return localeDirs;
   }
}
