/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.zanata.page.WebDriverFactory;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ScreenshotEnabledTestRunListener extends RunListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(ScreenshotEnabledTestRunListener.class);

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
        WebDriverFactory.INSTANCE.registerScreenshotListener(testDisplayName);
        String date = new Date().toString();
        log.debug("[TEST] {}:{}", testDisplayName, date);
    }

    private static void deleteScreenshots(String testDisplayName) {
        File testDir = ScreenshotDirForTest.screenshotForTest(testDisplayName);
        try {
            log.info("Deleting screenshots for {}", testDisplayName);
            FileUtils.deleteDirectory(testDir);
        } catch (IOException e) {
            log.warn("error deleting screenshot base directory: {}",
                    e.getMessage());
        }
    }

    private static void unregisterScreenshot() {
        WebDriverFactory.INSTANCE.unregisterScreenshotListener();
    }
}
