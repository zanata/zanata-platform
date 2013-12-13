package org.zanata.util;

import java.io.File;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ScreenshotDir {

    private static String baseDirPath =
        PropertiesHolder.getProperty("webdriver.screenshot.dir");
    private static final File baseDir = new File(baseDirPath);

    public static File getScreenshotBaseDir() {
        return baseDir;
    }

    public static boolean isScreenshotEnabled() {
        return baseDirPath != null;
    }

    public static File screenshotForTest(String testId) {
        return new File(baseDir, testId);
    }
}
