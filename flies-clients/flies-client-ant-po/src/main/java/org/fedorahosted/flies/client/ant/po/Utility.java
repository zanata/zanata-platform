package org.fedorahosted.flies.client.ant.po;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.tools.ant.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Utility
{

   private static final Logger log = LoggerFactory.getLogger(Utility.class);

   /**
    * Converts a string to a URL. If it doesn't form a valid URL as is, it will
    * be treated as a file and converted to a file: URL. Relative file paths
    * will be interpreted, relative to basedir, and converted to absolute form.
    * 
    * @param src a URL or a file path
    * @param basedir base directory for relative file paths
    * @return URL-equivalent of src
    * @throws MalformedURLException
    */
   public static URL createURL(String src, File basedir) throws MalformedURLException
   {
      URL srcURL;
      try
      {
         srcURL = new URL(src);
      }
      catch (MalformedURLException e)
      {
         File srcFile = new File(src);
         if (!srcFile.isAbsolute())
         {
            srcFile = new File(basedir, src);
         }
         srcURL = srcFile.toURI().toURL();
      }
      return srcURL;
   }

   public static String toString(ClassLoader loader)
   {
      if (loader instanceof URLClassLoader)
      {
         URLClassLoader ul = (URLClassLoader) loader;
         return "URLClassLoader" + Arrays.asList(ul.getURLs()).toString();
      }
      return String.valueOf(loader);
   }

   /**
    * Returns the base directory of the specified project, or user's current
    * working directory if project is null.
    * 
    * @param project
    * @return
    */
   public static File getBaseDir(Project project)
   {
      if (project == null)
         return new File(System.getProperty("user.dir"));
      else
         return project.getBaseDir();
   }

   public static void handleException(Exception e, boolean outputErrors)
   {
      System.err.println("Execution failed: " + e.getMessage());
      if (outputErrors)
      {
         e.printStackTrace();
      }
      else
      {
         System.err.println("Use -e/--errors for full stack trace (or when reporting bugs)");
      }
      System.exit(1);
   }

   public static void printJarVersion(PrintStream out) throws IOException
   {
      Class<Utility> clazz = Utility.class;

      String version = null;
      String buildTimestamp = null;
      CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
      if (codeSource != null)
      {
         String jarLocation = codeSource.getLocation().toString();
         URL manifestUrl = new URL("jar:" + jarLocation + "!/META-INF/MANIFEST.MF");
         try
         {
            Manifest mf = new Manifest(manifestUrl.openStream());
            Attributes atts = mf.getMainAttributes();

            version = atts.getValue("Implementation-Version");
            buildTimestamp = atts.getValue("Implementation-Build");
         }
         catch (IOException e)
         {
            // ignore: probably not running from a jar
            log.debug(e.getMessage(), e);
         }
      }

      // if we can't get version from the jar, try for the package version
      if (version == null)
      {
         Package pkg = clazz.getPackage();
         if (pkg != null)
            version = pkg.getImplementationVersion();
         if (version == null)
            version = "unknown";
      }
      if (buildTimestamp == null)
         buildTimestamp = "unknown";
      out.println("flies-publican");
      out.println("Version: " + version);
      out.println("Build: " + buildTimestamp);
   }

}
