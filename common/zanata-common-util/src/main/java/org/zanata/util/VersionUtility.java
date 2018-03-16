package org.zanata.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.VersionInfo;

public class VersionUtility {
    private static final Logger log = LoggerFactory
            .getLogger(VersionUtility.class);
    private static VersionInfo apiVersion;

    public static VersionInfo getAPIVersionInfo() {
        if (apiVersion == null) {
            // LocaleId jar version (zanata-common-api) is used as an
            // "API version"
            apiVersion = getVersionInfo(LocaleId.class);
        }
        return new VersionInfo(apiVersion);
    }

    /**
     * Gets version from manifest of jar containing clazz, or else from
     * package info.
     * @param clazz
     * @return
     */
    public static VersionInfo getVersionInfo(Class<?> clazz) {
        Attributes atts = null;
        try {
            atts = getJarAttributesForClass(clazz);
        } catch (IOException e) {
            log.debug("Couldn't find attributes", e);
        }
        return getVersionInfo(atts, clazz);
    }

    /**
     * Gets version from specified manifest attributes, or else from the
     * package info of the class.
     * @param atts
     * @param fallbackClass
     * @return
     */
    public static VersionInfo getVersionInfo(@Nullable Attributes atts, Class<?> fallbackClass) {
        String version = null;
        String buildTimestamp = null;
        String scmDescribe = null;

        if (atts != null) {
            version = atts.getValue("Implementation-Version");
            buildTimestamp = atts.getValue("Implementation-Build");
            scmDescribe = atts.getValue("SCM-Describe");
        }
        // if we can't get version from the jar, try for the package version
        if (version == null) {
            Package pkg = fallbackClass.getPackage();
            if (pkg != null)
                version = pkg.getImplementationVersion();
        }
        if (version == null) {
            version = "unknown";
        }
        if (buildTimestamp == null) {
            buildTimestamp = "unknown";
        }
        if (scmDescribe  == null) {
            scmDescribe = "unknown";
        }
        return new VersionInfo(version, buildTimestamp, scmDescribe);
    }

    private static Attributes getJarAttributesForClass(Class<?> clazz)
            throws MalformedURLException, IOException {
        // thanks to
        // http://stackoverflow.com/questions/1272648/need-to-read-own-jars-manifest-and-not-root-classloaders-manifest/1273432#1273432
        String className = clazz.getSimpleName() + ".class";
        String classPath = clazz.getResource(className).toString();
        // PORTABILITY (JBoss AS/EAP)
        if (classPath.startsWith("vfszip:")) {
            String manifestPath =
                    classPath.substring(0, classPath.lastIndexOf(".jar/")
                            + ".jar/".length())
                            + "META-INF/MANIFEST.MF";
            Manifest manifest =
                    new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            return attr;
        } else if (classPath.startsWith("jar:")) {
            String manifestPath =
                    classPath.substring(0,
                            classPath.lastIndexOf("!") + "!".length())
                            + "/META-INF/MANIFEST.MF";
            Manifest manifest =
                    new Manifest(new URL(manifestPath).openStream());
            Attributes attr = manifest.getMainAttributes();
            return attr;
        }
        return null;
    }

    public static void printVersions(Class<?> clientClass, PrintWriter out) {
        VersionInfo clientVer = getVersionInfo(clientClass);
        out.println("Client version: " + clientVer.getVersionNo());
        out.println("Client timestamp: " + clientVer.getBuildTimeStamp());
        out.println("Client SCM describe: " + clientVer.getScmDescribe());
        VersionInfo apiVer = getAPIVersionInfo();
        out.println("API version: " + apiVer.getVersionNo());
        out.println("API timestamp: " + apiVer.getBuildTimeStamp());
        out.println("API SCM describe: " + apiVer.getScmDescribe());
    }

}
