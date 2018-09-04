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
package org.zanata.page.projects.projectsettings

import org.openqa.selenium.By
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebElement
import org.zanata.page.projects.ProjectBasePage
import java.util.stream.Collectors
import com.google.common.collect.Lists
import org.assertj.core.api.Assertions.assertThat
import kotlin.streams.toList

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class ProjectWebHooksTab(driver: WebDriver) : ProjectBasePage(driver) {
    private val webHooksForm = By.id("settings-webhooks-form")
    private val newWebHooksForm = By.id("newWebhook")
    private val deleteBtn = By.name("deleteWebhookBtn")
    private val editBtn = By.name("editBtn")
    private val jsExecutor = driver as JavascriptExecutor

    val webHooks: List<WebhookItem>
        get() {
            val list = webhookList
            return if (list.isEmpty()) {
                Lists.newArrayList()
            } else list.stream()
                    .map { element ->
                        WebhookItem(element.findElement(By.name("url")).text,
                                getSelectedTypes(element))
                    }.toList()
        }

    private val webhookList: List<WebElement>
        get() {
            val listWrapper = readyElement(webHooksForm)
                    .findElements(By.className("list--slat"))
            return if (listWrapper == null || listWrapper.isEmpty()) {
                Lists.newArrayList()
            } else listWrapper[0].findElements(By.className("list-item"))
        }

    private fun getParentElement(child: WebElement): WebElement {
        return jsExecutor
                .executeScript("return arguments[0].parentNode;", child) as WebElement
    }

    fun enterUrl(url: String, key: String,
                 types: List<String>): ProjectWebHooksTab {
        enterText(getUrlInputField(newWebHooksForm), url)
        enterText(getSecretInputField(newWebHooksForm), key)
        for (type in types) {
            val checkbox = readyElement(newWebHooksForm)
                    .findElement(By.cssSelector("input[value=$type]"))
            getParentElement(checkbox).click()
        }
        getSaveWebhookButton(newWebHooksForm).click()
        return ProjectWebHooksTab(driver)
    }

    private fun getSelectedTypes(parentForm: WebElement): List<String> {
        return parentForm.findElement(By.name("types"))
                .findElements(By.cssSelector("input[checked=checked]")).stream()
                .map { input -> input.getAttribute("value") }.toList()
    }

    fun expectWebHooksContains(url: String): ProjectWebHooksTab {
        waitForPageSilence()
        assertThat(webHooks).extracting("url").contains(url)
        return ProjectWebHooksTab(driver)
    }

    fun clickRemoveOn(url: String): ProjectWebHooksTab {
        val listItems = webhookList
        if (listItems.isEmpty()) {
            log.info("Did not find item {}", url)
            return ProjectWebHooksTab(driver)
        }
        var clicked = false
        for (listItem in listItems) {
            if (listItem.text.contains(url)) {
                listItem.findElement(editBtn).click()
                val deleteButton = listItem.findElement(deleteBtn)
                if (!deleteButton.isDisplayed) {
                    listItem.findElement(editBtn).click()
                }
                deleteButton.click()
                clicked = true
                break
            }
        }
        if (!clicked) {
            log.info("Did not find item {}", url)
        }
        return ProjectWebHooksTab(driver)
    }

    class WebhookItem @java.beans.ConstructorProperties("url", "types")
    constructor(val url: String, val types: List<String>)

    private fun getSaveWebhookButton(parentId: By): WebElement {
        return readyElement(parentId).findElement(By.name("addWebhookBtn"))
    }

    private fun getUrlInputField(parentId: By): WebElement {
        return readyElement(parentId).findElement(By.name("payloadUrlInput"))
    }

    private fun getSecretInputField(parentId: By): WebElement {
        return readyElement(parentId).findElement(By.name("secretInput"))
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ProjectWebHooksTab::class.java)
    }
}
