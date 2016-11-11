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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.security.Credentials;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;

import javax.imageio.ImageIO;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TestEventForScreenshotListener extends AbstractWebDriverEventListener {

    private final WebDriver driver;
    private String testId = "";
    private boolean handlingException;

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
                testIDDir.mkdirs();
                assert testIDDir.isDirectory();
            }
            String filename = generateFileName(ofType);
            File screenshotFile = new File(testIDDir, filename);

            Optional<Alert> alert = getAlert(driver);
            if (alert.isPresent()) {
                log.error("ChromeDriver screenshot({}) prevented by browser " +
                        "alert. Attempting Robot screenshot instead. " +
                        "Alert text: {}",
                        testId, alert.get().getText());

                // warning: beta API: if it breaks, you can always use getScreenRectangle()
                WebDriver.Window window = driver.manage().window();
                Point pos = window.getPosition();
                Dimension size = window.getSize();

                Rectangle captureRectangle = new Rectangle(pos.x, pos.y, size.width, size.height);
//                Rectangle captureRectangle = getScreenRectangle();

                BufferedImage capture = new Robot().createScreenCapture(
                        captureRectangle);
                if (!ImageIO.write(capture, "png", screenshotFile)) {
                    log.error("png writer not found for screenshot");
                }
            } else {
                File tempFile =
                        ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
                FileUtils.moveFile(tempFile, screenshotFile);
            }
            log.info("Screenshot saved to file: {}", filename);
        } catch (WebDriverException e) {
            throw new RuntimeException("[Screenshot]: Invalid WebDriver: ", e);
        } catch (IOException e) {
            throw new RuntimeException("[Screenshot]: Failed to write to "
                    + testIDDir, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("[Screenshot]: Null Object: ", e);
        } catch (AWTException e) {
            throw new RuntimeException("[Screenshot]: ", e);
        }
    }

    private Rectangle getScreenRectangle() {
        // http://stackoverflow.com/a/13380999/14379
        Rectangle2D result = new Rectangle2D.Double();
        GraphicsEnvironment localGE = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : localGE.getScreenDevices()) {
            for (GraphicsConfiguration graphicsConfiguration : gd.getConfigurations()) {
                Rectangle2D.union(result, graphicsConfiguration.getBounds(), result);
            }
        }
        return new Rectangle((int) result.getWidth(), (int) result.getHeight());
    }

    private String generateFileName(String ofType) {
        return testId.concat(":").concat(String.valueOf(new Date().getTime()))
                .concat(ofType).concat(".png");
    }

    private Optional<Alert> getAlert(WebDriver driver) {
        try {
            return Optional.of(driver.switchTo().alert());
        } catch (NoAlertPresentException nape) {
            return Optional.empty();
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
        createScreenshot("_click");
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        if (handlingException) {
            log.error("skipping screenshot for exception in exception handler", throwable);
            return;
        }
        handlingException = true;
        try {
            createScreenshot("_exc");
            // try to let the browser recover for the next test
            Optional<Alert> alert = getAlert(driver);
            if (alert.isPresent()) {
                log.error("dismissing unexpected alert with text: ", alert.get().getText());
                alert.get().dismiss();
            }
        } catch (Throwable screenshotThrowable) {
            log.error("unable to create exception screenshot", screenshotThrowable);
        } finally {
            handlingException = false;
        }
    }

    public void customEvent(final String tag) {
        createScreenshot(tag);
    }

}
