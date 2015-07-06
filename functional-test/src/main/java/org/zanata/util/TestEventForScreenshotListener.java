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

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TestEventForScreenshotListener extends AbstractWebDriverEventListener {

    private final WebDriver driver;
    private String testId = "";

    /**
     * A registered TestEventListener will perform actions on navigate,
     * click and exception events
     * @param drv the WebDriver to derive screen shots from
     *
     */
    public TestEventForScreenshotListener(WebDriver drv) {
        driver = drv;
    }

    /**
     * Update the screen shot directory/filename test ID component
     * @param testId test identifier string
     */
    public void updateTestID(String testId) {
        this.testId = testId;
    }

    private void createScreenshot(String ofType) {
        File testIDDir = null;
        try {
            testIDDir = ScreenshotDirForTest.screenshotForTest(testId);
            if (!testIDDir.exists()) {
                log.info("Creating screenshot dir {}", testIDDir.getAbsolutePath());
                assert (testIDDir.mkdirs());
            }
            File screenshotFile =
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile,
                new File(testIDDir, generateFileName(ofType)));

        } catch (WebDriverException wde) {
            throw new RuntimeException("[Screenshot]: Invalid WebDriver: "
                    + wde.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException("[Screenshot]: Failed to write to "
                    + testIDDir);
        } catch (NullPointerException npe) {
            throw new RuntimeException("[Screenshot]: Null Object: "
                    + npe.getMessage());
        }
    }

    private String generateFileName(String ofType) {
        return testId.concat(":").concat(String.valueOf(new Date().getTime()))
                .concat(ofType).concat(".png");
    }

    private boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException nape) {
            return false;
        }
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        createScreenshot("_nav");
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        createScreenshot("_preclick");
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        if (isAlertPresent(driver)) {
            log.info("[Screenshot]: Prevented by Alert");
            return;
        }
        createScreenshot("_click");
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        try {
            createScreenshot("_exc");
        } catch (Throwable all) {
            log.error("error creating screenshot on exception");
        }
    }

}
