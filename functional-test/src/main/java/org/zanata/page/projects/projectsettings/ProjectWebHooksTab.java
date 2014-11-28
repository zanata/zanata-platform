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
package org.zanata.page.projects.projectsettings;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ProjectWebHooksTab extends ProjectBasePage {

    private By webHooksList = By.id("settings-webhooks-list");
    private By urlInputField = By.id("payloadUrlInput");

    public ProjectWebHooksTab(WebDriver driver) {
        super(driver);
    }

    public ProjectWebHooksTab enterUrl(String url) {
        waitForWebElement(urlInputField).sendKeys(url + Keys.ENTER);
        return new ProjectWebHooksTab(getDriver());
    }

    public List<String> getWebHooks() {
        return WebElementUtil.elementsToText(waitForWebElement(webHooksList)
                .findElement(By.className("list--slat"))
                .findElements(By.className("list-item")));
    }

    public ProjectWebHooksTab waitForWebHooksContains(final String url) {
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getWebHooks().contains(url);
            }
        });
        return new ProjectWebHooksTab(getDriver());
    }

    public ProjectWebHooksTab waitForWebHooksNotContains(final String url) {
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !getWebHooks().contains(url);
            }
        });
        return new ProjectWebHooksTab(getDriver());
    }

    public ProjectWebHooksTab clickRemoveOn(String url) {
        List<WebElement> listItems = waitForWebElement(webHooksList)
                .findElement(By.className("list--slat"))
                .findElements(By.className("list-item"));
        boolean clicked = false;
        for (WebElement listItem : listItems) {
            if (listItem.getText().contains(url)) {
                listItem.findElement(By.tagName("a")).click();
                clicked = true;
                break;
            }
        }
        if (!clicked) {
            log.info("Did not find item {}", url);
        }
        return new ProjectWebHooksTab(getDriver());
    }
}
