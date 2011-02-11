package net.openl10n.flies.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.VersionInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see net.openl10n.flies.client.VersionUtility
 */
//FIXME merge flies.client.VersionUtility and flies.util.VersionUtility into flies.common.util.VersionUtility (must prevent GWT compilation)
public class VersionUtility
{
   private static final Logger log = LoggerFactory.getLogger(VersionUtility.class);
   private static VersionInfo apiVersion;

   public static VersionInfo getAPIVersionInfo()
   {
      if (apiVersion == null)
      {
         // LocaleId jar version (flies-common-api) is used as an "API version"
         apiVersion = getVersionInfo(LocaleId.class);
      }
      return new VersionInfo(apiVersion);
   }

   public static VersionInfo getVersionInfo(Class<?> clazz)
   {
      String version = null;
      String buildTimestamp = null;
      CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
      if (codeSource != null)
      {
         String jarLocation = codeSource.getLocation().toString();
         InputStream in=null;
         try
         {
            URL manifestUrl = new URL("jar:" + jarLocation + "!/META-INF/MANIFEST.MF");
            in=manifestUrl.openStream();
            Manifest mf = new Manifest(in);
            Attributes atts = mf.getMainAttributes();

            version = atts.getValue("Implementation-Version");
            buildTimestamp = atts.getValue("Implementation-Build");
         }
         catch (IOException e)
         {
            // ignore: probably not running from a jar
            log.debug(e.getMessage(), e);
         }finally{
            if (in != null)
            {
               try
               {
                  in.close();
               }
               catch (IOException e)
               {
               }
            }
            
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
      VersionInfo result = new VersionInfo(version, buildTimestamp);
      return result;
   }

   public static void printVersions(Class<?> clientClass, PrintStream out)
   {
      VersionInfo clientVer = getVersionInfo(clientClass);
      out.println("Client version: " + clientVer.getVersionNo());
      out.println("Client timestamp: " + clientVer.getBuildTimeStamp());
      VersionInfo apiVer = getAPIVersionInfo();
      out.println("API version: " + apiVer.getVersionNo());
      out.println("API timestamp: " + apiVer.getBuildTimeStamp());
   }

}
