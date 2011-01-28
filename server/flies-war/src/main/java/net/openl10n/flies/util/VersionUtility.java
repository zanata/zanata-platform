package net.openl10n.flies.util;

import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
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
      Attributes atts = null;
      String version = null;
      String buildTimestamp = null;
      {
         try
         {
            atts = getJarAttributesForClass(clazz);
         }
         catch (IOException e)
         {
            log.debug(e.getMessage(), e);
         }
      }
      if (atts != null)
      {
         version = atts.getValue("Implementation-Version");
         buildTimestamp = atts.getValue("Implementation-Build");
      }
      
      // if we can't get version from the jar, try for the package version
      if (version == null)
      {
         Package pkg = clazz.getPackage();
         if (pkg != null)
            version = pkg.getImplementationVersion();
      }
      if (version == null)
         version = "unknown";
      if (buildTimestamp == null)
         buildTimestamp = "unknown";
      VersionInfo result = new VersionInfo(version, buildTimestamp);
      return result;
   }
   
   private static Attributes getJarAttributesForClass(Class<?> clazz) throws MalformedURLException, IOException
   {
      // thanks to http://stackoverflow.com/questions/1272648/need-to-read-own-jars-manifest-and-not-root-classloaders-manifest/1273432#1273432
      String className = clazz.getSimpleName() + ".class";
      String classPath = clazz.getResource(className).toString();
      if (classPath.startsWith("vfszip:"))
      {
         String manifestPath = classPath.substring(0, classPath.lastIndexOf(".jar/") + ".jar/".length()) + "META-INF/MANIFEST.MF";
         Manifest manifest = new Manifest(new URL(manifestPath).openStream());
         Attributes attr = manifest.getMainAttributes();
         return attr;
      }
      else if (classPath.startsWith("jar:")) 
      {
         String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + "!".length()) + "/META-INF/MANIFEST.MF";
         Manifest manifest = new Manifest(new URL(manifestPath).openStream());
         Attributes attr = manifest.getMainAttributes();
         return attr;
      }
      return null;
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
