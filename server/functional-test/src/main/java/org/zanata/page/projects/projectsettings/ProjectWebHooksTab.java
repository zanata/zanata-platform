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

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import java.util.List;
import java.util.stream.Collectors;
import com.google.common.collect.Lists;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectWebHooksTab extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectWebHooksTab.class);
    private By webHooksForm = By.id("settings-webhooks-form");
    private By newWebHooksForm = By.id("newWebhook");
    private By deleteBtn = By.name("deleteWebhookBtn");
    private By editBtn = By.name("editBtn");
    private final JavascriptExecutor jsExecutor =
            (JavascriptExecutor) getDriver();

    public ProjectWebHooksTab(WebDriver driver) {
        super(driver);
    }

    public WebElement getParentElement(WebElement child) {
        return (WebElement) jsExecutor
                .executeScript("return arguments[0].parentNode;", child);
    }

    public ProjectWebHooksTab enterUrl(String url, String key,
            List<String> types) {
        enterText(getUrlInputField(newWebHooksForm), url);
        enterText(getSecretInputField(newWebHooksForm), key);
        for (String type : types) {
            WebElement checkbox = readyElement(newWebHooksForm)
                    .findElement(By.cssSelector("input[value=" + type + "]"));
            getParentElement(checkbox).click();
        }
        getSaveWebhookButton(newWebHooksForm).click();
        return new ProjectWebHooksTab(getDriver());
    }

    public List<WebhookItem> getWebHooks() {
        List<WebElement> list = getWebhookList();
        if (list.isEmpty()) {
            return Lists.newArrayList();
        }
        return list.stream()
                .map(element -> new WebhookItem(
                        element.findElement(By.name("url")).getText(),
                        getSelectedTypes(element)))
                .collect(Collectors.toList());
    }

    private List<String> getSelectedTypes(WebElement parentForm) {
        return parentForm.findElement(By.name("types"))
                .findElements(By.cssSelector("input[checked=checked]")).stream()
                .map(input -> input.getAttribute("value"))
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
                listItem.findElement(editBtn).click();
                WebElement deleteButton = listItem.findElement(deleteBtn);
                if (!deleteButton.isDisplayed()) {
                    listItem.findElement(editBtn).click();
                }
                deleteButton.click();
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

    static class WebhookItem {
        private String url;
        private List<String> types;

        public String getUrl() {
            return this.url;
        }

        public List<String> getTypes() {
            return this.types;
        }

        @java.beans.ConstructorProperties({ "url", "types" })
        public WebhookItem(final String url, final List<String> types) {
            this.url = url;
            this.types = types;
        }
    }

    private WebElement getSaveWebhookButton(By parentId) {
        return readyElement(parentId).findElement(By.name("addWebhookBtn"));
    }

    private WebElement getUrlInputField(By parentId) {
        return readyElement(parentId).findElement(By.name("payloadUrlInput"));
    }

    private WebElement getSecretInputField(By parentId) {
        return readyElement(parentId).findElement(By.name("secretInput"));
    }
}
