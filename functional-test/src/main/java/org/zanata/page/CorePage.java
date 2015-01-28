/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.utility.HomePage;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

/**
 * Contains the physical elements, such as page title and home link, that must
 * exist on all Zanata pages.
 *
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class CorePage extends AbstractPage {

    private By homeLink = By.id("home");

    public CorePage(WebDriver driver) {
        super(driver);
        assertNoCriticalErrors();
    }

    public String getTitle() {
        return getDriver().getTitle();
    }

    public HomePage goToHomePage() {
        log.info("Click Zanata home icon");
        scrollToTop();
        waitForWebElement(homeLink).click();
        return new HomePage(getDriver());
    }

    protected void clickAndCheckErrors(WebElement button) {
        button.click();
        List<String> errors = getErrors();
        if (!errors.isEmpty()) {
            throw new RuntimeException(Joiner.on(";").join(errors));
        }
    }

    protected void clickAndExpectErrors(WebElement button) {
        button.click();
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() > 0;
            }
        }, "errors > 0");
    }

    public List<String> getErrors() {
        log.info("Query page errors");
        List<String> oldError =
                WebElementUtil.elementsToText(getDriver(),
                        By.xpath("//span[@class='errors']"));

        List<String> newError =
                WebElementUtil.elementsToText(getDriver(),
                        By.className("message--danger"));

        List<String> allErrors = Lists.newArrayList();
        allErrors.addAll(oldError);
        allErrors.addAll(newError);

        return allErrors;
    }

    /**
     * Wait until expected number of errors presented on page or timeout.
     *
     * @param expectedNumber
     *            expected number of errors on page
     * @return list of error message
     */
    public List<String> getErrors(final int expectedNumber) {
        log.info("Query page errors, expecting {}", expectedNumber);
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() == expectedNumber;
            }
        }, "errors = " + expectedNumber);
        return getErrors();
    }

    /**
     * Wait until at least one error is visible
     *
     * @return The full list of visible errors
     */
    public List<String> expectErrors() {
        waitForPageSilence();
        return getErrors();
    }

    public String getNotificationMessage(By elementBy) {
        log.info("Query notification message: " + elementBy);
        WebElement message = waitForElementExists(elementBy);
        return message.getText();
    }

    public String getNotificationMessage() {
        return getNotificationMessage(By.cssSelector("#messages li"));
    }

    public boolean expectNotification(final String notification) {
        String msg = "notification " + notification;
        logWaiting(msg);
        return waitForAMoment().withMessage(msg).until(
                new Function<WebDriver, Boolean>() {
                    @Override
                    public Boolean apply(WebDriver driver) {
                        List<WebElement> messages = getDriver()
                                .findElement(By.id("messages"))
                                .findElements(By.tagName("li"));
                        List<String> notifications = new ArrayList<String>();
                        for (WebElement message : messages) {
                            notifications.add(message.getText().trim());
                        }
                        return notifications.contains(notification);
                    }
                });
    }

    public void assertNoCriticalErrors() {
        List<WebElement> errors = getDriver()
                .findElements(By.className("alert--danger"));
        if (errors.size() > 0) {
            log.info("Error page displayed");
            throw new RuntimeException("Critical error: \n"
                    + errors.get(0).getText());
        }
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
        WebElement element = getDriver().findElement(elementBy);
        getExecutor().executeScript("arguments[0].blur()", element);
    }

    /* The system sometimes moves too fast for the Ajax pages, so provide a
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
        getExecutor().executeScript(
                "arguments[0].scrollIntoView(true);", targetElement);
    }

    public void scrollToTop() {
        getExecutor().executeScript("scroll(0, 0);");
    }
}
