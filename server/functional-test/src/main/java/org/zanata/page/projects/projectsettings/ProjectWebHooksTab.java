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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ProjectWebHooksTab extends ProjectBasePage {

    private By webHooksForm = By.id("settings-webhooks-form");
    private By saveWebhookButton = By.id("add-webhook-button");
    private By urlInputField = By.id("payloadUrlInput");
    private By secretInputField = By.id("secretInput");

    public ProjectWebHooksTab(WebDriver driver) {
        super(driver);
    }

    public ProjectWebHooksTab enterUrl(String url, String key) {
        enterText(readyElement(urlInputField), url);
        enterText(readyElement(secretInputField), key);
        readyElement(saveWebhookButton).click();
        return new ProjectWebHooksTab(getDriver());
    }

    public List<WebhookItem> getWebHooks() {
        List<WebElement> list = getWebhookList();
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }

        return list.stream().map(element -> new WebhookItem(
            element.findElement(By.name("url")).getText(),
            element.findElement(By.name("type")).getText()))
            .collect(Collectors.toList());
    }

    public ProjectWebHooksTab expectWebHooksContains(final String url) {
        waitForPageSilence();
        assertThat(getWebHooks()).extracting("url").contains(url);
        return new ProjectWebHooksTab(getDriver());
    }

    public ProjectWebHooksTab clickRemoveOn(String url) {
        List<WebElement> listItems = getWebhookList();
        if (listItems.isEmpty()) {
            log.info("Did not find item {}", url);
            return new ProjectWebHooksTab(getDriver());
        }

        boolean clicked = false;
        for (WebElement listItem : listItems) {
            if (listItem.getText().contains(url)) {
                listItem.findElement(By.tagName("button")).click();
                clicked = true;
                break;
            }
        }
        if (!clicked) {
            log.info("Did not find item {}", url);
        }
        return new ProjectWebHooksTab(getDriver());
    }

    private List<WebElement> getWebhookList() {
        List<WebElement> listWrapper = readyElement(webHooksForm)
            .findElements(By.className("list--slat"));
        if (listWrapper == null || listWrapper.isEmpty()) {
            return Lists.newArrayList();
        }
        return listWrapper.get(0).findElements(By.className("list-item"));
    }

    @Getter
    @AllArgsConstructor
    public class WebhookItem {
        private String url;
        private String type;
    }
}
