package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.zanata.page.WebDriverFactory;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ScreenshotEnabledTestRunListener extends RunListener {
    private volatile boolean testFailed;

    @Override
    public void testStarted(Description description) throws Exception {
        super.testStarted(description);
        if (description.getTestClass().getAnnotation(NoScreenshot.class) == null
                && description.getAnnotation(NoScreenshot.class) == null) {
            enableScreenshotForTest(description.getDisplayName());
        }
    }

    @Override
    public void testFinished(Description description) throws Exception {
        super.testFinished(description);
        unregisterScreenshot();
        if (!testFailed) {
            deleteScreenshots(description.getDisplayName());
        }
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        super.testFailure(failure);
        testFailed = true;
    }

    private static void enableScreenshotForTest(String testDisplayName)
            throws Exception {
        WebDriverFactory.INSTANCE.updateListenerTestName(testDisplayName);
        String date = new Date().toString();
        log.debug("[TEST] {}:{}", testDisplayName, date);
    }

    private static void deleteScreenshots(String testDisplayName) {
        File testDir = ScreenshotDirForTest.screenshotForTest(testDisplayName);
        try {
            FileUtils.deleteDirectory(testDir);
        } catch (IOException e) {
            log.warn("error deleting screenshot base directory: {}",
                    e.getMessage());
        }
    }

    private static void unregisterScreenshot() {
        WebDriverFactory.INSTANCE.unregisterScreenshot();
    }
}
