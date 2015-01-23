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
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.openqa.selenium.*;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.FluentWait;
import org.zanata.util.ShortString;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Predicate;

/**
 * The base class for the page driver. Contains functionality not generally of
 * a user visible nature.
 */
@Slf4j
public class AbstractPage {
    private final WebDriver driver;
    private final FluentWait<WebDriver> ajaxWaitForSec;

    public AbstractPage(final WebDriver driver) {
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, 10),
                this);
        this.driver = driver;
        ajaxWaitForSec = WebElementUtil.waitForAMoment(driver);
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

    public String getUrl() {
        return driver.getCurrentUrl();
    }

    protected void logWaiting(String msg) {
        log.info("Waiting for {}", msg);
    }

    public FluentWait<WebDriver> waitForAMoment() {
        return ajaxWaitForSec;
    }

    /**
     * Wait for all necessary elements to be available on page.
     * @param elementBys
     *            selenium search criteria for locating elements
     */
    public void waitForPage(List<By> elementBys) {
        for (final By by : elementBys) {
            waitForElementExists(by);
        }
    }

    public Alert switchToAlert() {
        return waitForAMoment().withMessage("alert").until(new Function<WebDriver, Alert>() {
            @Override
            public Alert apply(WebDriver driver) {
                try {
                    return getDriver().switchTo().alert();
                } catch (NoAlertPresentException noAlertPresent) {
                    return null;
                }
            }
        });
    }

    /**
     * @deprecated Use the overload which includes a message
     */
    @Deprecated
    protected <P extends AbstractPage> P refreshPageUntil(P currentPage,
            Predicate<WebDriver> predicate) {
        return refreshPageUntil(currentPage, predicate, null);
    }

    /**
     * @param currentPage
     * @param predicate
     * @param message description of predicate
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
     * @deprecated Use the overload which includes a message
     */
    @Deprecated
    protected <P extends AbstractPage, T> T refreshPageUntil(P currentPage,
            Function<WebDriver, T> function) {
        return refreshPageUntil(currentPage, function, null);
    }

    /**
     *
     * @param currentPage
     * @param function
     * @param message description of function
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
     * Wait for certain condition to happen.
     *
     * For example, wait for a translation updated event gets broadcast to editor.
     *
     * @param callable a callable that returns a result
     * @param matcher a matcher that matches to expected result
     * @param <T> result type
     */
    public <T> void
            waitFor(final Callable<T> callable, final Matcher<T> matcher) {
        waitForAMoment().withMessage(StringDescription.toString(matcher)).until(
                new Predicate<WebDriver>() {
                    @Override
                    public boolean apply(WebDriver input) {
                        try {
                            T result = callable.call();
                            if (!matcher.matches(result)) {
                                matcher.describeMismatch(result,
                                        new Description.NullDescription());
                            }
                            return matcher.matches(result);
                        } catch (Exception e) {
                            log.warn("exception", e);
                            return false;
                        }
                    }
                });
    }

    /**
     * Normally a page has no outstanding ajax requests when it has
     * finished an operation, but some pages use long polling to
     * "push" changes to the user, eg for the editor's event service.
     * @return
     */
    protected int getExpectedBackgroundRequests() {
        return 0;
    }

    /**
     * Wait for any AJAX calls to return.
     */
    public void waitForPageSilence() {
        // Wait for AJAX calls to be 0
        waitForAMoment().withMessage("page silence").until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                Long ajaxCalls = (Long) ((JavascriptExecutor) getDriver())
                        .executeScript(
                                "return XMLHttpRequest.active");
                if (ajaxCalls == null) {
                    if (log.isWarnEnabled()) {
                        String url = getDriver().getCurrentUrl();
                        String pageSource = ShortString.shorten(getDriver().getPageSource(), 2000);
                        log.warn("XMLHttpRequest.active is null.  URL: {}\nPartial page source follows:\n{}", url, pageSource);
                    }
                    return true;
                }
                if (ajaxCalls < 0) {
                    throw new RuntimeException("XMLHttpRequest.active " +
                            "is negative.  Please ensure that " +
                            "AjaxCounterBean's script is run before " +
                            "any AJAX requests.");
                }
                return ajaxCalls <= getExpectedBackgroundRequests();
            }
        });
    }

    /**
     * Wait for an element to be visible, and return it
     * @param elementBy WebDriver By locator
     * @return target WebElement
     */
    public WebElement waitForWebElement(final By elementBy) {
        String msg = "element ready " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg).until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                WebElement targetElement = getDriver().findElement(elementBy);
                if (!elementIsReady(targetElement)) {
                    return null;
                }
                return targetElement;
            }
        });
    }

    /**
     * Wait for a child element to be visible, and return it
     * @param parentElement parent element of target
     * @param elementBy WebDriver By locator
     * @return target WebElement
     */
    public WebElement waitForWebElement(final WebElement parentElement,
                                        final By elementBy) {
        String msg = "element ready " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg).until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                WebElement targetElement = parentElement.findElement(elementBy);
                if (!elementIsReady(targetElement)) {
                    return null;
                }
                return targetElement;
            }
        });
    }

    /**
     * Wait for an element to exist on the page, and return it.
     * Generally used for situations where checking on the state of an element,
     * e.g isVisible, rather than clicking on it or getting its text.
     * @param elementBy WebDriver By locator
     * @return target WebElement
     */
    public WebElement waitForElementExists(final By elementBy) {
        String msg = "element exists " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg).until(
                new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return getDriver().findElement(elementBy);
            }
        });
    }

    /**
     * Wait for a child element to exist on the page, and return it.
     * Generally used for situations where checking on the state of an element,
     * e.g isVisible, rather than clicking on it or getting its text.
     * @param elementBy WebDriver By locator
     * @return target WebElement
     */
    public WebElement waitForElementExists(final WebElement parentElement,
                                           final By elementBy) {
        String msg = "element exists " + elementBy;
        logWaiting(msg);
        waitForPageSilence();
        return waitForAMoment().withMessage(msg).until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                return parentElement.findElement(elementBy);
            }
        });
    }

    private boolean elementIsReady(WebElement targetElement) {
        return targetElement.isDisplayed() && targetElement.isEnabled();
    }
}
