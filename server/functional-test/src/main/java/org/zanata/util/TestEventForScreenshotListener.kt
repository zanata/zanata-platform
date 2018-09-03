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
package org.zanata.util

import java.awt.AWTException
import java.awt.Color
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.awt.Rectangle
import java.awt.Robot
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.util.Date
import java.util.Optional
import javax.imageio.ImageIO

import org.openqa.selenium.Alert
import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.support.events.AbstractWebDriverEventListener

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class TestEventForScreenshotListener
/**
 * A registered TestEventListener will perform actions on navigate, click
 * and exception events
 *
 * @param driver the WebDriver to derive screen shots from
 */
(private val driver: WebDriver) : AbstractWebDriverEventListener() {
    private var testId = ""
    private var handlingException: Boolean = false
    // Get the capture dimensions using WebDriver.Window (beta)

    private val windowRectangle: Rectangle
        get() {
            val window = driver.manage().window()
            val pos = window.position
            val size = window.size
            return Rectangle(pos.x, pos.y, size.width, size.height)
        }
    // Get the capture dimensions using GraphicsEnvironment

    @Suppress("unused")
    private// http://stackoverflow.com/a/13380999/14379
    val screenRectangle: Rectangle
        get() {
            val result = Rectangle2D.Double()
            val localGE = GraphicsEnvironment.getLocalGraphicsEnvironment()
            for (gd in localGE.screenDevices) {
                for (graphicsConfiguration in gd
                        .configurations) {
                    Rectangle2D.union(result, graphicsConfiguration.bounds,
                            result)
                }
            }
            return Rectangle(result.getWidth().toInt(), result.getHeight().toInt())
        }

    /**
     * Update the screen shot directory/filename test ID component
     *
     * @param testId
     * test identifier string
     */
    fun updateTestID(testId: String) {
        this.testId = testId
    }

    private fun createScreenshot(ofType: String) {
        var testIDDir: File? = null
        try {
            testIDDir = screenshotForTest(testId)
            if (!testIDDir.exists()) {
                log.info("[Screenshot]: Creating screenshot dir {}",
                        testIDDir.absolutePath)
                val mkdirSuccess = testIDDir.mkdirs()
                assert(mkdirSuccess && testIDDir.isDirectory)
            }
            val filename = generateFileName(ofType)
            val screenshotFile = File(testIDDir, filename)
            val alert = getAlert(driver)
            val capture: BufferedImage
            capture = if (alert.isPresent) {
                log.error("[Screenshot]: ChromeDriver screenshot({}) prevented by browser alert. Attempting Robot screenshot instead. Alert text: {}",
                        testId, alert.get().text)
                // Warning: beta API: if it breaks, try getScreenRectangle()
                val captureRectangle = windowRectangle
                // Rectangle captureRectangle = getScreenRectangle();
                Robot().createScreenCapture(captureRectangle)
            } else {
                ImageIO.read(
                        ByteArrayInputStream((driver as TakesScreenshot)
                                .getScreenshotAs(OutputType.BYTES)))
            }
            val captureWithHeader = addHeader(capture, driver.currentUrl)
            if (!ImageIO.write(captureWithHeader, "png", screenshotFile)) {
                log.error("[Screenshot]: PNG writer not found for {}",
                        filename)
            } else {
                log.info("[Screenshot]: ({})saved to file: {}",
                        driver.currentUrl, filename)
            }
        } catch (e: WebDriverException) {
            throw RuntimeException("[Screenshot]: Invalid WebDriver: ", e)
        } catch (e: IOException) {
            throw RuntimeException(
                    "[Screenshot]: Failed to write to " + testIDDir!!, e)
        } catch (e: NullPointerException) {
            throw RuntimeException("[Screenshot]: Null Object: ", e)
        } catch (e: AWTException) {
            throw RuntimeException("[Screenshot]: ", e)
        }

    }
    /*
     * Create a header above the given image, containing the given text
     */

    private fun addHeader(input: BufferedImage, textStamp: String): BufferedImage {
        val newImg = BufferedImage(input.width,
                input.height + 40, input.type)
        val graphics = newImg.graphics
        graphics.color = Color(255, 255, 255)
        graphics.fillRect(0, 0, newImg.width, newImg.height)
        graphics.drawImage(input, 0, 40, null)
        graphics.font = Font("TimesRoman", Font.PLAIN, 12)
        graphics.color = Color(0)
        graphics.drawLine(0, 40, newImg.width, 40)
        graphics.drawString(textStamp, 20, 20)
        graphics.dispose()
        return newImg
    }

    private fun generateFileName(ofType: String): String {
        return testId + ":" + Date().time.toString() + ofType + ".png"
    }

    private fun getAlert(driver: WebDriver): Optional<Alert> {
        return try {
            Optional.of(driver.switchTo().alert())
        } catch (nape: NoAlertPresentException) {
            Optional.empty()
        }

    }

    override fun afterNavigateTo(url: String?, driver: WebDriver?) {
        createScreenshot("_nav")
    }

    override fun beforeClickOn(element: WebElement?, driver: WebDriver?) {
        createScreenshot("_preclick")
    }

    override fun afterClickOn(element: WebElement?, driver: WebDriver?) {
        createScreenshot("_click")
    }

    override fun onException(throwable: Throwable?, driver: WebDriver?) {
        if (handlingException) {
            log.error(
                    "[Screenshot]: Skipping screenshot for exception in exception handler",
                    throwable)
            return
        }
        handlingException = true
        try {
            createScreenshot("_exc")
            // try to let the browser recover for the next test
            val alert = getAlert(driver!!)
            if (alert.isPresent) {
                log.error("[Screenshot]: dismissing unexpected alert with text: {}",
                        alert.get().text)
                alert.get().dismiss()
            }
        } catch (screenshotThrowable: Throwable) {
            log.error("[Screenshot]: Unable to create exception screenshot",
                    screenshotThrowable)
        } finally {
            handlingException = false
        }
    }

    fun customEvent(tag: String) {
        createScreenshot(tag)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory
                .getLogger(TestEventForScreenshotListener::class.java)
    }
}
