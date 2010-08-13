package org.fedorahosted.flies.client;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionUtility
{

   private static final Logger log = LoggerFactory.getLogger(VersionUtility.class);

   public static void printJarVersion(PrintStream out)
   {
      Class<VersionUtility> clazz = VersionUtility.class;

      String version = null;
      String buildTimestamp = null;
      CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
      if (codeSource != null)
      {
         String jarLocation = codeSource.getLocation().toString();
         try
         {
            URL manifestUrl = new URL("jar:" + jarLocation + "!/META-INF/MANIFEST.MF");
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
