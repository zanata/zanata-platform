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
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.more.MorePage;
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
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class CorePage extends AbstractPage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CorePage.class);
    private By homeLink = By.id("nav_home");
    private By moreLink = By.id("nav_more");

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
        clickElement(homeLink);
        return new HomePage(getDriver());
    }

    public MorePage gotoMorePage() {
        log.info("Click More icon");
        clickElement(moreLink);
        return new MorePage(getDriver());
    }

    protected void clickAndCheckErrors(WebElement button) {
        clickElement(button);
        List<String> errors = getErrors();
        if (!errors.isEmpty()) {
            throw new RuntimeException(Joiner.on(";").join(errors));
        }
    }

    protected void clickAndExpectErrors(WebElement button) {
        clickElement(button);
        refreshPageUntil(this, new Predicate<WebDriver>() {

            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() > 0;
            }
        }, "errors > 0");
    }

    public List<String> getErrors() {
        log.info("Query page errors");
        List<String> oldError = WebElementUtil.elementsToText(getDriver(),
                By.xpath("//span[@class=\'errors\']"));
        // app-error is a pseudo class we put in just for this
        List<String> newError = WebElementUtil.elementsToText(getDriver(),
                By.className("app-error"));
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
        refreshPageUntil(this,
                (Predicate<WebDriver>) webDriver -> getErrors()
                        .size() == expectedNumber,
                "errors = " + expectedNumber);
        return getErrors();
    }

    /**
     * Wait until an expected error is visible
     *
     * @param expected
     *            The expected error string
     * @return The full list of visible errors
     */
    public List<String> expectError(final String expected) {
        String msg = "expected error: " + expected;
        logWaiting(msg);
        waitForAMoment().withMessage(msg)
                .until((Predicate<WebDriver>) webDriver -> getErrors()
                        .contains(expected));
        return getErrors();
    }

    public String getNotificationMessage(By elementBy) {
        log.info("Query notification message");
        List<WebElement> messages =
                existingElement(elementBy).findElements(By.tagName("li"));
        return messages.size() > 0 ? messages.get(0).getText() : "";
    }

    public String getNotificationMessage() {
        return getNotificationMessage(By.id("messages"));
    }

    public boolean expectNotification(final String notification) {
        String msg = "notification " + notification;
        logWaiting(msg);
        return waitForAMoment().withMessage(msg)
                .until((Function<WebDriver, Boolean>) driver -> {
                    List<WebElement> messages =
                            getDriver().findElement(By.id("messages"))
                                    .findElements(By.tagName("li"));
                    List<String> notifications = new ArrayList<String>();
                    for (WebElement message : messages) {
                        notifications.add(message.getText().trim());
                    }
                    if (!notifications.isEmpty()) {
                        triggerScreenshot("_notify");
                        log.info("Notifications: {}", notifications);
                    }
                    return notifications.contains(notification);
                });
    }

    public void assertNoCriticalErrors() {
        List<WebElement> errors =
                getDriver().findElements(By.className("alert--danger"));
        if (errors.size() > 0) {
            log.info("Error page displayed");
            throw new RuntimeException(
                    "Critical error: \n" + errors.get(0).getText());
        }
    }
}
