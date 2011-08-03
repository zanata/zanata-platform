package org.zanata.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;

import org.testng.annotations.Test;
import org.zanata.util.PathUtil.PathResolutionException;

@Test(groups = "unit-tests")
public class PathUtilTest
{

   public void testSubPath1() throws Exception
   {
      File dir = new File(".");
      File file = new File(dir, "sub/myfile.pot");
      assertThat(PathUtil.getSubPath(file, dir), is("sub/myfile.pot"));
   }

   public void testSubPath2() throws Exception
   {
      File dir = new File("pot");
      File file = new File("pot/sub/myfile.pot");
      assertThat(PathUtil.getSubPath(file, dir), is("sub/myfile.pot"));
   }

   public void testSubPath3() throws Exception
   {
      File dir = new File("/tmp/pot");
      File file = new File("/tmp/pot/sub/myfile.pot");
      assertThat(PathUtil.getSubPath(file, dir), is("sub/myfile.pot"));
   }


   public void testGetRelativePathsUnix()
   {
      assertThat(PathUtil.getRelativePath("/var/data/stuff/xyz.dat", "/var/data/", "/"), is("stuff/xyz.dat"));
      assertThat(PathUtil.getRelativePath("/a/b/c", "/a/x/y/", "/"), is("../../b/c"));
      assertThat(PathUtil.getRelativePath("/m/n/o/a/b/c", "/m/n/o/a/x/y/", "/"), is("../../b/c"));
   }

   public void testGetRelativePathFileToFile()
   {
      String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
      String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";

      String relPath = PathUtil.getRelativePath(target, base, "\\");
      assertThat(relPath, is("..\\..\\Boot\\Fonts\\chs_boot.ttf"));
   }

   public void testGetRelativePathDirectoryToFile()
   {
      String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
      String base = "C:\\Windows\\Speech\\Common\\";

      String relPath = PathUtil.getRelativePath(target, base, "\\");
      assertThat(relPath, is("..\\..\\Boot\\Fonts\\chs_boot.ttf"));
   }

   public void testGetRelativePathFileToDirectory()
   {
      String target = "C:\\Windows\\Boot\\Fonts";
      String base = "C:\\Windows\\Speech\\Common\\foo.txt";

      String relPath = PathUtil.getRelativePath(target, base, "\\");
      assertThat(relPath, is("..\\..\\Boot\\Fonts"));
   }

   public void testGetRelativePathDirectoryToDirectory()
   {
      String target = "C:\\Windows\\Boot\\";
      String base = "C:\\Windows\\Speech\\Common\\";
      String expected = "..\\..\\Boot";

      String relPath = PathUtil.getRelativePath(target, base, "\\");
      assertThat(relPath, is(expected));
   }

   @Test(expectedExceptions = { PathResolutionException.class })
   public void testGetRelativePathDifferentDriveLetters()
   {
      String target = "D:\\sources\\recovery\\RecEnv.exe";
      String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";

      PathUtil.getRelativePath(target, base, "\\");
   }
}
