package org.zanata.util;

import java.io.File;

import org.junit.Test;
import org.zanata.util.PathUtil.PathResolutionException;

import static org.assertj.core.api.Assertions.assertThat;

public class PathUtilTest {

    @Test
    public void testSubPath1() throws Exception {
        File dir = new File(".");
        File file = new File(dir, "sub/myfile.pot");
        assertThat(PathUtil.getSubPath(file, dir)).isEqualTo("sub" + File.separator
                + "myfile.pot");
    }

    @Test
    public void testSubPath2() throws Exception {
        File dir = new File("pot");
        File file = new File("pot/sub/myfile.pot");
        assertThat(PathUtil.getSubPath(file, dir)).isEqualTo("sub" + File.separator
                + "myfile.pot");
    }

    @Test
    public void testSubPath3() throws Exception {
        File dir = new File("/tmp/pot");
        File file = new File("/tmp/pot/sub/myfile.pot");
        assertThat(PathUtil.getSubPath(file, dir)).isEqualTo("sub" + File.separator
                + "myfile.pot");
    }

    @Test
    public void testGetRelativePathsUnix() {
        assertThat(PathUtil.getRelativePath("/var/data/stuff/xyz.dat",
                "/var/data/", "/")).isEqualTo("stuff/xyz.dat");
        assertThat(PathUtil.getRelativePath("/a/b/c", "/a/x/y/", "/"))
                .isEqualTo("../../b/c");
        assertThat(
                PathUtil.getRelativePath("/m/n/o/a/b/c", "/m/n/o/a/x/y/", "/"))
                .isEqualTo("../../b/c");
    }

    @Test
    public void testGetRelativePathFileToFile() {
        String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
        String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";

        String relPath = PathUtil.getRelativePath(target, base, "\\");
        assertThat(relPath).isEqualTo("..\\..\\Boot\\Fonts\\chs_boot.ttf");
    }

    @Test
    public void testGetRelativePathDirectoryToFile() {
        String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
        String base = "C:\\Windows\\Speech\\Common\\";

        String relPath = PathUtil.getRelativePath(target, base, "\\");
        assertThat(relPath).isEqualTo("..\\..\\Boot\\Fonts\\chs_boot.ttf");
    }

    @Test
    public void testGetRelativePathFileToDirectory() {
        String target = "C:\\Windows\\Boot\\Fonts";
        String base = "C:\\Windows\\Speech\\Common\\foo.txt";

        String relPath = PathUtil.getRelativePath(target, base, "\\");
        assertThat(relPath).isEqualTo("..\\..\\Boot\\Fonts");
    }

    @Test
    public void testGetRelativePathDirectoryToDirectory() {
        String target = "C:\\Windows\\Boot\\";
        String base = "C:\\Windows\\Speech\\Common\\";
        String expected = "..\\..\\Boot";

        String relPath = PathUtil.getRelativePath(target, base, "\\");
        assertThat(relPath).isEqualTo(expected);
    }

    @Test(expected = PathResolutionException.class)
    public void testGetRelativePathDifferentDriveLetters() {
        String target = "D:\\sources\\recovery\\RecEnv.exe";
        String base =
                "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";

        PathUtil.getRelativePath(target, base, "\\");
    }
}
