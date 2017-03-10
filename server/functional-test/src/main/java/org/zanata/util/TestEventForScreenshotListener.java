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
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.security.Credentials;
import org.openqa.selenium.support.events.AbstractWebDriverEventListener;
import javax.imageio.ImageIO;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class TestEventForScreenshotListener
        extends AbstractWebDriverEventListener {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TestEventForScreenshotListener.class);
    private final WebDriver driver;
    private String testId = "";
    private boolean handlingException;

    /**
     * A registered TestEventListener will perform actions on navigate, click
     * and exception events
     *
     * @param drv
     *            the WebDriver to derive screen shots from
     */
    public TestEventForScreenshotListener(WebDriver drv) {
        driver = drv;
    }

    /**
     * Update the screen shot directory/filename test ID component
     *
     * @param testId
     *            test identifier string
     */
    public void updateTestID(String testId) {
        this.testId = testId;
    }

    private void createScreenshot(String ofType) {
        File testIDDir = null;
        try {
            testIDDir = ScreenshotDirForTest.screenshotForTest(testId);
            if (!testIDDir.exists()) {
                log.info("[Screenshot]: Creating screenshot dir {}",
                        testIDDir.getAbsolutePath());
                boolean mkdirSuccess = testIDDir.mkdirs();
                assert mkdirSuccess && testIDDir.isDirectory();
            }
            String filename = generateFileName(ofType);
            File screenshotFile = new File(testIDDir, filename);
            Optional<Alert> alert = getAlert(driver);
            BufferedImage capture;
            if (alert.isPresent()) {
                log.error(
                        "[Screenshot]: ChromeDriver screenshot({}) prevented by browser alert. Attempting Robot screenshot instead. Alert text: {}",
                        testId, alert.get().getText());
                // Warning: beta API: if it breaks, try getScreenRectangle()
                Rectangle captureRectangle = getWindowRectangle();
                // Rectangle captureRectangle = getScreenRectangle();
                capture = new Robot().createScreenCapture(captureRectangle);
            } else {
                capture = ImageIO.read(
                        new ByteArrayInputStream(((TakesScreenshot) driver)
                                .getScreenshotAs(OutputType.BYTES)));
            }
            BufferedImage captureWithHeader =
                    addHeader(capture, driver.getCurrentUrl());
            if (!ImageIO.write(captureWithHeader, "png", screenshotFile)) {
                log.error("[Screenshot]: PNG writer not found for {}",
                        filename);
            } else {
                log.info("[Screenshot]: ({})saved to file: {}",
                        driver.getCurrentUrl(), filename);
            }
        } catch (WebDriverException e) {
            throw new RuntimeException("[Screenshot]: Invalid WebDriver: ", e);
        } catch (IOException e) {
            throw new RuntimeException(
                    "[Screenshot]: Failed to write to " + testIDDir, e);
        } catch (NullPointerException e) {
            throw new RuntimeException("[Screenshot]: Null Object: ", e);
        } catch (AWTException e) {
            throw new RuntimeException("[Screenshot]: ", e);
        }
    }
    /*
     * Create a header above the given image, containing the given text
     */

    private BufferedImage addHeader(BufferedImage input, String textStamp) {
        BufferedImage newImg = new BufferedImage(input.getWidth(),
                input.getHeight() + 40, input.getType());
        Graphics graphics = newImg.getGraphics();
        graphics.setColor(new Color(255, 255, 255));
        graphics.fillRect(0, 0, newImg.getWidth(), newImg.getHeight());
        graphics.drawImage(input, 0, 40, null);
        graphics.setFont(new Font("TimesRoman", Font.PLAIN, 12));
        graphics.setColor(new Color(0));
        graphics.drawLine(0, 40, newImg.getWidth(), 40);
        graphics.drawString(textStamp, 20, 20);
        graphics.dispose();
        return newImg;
    }
    // Get the capture dimensions using WebDriver.Window (beta)

    private Rectangle getWindowRectangle() {
        WebDriver.Window window = driver.manage().window();
        Point pos = window.getPosition();
        Dimension size = window.getSize();
        return new Rectangle(pos.x, pos.y, size.width, size.height);
    }
    // Get the capture dimensions using GraphicsEnvironment

    @SuppressWarnings("unused")
    private Rectangle getScreenRectangle() {
        // http://stackoverflow.com/a/13380999/14379
        Rectangle2D result = new Rectangle2D.Double();
        GraphicsEnvironment localGE =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : localGE.getScreenDevices()) {
            for (GraphicsConfiguration graphicsConfiguration : gd
                    .getConfigurations()) {
                Rectangle2D.union(result, graphicsConfiguration.getBounds(),
                        result);
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
            log.error(
                    "[Screenshot]: Skipping screenshot for exception in exception handler",
                    throwable);
            return;
        }
        handlingException = true;
        try {
            createScreenshot("_exc");
            // try to let the browser recover for the next test
            Optional<Alert> alert = getAlert(driver);
            if (alert.isPresent()) {
                log.error(
                        "[Screenshot]: dismissing unexpected alert with text: ",
                        alert.get().getText());
                alert.get().dismiss();
            }
        } catch (Throwable screenshotThrowable) {
            log.error("[Screenshot]: Unable to create exception screenshot",
                    screenshotThrowable);
        } finally {
            handlingException = false;
        }
    }

    public void customEvent(final String tag) {
        createScreenshot(tag);
    }
}
