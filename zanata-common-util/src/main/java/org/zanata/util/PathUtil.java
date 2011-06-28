package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

/**
 * From
 * http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path
 * -in-java-from-two-absolute-paths-or-urls/3054692#3054692
 * 
 */

public class PathUtil
{
   private static final String FILESEP = System.getProperty("file.separator");

   /**
    * Get the relative path from one file to another, specifying the directory
    * separator. If one of the provided resources does not exist, it is assumed
    * to be a file unless it ends with '/' or '\'.
    * 
    * @param target targetPath is calculated to this file
    * @param base basePath is calculated from this file
    * @return
    * @throws PathResolutionException if no relative path exists
    */
   public static String getRelativePath(String targetPath, String basePath) throws PathResolutionException
   {
      return getRelativePath(targetPath, basePath, FILESEP);
   }

   /**
    * Get the relative path from one file to another, specifying the directory
    * separator. If one of the provided resources does not exist, it is assumed
    * to be a file unless it ends with '/' or '\'.
    * 
    * @param target targetPath is calculated to this file
    * @param base basePath is calculated from this file
    * @param separator directory separator. The platform default is not assumed
    *           so that we can test Unix behaviour when running on Windows (for
    *           example)
    * @return
    * @throws PathResolutionException if no relative path exists
    */
   public static String getRelativePath(String targetPath, String basePath, String pathSeparator) throws PathResolutionException
   {

      // Normalize the paths
      String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
      String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

      // Undo the changes to the separators made by normalization
      if (pathSeparator.equals("/"))
      {
         normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
         normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

      }
      else if (pathSeparator.equals("\\"))
      {
         normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
         normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

      }
      else
      {
         throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
      }

      String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
      String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

      // First get all the common elements. Store them as a string,
      // and also count how many of them there are.
      StringBuffer common = new StringBuffer();

      int commonIndex = 0;
      while (commonIndex < target.length && commonIndex < base.length && target[commonIndex].equals(base[commonIndex]))
      {
         common.append(target[commonIndex] + pathSeparator);
         commonIndex++;
      }

      if (commonIndex == 0)
      {
         // No single common path element. This most
         // likely indicates differing drive letters, like C: and D:.
         // These paths cannot be relativized.
         throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath + "'");
      }

      // The number of directories we have to backtrack depends on whether the
      // base is a file or a dir
      // For example, the relative path from
      //
      // /foo/bar/baz/gg/ff to /foo/bar/baz
      //
      // ".." if ff is a file
      // "../.." if ff is a directory
      //
      // The following is a heuristic to figure out if the base refers to a file
      // or dir. It's not perfect, because
      // the resource referred to by this path may not actually exist, but it's
      // the best I can do
      boolean baseIsFile = true;

      File baseResource = new File(normalizedBasePath);

      if (baseResource.exists())
      {
         baseIsFile = baseResource.isFile();

      }
      else if (basePath.endsWith(pathSeparator))
      {
         baseIsFile = false;
      }

      StringBuffer relative = new StringBuffer();

      if (base.length != commonIndex)
      {
         int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

         for (int i = 0; i < numDirsUp; i++)
         {
            relative.append(".." + pathSeparator);
         }
      }
      relative.append(normalizedTargetPath.substring(common.length()));
      return relative.toString();
   }

   /**
    * Returns relative path from parentDir to f. NB: assumes that f is inside
    * parentDir.
    * @param f a file inside parentDir
    * @param parentDir
    * 
    * @return
    * @throws IOException
    * @throws PathResolutionException
    */
   public static String getSubPath(File f, File parentDir) throws IOException, PathResolutionException
   {
      // we could use canonical path here, but we don't want symlinks to be
      // resolved
      String dirPath = parentDir.getAbsolutePath();
      String filePath = f.getAbsolutePath();
      if (!filePath.startsWith(dirPath))
      {
         throw new PathResolutionException("can't find relative path from " + parentDir + " to " + f);
      }
      int skipSize = dirPath.length();
      if (!dirPath.endsWith(FILESEP))
         skipSize++;
      return filePath.substring(skipSize);
   }

   static class PathResolutionException extends RuntimeException
   {
      private static final long serialVersionUID = 1L;

      PathResolutionException(String msg)
      {
         super(msg);
      }
   }

   public static boolean makeParents(File f) throws IOException
   {
      File parent = f.getCanonicalFile().getParentFile();
      if (parent == null || parent.exists())
      {
         return false;
      }
      return parent.mkdirs();
   }

}
