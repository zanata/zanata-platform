/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page

import org.assertj.core.api.Assertions.assertThat
import org.openqa.selenium.Alert
import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.Keys
import org.openqa.selenium.NoAlertPresentException
import org.openqa.selenium.StaleElementReferenceException
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.Actions
import org.openqa.selenium.support.PageFactory
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory
import org.openqa.selenium.support.ui.ExpectedConditions
import org.openqa.selenium.support.ui.FluentWait
import org.zanata.page.utility.shortenPageSource
import org.zanata.util.WebElementUtil
import org.zanata.util.until

/**
 * The base class for the page driver. Contains functionality not generally of a
 * user visible nature.
 */
abstract class AbstractPage(val driver: WebDriver) {

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(AbstractPage::class.java)
    }

    init {
        PageFactory.initElements(AjaxElementLocatorFactory(driver, 10),
                this)
        waitForPageSilence()
    }

    fun reload() {
        log.info("Sys: Reload")
        driver.navigate().refresh()
    }

    fun deleteCookiesAndRefresh() {
        log.info("Sys: Delete cookies, reload")
        driver.manage().deleteAllCookies()
        val cookies = driver.manage().cookies
        if (cookies.size > 0) {
            log.warn("Failed to delete cookies: {}", cookies)
        }
        driver.navigate().refresh()
    }

    protected val executor: JavascriptExecutor
        get() = driver as JavascriptExecutor

    @Suppress("UNCHECKED_CAST")
    private fun JavascriptExecutor.executeScriptToElements(script: String): List<WebElement> =
            executeScript(script) as List<WebElement>

    val url: String
        get() = driver.currentUrl

    protected fun logWaiting(msg: String) {
        log.info("Waiting for {}", msg)
    }

    protected fun logFinished(msg: String) {
        log.debug("Finished {}", msg)
    }

    fun waitForAMoment(): FluentWait<WebDriver> {
        return WebElementUtil.waitForAMoment(driver)
    }

    /**
     * TODO: need to replace javascript alert. chromedriver have issue with
     * handling alert popup
     */
    @Deprecated("")
    fun switchToAlert(): Alert? {
        return waitForAMoment()
                .until("alert") { driver ->
                    try {
                        driver.switchTo().alert();
                    } catch (e: NoAlertPresentException) {
                        null;
                    }
                }
    }

    /**
     * @param currentPage
     *
     * @param function
     *
     * @param message
     *            description of function
     *
     * @param P
     *
     * @param T
     *
     * @return
     */
    protected fun <P : AbstractPage, T> refreshPageUntil(currentPage: P,
            message: String, function: (WebDriver) -> T): T {
        val done = waitForAMoment().withMessage(message).until(function)
        PageFactory.initElements(driver, currentPage)
        return done
    }

    /**
     * Normally a page has no outstanding ajax requests when it has finished an
     * operation, but some pages use long polling to "push" changes to the user,
     * eg for the editor's event service.

     * @return
     */
    protected open val expectedBackgroundRequests: Int
        get() = 0

    // TODO use this to wait for a page load after user input (eg click)
    fun execAndWaitForNewPage(runnable: Runnable) {
        val oldPage = driver.findElement(By.tagName("html"))
        runnable.run()
        val msg = "new page load"
        logWaiting(msg)
        waitForAMoment()
                .until(msg, { _ ->
                    try {
                        // ignore result
                        oldPage.getAttribute("class")
                        // if we get here, the old page is still there
                        false
                    } catch (e: StaleElementReferenceException) {
                        // http://www.obeythetestinggoat.com/how-to-get-selenium-to-wait-for-page-load-after-a-click.html
                        //
                        // This exception means the new page has loaded
                        // (or started to).
                        val script = "return document.readyState === \'complete\' && window.deferScriptsFinished"
                        val documentComplete = executor.executeScript(script) as Boolean?
                        // TODO wait for ajax?
                        // NB documentComplete might be null/undefined
                        documentComplete == true
                    }
                })
        logFinished(msg)
    }

    /**
     * Wait for any AJAX/timeout requests to return.
     */
    fun waitForPageSilence() {
        // TODO wait for any short-lived timeouts to expire (eg less than 1000
        // ms)
        // but not multi-second timeouts (eg global faces messages)
        val script = "return XMLHttpRequest.active"
        // Wait for AJAX/timeout requests to be 0
        waitForAMoment().withMessage("page silence")
                .until({ _ ->
                    val outstanding = executor.executeScript(script) as Long?
                    if (outstanding == null) {
                        if (log.isWarnEnabled) {
                            val url = driver.currentUrl
                            val pageSource = driver.shortenPageSource()
                            log.warn(
                                    "XMLHttpRequest.active is null. Is zanata-testing-extension installed? URL: {}\nPartial page source follows:\n{}",
                                    url, pageSource)
                        }
                        return@until true
                    }
                    if (outstanding < 0) {
                        throw RuntimeException(
                                "XMLHttpRequest.active and/or window.timeoutCounter is negative.  Please check the implementation of zanata-testing-extension, and ensure that the injected script is run before any other JavaScript in the page.")
                    }
                    val expected = expectedBackgroundRequests
                    if (outstanding < expected) {
                        log.warn(
                                "Expected at least {} background requests, but actual count is {}",
                                expected, outstanding, Throwable())
                    } else {
                        log.debug("Waiting: outstanding = {}, expected = {}",
                                outstanding, expected)
                    }
                    outstanding <= expected
                })
        waitForLoaders()
    }

    /**
     * Wait for all loaders to be inactive
     */
    private fun waitForLoaders() {
        waitForAMoment().withMessage("Loader indicator")
                .until({ _ ->
                    // Find all elements with class name js-loader, or return []
                    val script = "return (typeof $ == \'undefined\') ?  [] : $(\'.js-loader\').toArray()"
                    val loaders = executor.executeScriptToElements(script)
                    for (loader in loaders) {
                        if (loader.getAttribute("class")
                                .contains("is-active")) {
                            log.info("Wait for loader finished")
                            return@until false
                        }
                    }
                    true
                })
    }

    /**
     * Expect an element to be interactive, and return it

     * @param elementBy
     *            WebDriver By locator
     *
     * @return target WebElement
     */
    fun readyElement(elementBy: By): WebElement {
        val msg = "element ready " + elementBy
        logWaiting(msg)
        waitForPageSilence()
        val targetElement = existingElement(elementBy)
        waitForElementReady(targetElement)
        assertReady(targetElement)
        return targetElement
    }

    /**
     * Wait for a child element to be visible, and return it

     * @param parentElement
     *            parent element of target
     *
     * @param elementBy
     *            WebDriver By locator
     *
     * @return target WebElement
     */
    fun readyElement(parentElement: WebElement,
            elementBy: By): WebElement {
        val msg = "element ready " + elementBy
        logWaiting(msg)
        waitForPageSilence()
        val targetElement = existingElement(parentElement, elementBy)
        assertReady(targetElement)
        return targetElement
    }

    /**
     * Wait for an element to exist on the page, and return it. Generally used
     * for situations where checking on the state of an element, e.g isVisible,
     * rather than clicking on it or getting its text.

     * @param elementBy
     *            WebDriver By locator
     *
     * @return target WebElement
     */
    fun existingElement(elementBy: By): WebElement {
        val msg = "element exists " + elementBy
        logWaiting(msg)
        waitForPageSilence()
        return waitForAMoment()
                .until(msg) { driver -> driver.findElement(elementBy) }
    }

    /**
     * Wait for a child element to exist on the page, and return it. Generally
     * used for situations where checking on the state of an element, e.g
     * isVisible, rather than clicking on it or getting its text.

     * @param elementBy
     *            WebDriver By locator
     *
     * @return target WebElement
     */
    fun existingElement(parentElement: WebElement,
            elementBy: By): WebElement {
        val msg = "element exists " + elementBy
        logWaiting(msg)
        waitForPageSilence()
        return waitForAMoment().withMessage(msg)
                .until(msg, { _ -> parentElement.findElement(elementBy) })
    }

    /**
     * Convenience function for clicking elements. Removes obstructing elements,
     * scrolls the item into view and clicks it when it is ready.

     * @param findby
     *            locator for element to be clicked
     */
    fun clickElement(findby: By) {
        clickElement(readyElement(findby))
    }

    /**
     * Convenience function for clicking elements. Removes obstructing elements,
     * scrolls the item into view and clicks it when it is ready.

     * @param element
     *            element to be clicked
     */
    fun clickElement(element: WebElement) {
        removeNotifications()
        waitForNotificationsGone()
        scrollIntoView(element)
        waitForAMoment().withMessage("clickable: " + element.toString())
                .until(ExpectedConditions.elementToBeClickable(element))
        element.click()
    }

    /**
     * Enter text into an element.

     * Waits for notifications to be dismissed and element to be ready and
     * visible before entering the text. If no checking is performed, the
     * resulting screenshot may not be accurate.

     * @param element
     *            element to pass text to
     *
     * @param text
     *            text to be entered
     *
     * @param clear
     *            clear the element's text before entering new text
     *
     * @param inject
     *            use sendKeys rather than the Actions chain (direct injection)
     *
     * @param check
     *            check the 'value' attribute for success, and accurate
     *            screenshot delay
     */
    @JvmOverloads fun enterText(element: WebElement, text: String,
            clear: Boolean = true, inject: Boolean = false, check: Boolean = true) {
        removeNotifications()
        waitForNotificationsGone()
        scrollIntoView(element)
        triggerScreenshot("_pretext")
        waitForAMoment().withMessage("editable: " + element.toString())
                .until(ExpectedConditions.elementToBeClickable(element))
        if (inject) {
            if (clear) {
                element.clear()
            }
            element.sendKeys(text)
        } else {
            var enterTextAction = Actions(driver).moveToElement(element)
            enterTextAction = enterTextAction.click()
            // Fields can 'blur' on click
            waitForPageSilence()
            if (clear) {
                enterTextAction = enterTextAction.sendKeys(Keys.chord(Keys.CONTROL, "a"))
                        .sendKeys(Keys.DELETE)
                // Fields can 'blur' on clear
                waitForPageSilence()
            }
            enterTextAction.sendKeys(text).perform()
        }
        if (check) {
            waitForAMoment()
                    .until("Text equal to entered", { _ ->
                        val foundText = element.getAttribute("value")
                        if (text != foundText) {
                            log.info("Found: {}", foundText)
                            triggerScreenshot("_textWaiting")
                            false
                        } else true
                    })
        } else {
            log.info("Not checking text entered")
        }
        triggerScreenshot("_text")
    }

    /**
     * 'Touch' a text field to see if it's writable. For cases where fields are
     * available but briefly won't accept text for some reason

     * @param textField
     */
    fun touchTextField(textField: WebElement) {
        waitForAMoment().until({ _ ->
            enterText(textField, ".", true, false, false)
            textField.getAttribute("value") == "."
        })
        textField.clear()
    }

    private fun waitForElementReady(element: WebElement) {
        waitForAMoment().withMessage("Waiting for element to be ready")
                .until({ _ -> element.isDisplayed && element.isEnabled })
    }

    /** Assert the element is available and visible */
    private fun assertReady(targetElement: WebElement) {
        assertThat(targetElement.isDisplayed).`as`("displayed").isTrue()
        assertThat(targetElement.isEnabled).`as`("enabled").isTrue()
    }

    /**
     * Remove any visible notifications
     */
    fun removeNotifications() {
        val notifications = executor.executeScriptToElements(
                "return (typeof $ == \'undefined\') ?  [] : $(\'a.message__remove\').toArray()")
        log.info("Closing {} notifications", notifications.size)
        for (notification in notifications) {
            try {
                notification.click()
            } catch (exc: WebDriverException) {
                log.info("Missed a notification X click")
            }
        }
        // Finally, forcibly un-is-active the message container - for speed
        val script = "return (typeof $ == \'undefined\') ?  [] : $(\'ul.message--global\').toArray()"
        val messageBoxes = executor.executeScriptToElements(script)
        for (messageBox in messageBoxes) {
            executor.executeScript(
                    "arguments[0].setAttribute(\'class\', arguments[1]);",
                    messageBox,
                    messageBox.getAttribute("class").replace("is-active", ""))
        }
    }

    /**
     * Wait for the notifications box to go. Assumes test has dealt with
     * removing it, or is waiting for it to time out.
     */
    fun waitForNotificationsGone() {
        val script = "return (typeof $ == \'undefined\') ?  [] : $(\'ul.message--global\').toArray()"
        val message = "Waiting for notifications box to go"
        waitForAMoment().withMessage(message)
                .until({ _ ->
                    val boxes = executor.executeScriptToElements(script)
                    for (box in boxes) {
                        if (box.isDisplayed) {
                            log.info(message)
                            return@until false
                        }
                    }
                    true
                })
    }

    /**
     * Shift focus to the page, in order to activate some elements that only
     * exhibit behaviour on losing focus. Some pages with contained objects
     * cause object behaviour to occur when interacted with, so in this case
     * interact with the container instead.
     */
    fun defocus() {
        log.info("Click off element focus")
        val webElements = driver.findElements(By.id("container"))
        webElements.addAll(driver.findElements(By.tagName("body")))
        if (webElements.size > 0) {
            webElements[0].click()
        } else {
            log.warn("Unable to focus page container")
        }
        waitForPageSilence();
    }

    /**
     * Force the blur 'unfocus' process on a given element
     */
    fun defocus(elementBy: By) {
        log.info("Force unfocus")
        val element = existingElement(elementBy)
        executor.executeScript("arguments[0].blur()", element)
        waitForPageSilence()
    }
    /*
     * The system sometimes moves too fast for the Ajax pages, so provide a
     * pause
     */

    fun slightPause() {
        try {
            Thread.sleep(500)
        } catch (ie: InterruptedException) {
            log.warn("Pause was interrupted")
        }

    }

    fun scrollIntoView(targetElement: WebElement) {
        executor.executeScript("arguments[0].scrollIntoView(true);",
                targetElement)
    }

    fun scrollToTop() {
        executor.executeScript("scroll(0, 0);")
    }

    fun getHtmlSource(webElement: WebElement): String {
        return executor
                .executeScript("return arguments[0].innerHTML;", webElement) as String
    }

    fun triggerScreenshot(tag: String) {
        WebElementUtil.triggerScreenshot(tag)
    }

    val allWindowHandles: Set<String>
        get() = driver.windowHandles

}
