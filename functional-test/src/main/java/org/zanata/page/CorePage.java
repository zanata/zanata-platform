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

    @FindBy(id = "home")
    private WebElement homeLink;

    public CorePage(WebDriver driver) {
        super(driver);
    }

    public String getTitle() {
        return getDriver().getTitle();
    }

    public HomePage goToHomePage() {
        homeLink.click();
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
        });
    }

    public List<String> getErrors() {
        List<String> oldError =
                WebElementUtil.elementsToText(getDriver(),
                        By.xpath("//span[@class='errors']"));

        List<String> newError =
                WebElementUtil.elementsToText(getDriver(),
                        By.xpath("//div[@class='message--danger']"));

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
        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getErrors().size() == expectedNumber;
            }
        });
        return getErrors();
    }

    public String getNotificationMessage() {
        List<WebElement> messages =
                getDriver().findElement(By.id("messages"))
                        .findElements(By.tagName("li"));
        return messages.size() > 0 ? messages.get(0).getText() : "";
    }

    public void waitForNotificationMessage(final String expectedMessage) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                List<WebElement> messages =
                        getDriver().findElement(By.id("messages"))
                                .findElements(By.tagName("li"));
                for( WebElement message : messages ) {
                    if( message.getText().trim().equals( expectedMessage ) ) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public List<String> waitForErrors() {
        waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return getDriver().findElement(
                        By.xpath("//span[@class='errors']"));
            }
        });
        return getErrors();
    }

    public void assertNoCriticalErrors() {
        List<WebElement> errors =
                getDriver().findElements(By.id("errorMessage"));
        if (errors.size() > 0) {
            throw new RuntimeException("Critical error: \n"
                    + errors.get(0).getText());
        }
    }

    public boolean hasNoCriticalErrors() {
        return getDriver().findElements(By.id("errorMessage")).size() <= 0;
    }

    /**
     * Shift focus to the page, in order to activate some elements that only
     * exhibit behaviour on losing focus. Some pages with contained objects
     * cause object behaviour to occur when interacted with, so in this case
     * interact with the container instead.
     */
    public void defocus() {
        List<WebElement> webElements =
                getDriver().findElements(By.id("container"));
        webElements.addAll(getDriver().findElements(By.tagName("body")));
        if (webElements.size() > 0) {
            webElements.get(0).click();
        } else {
            log.warn("Unable to focus page container");
        }
    }

    public List<String> waitForFieldErrors() {
        waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return getDriver().findElement(By.className("message--danger"));
            }
        });
        return getFieldErrors();
    }

    public List<String> getFieldErrors() {
        List<String> errorList = new ArrayList<String>();
        List<WebElement> errorObjects =
                getDriver().findElements(By.className("message--danger"));
        for (WebElement element : errorObjects) {
            errorList.add(element.getText().trim());
        }
        return errorList;
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
}
