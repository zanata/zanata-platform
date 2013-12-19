package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zanata.page.WebDriverFactory;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TakeScreenshotRule implements TestRule {

    @Override
    public Statement apply(final Statement base, Description description) {
        final String testDisplayName = description.getDisplayName();

        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                enableScreenshotForTest(testDisplayName);
                try {
                    base.evaluate();
                } catch (Throwable throwable) {
                    throw throwable;
                } finally {
                    unregisterScreenshot();
                }
                // if we are here the test passes
                deleteScreenshots(testDisplayName);
            }
        };
    }

    private static void enableScreenshotForTest(String testDisplayName)
            throws Throwable {
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
