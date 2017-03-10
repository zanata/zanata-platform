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
package org.zanata.page;

import java.util.List;
import java.util.Set;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.util.ShortString;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The base class for the page driver. Contains functionality not generally of a
 * user visible nature.
 */
public class AbstractPage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AbstractPage.class);
    private final WebDriver driver;

    public AbstractPage(final WebDriver driver) {
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, 10),
                this);
        this.driver = driver;
        waitForPageSilence();
    }

    public void reload() {
        log.info("Sys: Reload");
        getDriver().navigate().refresh();
    }

    public void deleteCookiesAndRefresh() {
        log.info("Sys: Delete cookies, reload");
        getDriver().manage().deleteAllCookies();
        Set<Cookie> cookies = getDriver().manage().getCookies();
        if (cookies.size() > 0) {
            log.warn("Failed to delete cookies: {}", cookies);
        }
        getDriver().navigate().refresh();
    }

    public WebDriver getDriver() {
        return driver;
    }

    public JavascriptExecutor getExecutor() {
        return (JavascriptExecutor) getDriver();
    }

    public String getUrl() {
        return driver.getCurrentUrl();
    }

    protected void logWaiting(String msg) {
        log.info("Waiting for {}", msg);
    }

    protected void logFinished(String msg) {
        log.debug("Finished {}", msg);
    }

    public FluentWait<WebDriver> waitForAMoment() {
        return WebElementUtil.waitForAMoment(driver);
    }

    /**
     * TODO: need to replace javascript alert. chromedriver have issue with
     * handling alert popup
     */
    @Deprecated
    public Alert switchToAlert() {
        return waitForAMoment().withMessage("alert")
                .until((Function<WebDriver, Alert>) webDriver -> {
                    try {
                        return getDriver().switchTo().alert();
                    } catch (NoAlertPresentException noAlertPresent) {
                        return null;
                    }
                });
    }

    /**
     * @param currentPage
     * @param predicate
     * @param message
     *            description of predicate
     * @param <P>
     * @return
     */
    protected <P extends AbstractPage> P refreshPageUntil(P currentPage,
            Predicate<WebDriver> predicate, String message) {
        waitForAMoment().withMessage(message).until(predicate);
        PageFactory.initElements(driver, currentPage);
        return currentPage;
    }

    /**
     * @param currentPage
     * @param function
     * @param message
     *            description of function
     * @param <P>
     * @param <T>
     * @return
     */
    protected <P extends AbstractPage, T> T refreshPageUntil(P currentPage,
            Function<WebDriver, T> function, String message) {
        T done = waitForAMoment().withMessage(message).until(function);
        PageFactory.initElements(driver, currentPage);
        return done;
    }

    /**
     * Normally a page has no outstanding ajax requests when it has finished an
     * operation, but some pages use long polling to "push" changes to the user,
     * eg for the editor's event service.
     *
     * @return
     */
    protected int getExpectedBackgroundRequests() {
        return 0;
    }
    // TODO use this to wait for a page load after user input (eg click)

    public void execAndWaitForNewPage(Runnable runnable) {
        final WebElement oldPage = driver.findElement(By.tagName("html"));
        runnable.run();
        String msg = "new page load";
        logWaiting(msg);
        waitForAMoment().withMessage(msg)
                .until((Predicate<WebDriver>) webDriver -> {
                    try {
                        // ignore result
                        oldPage.getAttribute("class");
                        // if we get here, the old page is still there
                        return false;
                    } catch (StaleElementReferenceException e) {
                        // http://www.obeythetestinggoat.com/how-to-get-selenium-to-wait-for-page-load-after-a-click.html
                        //
                        // This exception means the new page has loaded
                        // (or started to).
                        String script =
                                "return document.readyState === \'complete\' && window.deferScriptsFinished";
                        Boolean documentComplete =
                                (Boolean) getExecutor().executeScript(script);
                        // TODO wait for ajax?
                        // NB documentComplete might be null/undefined
                        return documentComplete == Boolean.TRUE;
                    }
                });
        logFinished(msg);
    }

    /**
     * Wait for any AJAX/timeout requests to return.
     */
    public void waitForPageSilence() {
        // TODO wait for any short-lived timeouts to expire (eg less than 1000
        // ms)
        // but not multi-second timeouts (eg global faces messages)
        final String script = "return XMLHttpRequest.active";
        // Wait for AJAX/timeout requests to be 0
        waitForAMoment().withMessage("page silence")
                .until((Predicate<WebDriver>) webDriver -> {
                    Long outstanding =
                            (Long) getExecutor().executeScript(script);
                    if (outstanding == null) {
                        if (log.isWarnEnabled()) {
                            String url = getDriver().getCurrentUrl();
                            String pageSource = ShortString
                                    .shorten(getDriver().getPageSource(), 2000);
                            log.warn(
                                    "XMLHttpRequest.active is null. Is zanata-testing-extension installed? URL: {}\nPartial page source follows:\n{}",
                                    url, pageSource);
                        }
                        return true;
                    }
                    if (outstanding < 0) {
                        throw new RuntimeException(
                                "XMLHttpRequest.active and/or window.timeoutCounter is negative.  Please check the implementation of zanata-testing-extension, and ensure that the injected script is run before any other JavaScript in the page.");
                    }
                    int expected = getExpectedBackgroundRequests();
                    if (outstanding < expected) {
                        log.warn(
                                "Expected at least {} background requests, but actual count is {}",
                                expected, outstanding, new Throwable());
                    } else {
                        log.debug("Waiting: outstanding = {}, expected = {}",
                                outstanding, expected);
                    }
                    return outstanding <= expected;
                });
        waitForLoaders();
    }

    /**
     * Wait for all loaders to be inactive
     */
    private void waitForLoaders() {
        waitForAMoment().withMessage("Loader indicator")
                .until((Predicate<WebDriver>) webDriver -> {
                    // Find all elements with class name js-loader, or return []
                    String script =
                            "return (typeof $ == \'undefined\') ?  [] : $(\'.js-loader\').toArray()";
                    @SuppressWarnings("unchecked")
                    List<WebElement> loaders = (List<WebElement>) getExecutor()
                            .executeScript(script);
                    for (WebElement loader : loaders) {
                        if (loader.getAttribute("class")
                                .contains("is-active")) {
                            log.info("Wait for loader finished");
                            return false;
                        }
                    }
                    return true;
                });
    }

    /**
     * Expect an element to be interactive, and return it
     *
     * @param elementBy
     *            WebDriver By locator
     * @return target WebElement
     */
    public WebElement readyElement(final By elementBy) {
        String msg = "element ready " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        WebElement targetElement = existingElement(elementBy);
        waitForElementReady(targetElement);
        assertReady(targetElement);
        return targetElement;
    }

    /**
     * Wait for a child element to be visible, and return it
     *
     * @param parentElement
     *            parent element of target
     * @param elementBy
     *            WebDriver By locator
     * @return target WebElement
     */
    public WebElement readyElement(final WebElement parentElement,
            final By elementBy) {
        String msg = "element ready " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        WebElement targetElement = existingElement(parentElement, elementBy);
        assertReady(targetElement);
        return targetElement;
    }

    /**
     * Wait for an element to exist on the page, and return it. Generally used
     * for situations where checking on the state of an element, e.g isVisible,
     * rather than clicking on it or getting its text.
     *
     * @param elementBy
     *            WebDriver By locator
     * @return target WebElement
     */
    public WebElement existingElement(final By elementBy) {
        String msg = "element exists " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg)
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    return getDriver().findElement(elementBy);
                });
    }

    /**
     * Wait for a child element to exist on the page, and return it. Generally
     * used for situations where checking on the state of an element, e.g
     * isVisible, rather than clicking on it or getting its text.
     *
     * @param elementBy
     *            WebDriver By locator
     * @return target WebElement
     */
    public WebElement existingElement(final WebElement parentElement,
            final By elementBy) {
        String msg = "element exists " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg)
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    return parentElement.findElement(elementBy);
                });
    }

    /**
     * Convenience function for clicking elements. Removes obstructing elements,
     * scrolls the item into view and clicks it when it is ready.
     *
     * @param findby
     *            locator for element to be clicked
     */
    public void clickElement(By findby) {
        clickElement(readyElement(findby));
    }

    /**
     * Convenience function for clicking elements. Removes obstructing elements,
     * scrolls the item into view and clicks it when it is ready.
     *
     * @param element
     *            element to be clicked
     */
    public void clickElement(final WebElement element) {
        removeNotifications();
        waitForNotificationsGone();
        scrollIntoView(element);
        waitForAMoment().withMessage("clickable: " + element.toString())
                .until(ExpectedConditions.elementToBeClickable(element));
        element.click();
    }

    /**
     * Convenience function for enter text (common case)
     *
     * @param element
     *            element to pass text to
     * @param text
     *            text to be entered
     */
    public void enterText(final WebElement element, final String text) {
        enterText(element, text, true, false, true);
    }

    /**
     * Enter text into an element.
     *
     * Waits for notifications to be dismissed and element to be ready and
     * visible before entering the text. If no checking is performed, the
     * resulting screenshot may not be accurate.
     *
     * @param element
     *            element to pass text to
     * @param text
     *            text to be entered
     * @param clear
     *            clear the element's text before entering new text
     * @param inject
     *            use sendKeys rather than the Actions chain (direct injection)
     * @param check
     *            check the 'value' attribute for success, and accurate
     *            screenshot delay
     */
    public void enterText(final WebElement element, final String text,
            boolean clear, boolean inject, final boolean check) {
        removeNotifications();
        waitForNotificationsGone();
        scrollIntoView(element);
        triggerScreenshot("_pretext");
        waitForAMoment().withMessage("editable: " + element.toString())
                .until(ExpectedConditions.elementToBeClickable(element));
        if (inject) {
            if (clear) {
                element.clear();
            }
            element.sendKeys(text);
        } else {
            Actions enterTextAction =
                    new Actions(getDriver()).moveToElement(element);
            enterTextAction = enterTextAction.click();
            // Fields can 'blur' on click
            waitForPageSilence();
            if (clear) {
                enterTextAction =
                        enterTextAction.sendKeys(Keys.chord(Keys.CONTROL, "a"))
                                .sendKeys(Keys.DELETE);
                // Fields can 'blur' on clear
                waitForPageSilence();
            }
            enterTextAction.sendKeys(text).perform();
        }
        if (check) {
            waitForAMoment().withMessage("Text equal to entered")
                    .until((Predicate<WebDriver>) webDriver -> {
                        String foundText = element.getAttribute("value");
                        if (!text.equals(foundText)) {
                            log.info("Found: {}", foundText);
                            triggerScreenshot("_textWaiting");
                            return false;
                        }
                        return true;
                    });
        } else {
            log.info("Not checking text entered");
        }
        triggerScreenshot("_text");
    }

    /**
     * 'Touch' a text field to see if it's writable. For cases where fields are
     * available but briefly won't accept text for some reason
     *
     * @param textField
     */
    public void touchTextField(WebElement textField) {
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            enterText(textField, ".", true, false, false);
            return textField.getAttribute("value").equals(".");
        });
        textField.clear();
    }

    private void waitForElementReady(final WebElement element) {
        waitForAMoment().withMessage("Waiting for element to be ready")
                .until((Predicate<WebDriver>) webDriver -> element.isDisplayed()
                        && element.isEnabled());
    }
    // Assert the element is available and visible

    private void assertReady(WebElement targetElement) {
        assertThat(targetElement.isDisplayed()).as("displayed").isTrue();
        assertThat(targetElement.isEnabled()).as("enabled").isTrue();
    }

    /**
     * Remove any visible notifications
     */
    public void removeNotifications() {
        @SuppressWarnings("unchecked")
        List<WebElement> notifications =
                (List<WebElement>) getExecutor().executeScript(
                        "return (typeof $ == \'undefined\') ?  [] : $(\'a.message__remove\').toArray()");
        if (notifications.isEmpty()) {
            return;
        }
        log.info("Closing {} notifications", notifications.size());
        for (WebElement notification : notifications) {
            try {
                notification.click();
            } catch (WebDriverException exc) {
                log.info("Missed a notification X click");
            }
        }
        // Finally, forcibly un-is-active the message container - for speed
        String script =
                "return (typeof $ == \'undefined\') ?  [] : $(\'ul.message--global\').toArray()";
        @SuppressWarnings("unchecked")
        List<WebElement> messageBoxes =
                ((List<WebElement>) getExecutor().executeScript(script));
        for (WebElement messageBox : messageBoxes) {
            getExecutor().executeScript(
                    "arguments[0].setAttribute(\'class\', arguments[1]);",
                    messageBox,
                    messageBox.getAttribute("class").replace("is-active", ""));
        }
    }

    /**
     * Wait for the notifications box to go. Assumes test has dealt with
     * removing it, or is waiting for it to time out.
     */
    public void waitForNotificationsGone() {
        final String script =
                "return (typeof $ == \'undefined\') ?  [] : $(\'ul.message--global\').toArray()";
        final String message = "Waiting for notifications box to go";
        waitForAMoment().withMessage(message)
                .until((Predicate<WebDriver>) webDriver -> {
                    @SuppressWarnings("unchecked")
                    List<WebElement> boxes = (List<WebElement>) getExecutor()
                            .executeScript(script);
                    for (WebElement box : boxes) {
                        if (box.isDisplayed()) {
                            log.info(message);
                            return false;
                        }
                    }
                    return true;
                });
    }

    /**
     * Shift focus to the page, in order to activate some elements that only
     * exhibit behaviour on losing focus. Some pages with contained objects
     * cause object behaviour to occur when interacted with, so in this case
     * interact with the container instead.
     */
    public void defocus() {
        log.info("Click off element focus");
        List<WebElement> webElements =
                getDriver().findElements(By.id("container"));
        webElements.addAll(getDriver().findElements(By.tagName("body")));
        if (webElements.size() > 0) {
            webElements.get(0).click();
        } else {
            log.warn("Unable to focus page container");
        }
    }

    /**
     * Force the blur 'unfocus' process on a given element
     */
    public void defocus(By elementBy) {
        log.info("Force unfocus");
        WebElement element = existingElement(elementBy);
        getExecutor().executeScript("arguments[0].blur()", element);
        waitForPageSilence();
    }
    /*
     * The system sometimes moves too fast for the Ajax pages, so provide a
     * pause
     */

    public void slightPause() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            log.warn("Pause was interrupted");
        }
    }

    public void scrollIntoView(WebElement targetElement) {
        getExecutor().executeScript("arguments[0].scrollIntoView(true);",
                targetElement);
    }

    public void scrollToTop() {
        getExecutor().executeScript("scroll(0, 0);");
    }

    public String getHtmlSource(WebElement webElement) {
        return (String) getExecutor()
                .executeScript("return arguments[0].innerHTML;", webElement);
    }

    public void triggerScreenshot(String tag) {
        WebElementUtil.triggerScreenshot(tag);
    }
}
