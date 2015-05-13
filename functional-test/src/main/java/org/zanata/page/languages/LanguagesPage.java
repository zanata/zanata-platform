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
package org.zanata.page.languages;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.administration.AddLanguagePage;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class LanguagesPage extends BasePage {

    private By moreActions = By.id("more-actions");
    private By addLanguageLink = By.linkText("Add New Language");
    private By enabledByDefaultLabel = By.className("label");

    public LanguagesPage(WebDriver driver) {
        super(driver);
    }

    public LanguagePage selectLanguage(String language) {
        log.info("Select {} from the language list", language);
        findRowByLocale(language).click();
        return new LanguagePage(getDriver());
    }

    private WebElement findRowByLocale(final String localeId) {
        for (WebElement row : getRows()) {
            if (getShortLocale(row).equals(localeId)) {
                return row;
            }
        }
        throw new RuntimeException("Did not find locale " + localeId);
    }

    private String getShortLocale(WebElement row) {
        String locale = getListEntryLocale(row);
        return locale.substring(0, locale.indexOf('[')).trim();
    }

    private List<WebElement> getRows() {
        return readyElement(existingElement(By.id("languageForm")),
                By.className("list--stats"))
                .findElements(By.tagName("li"));
    }

    private String getListEntryLocale(WebElement listElement) {
        return listElement.findElement(By.className("list__item__meta")).getText().trim();
    }

    public List<String> getLanguageLocales() {
        log.info("Query list of languages");
        List<String> names = new ArrayList<>();
        for (WebElement listItem : getRows()) {
            names.add(getShortLocale(listItem));
        }
        return names;
    }

    public LanguagesPage clickMoreActions() {
        log.info("Click More Actions dropdown");
        readyElement(moreActions).click();
        return new LanguagesPage(getDriver());
    }

    public AddLanguagePage addNewLanguage() {
        log.info("Click Add New Language");
        readyElement(addLanguageLink).click();
        return new AddLanguagePage(getDriver());
    }

    public LanguagePage gotoLanguagePage(String localeId) {
        log.info("Click language {}", localeId);
        findRowByLocale(localeId).click();
        return new LanguagePage(getDriver());
    }

    public boolean languageIsEnabledByDefault(String localeId) {
        log.info("Query is language enabled by default {}", localeId);
        // Search for enabledByDefaultLabel label
        for (WebElement label: findRowByLocale(localeId).findElements(enabledByDefaultLabel)) {
            if (label.getText().trim().equals("Default")) {
                return true;
            }
        }
        return false;
    }

}
