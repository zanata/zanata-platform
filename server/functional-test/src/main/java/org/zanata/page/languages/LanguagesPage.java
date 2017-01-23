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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.administration.AddLanguagePage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class LanguagesPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(LanguagesPage.class);
    private By addLanguageButton = By.id("btn-language-add-new");
    private By defaultLabel = By.className("label-default");

    public LanguagesPage(WebDriver driver) {
        super(driver);
    }
    // return list of tr

    private WebElement findRowByLocale(final String localeId) {
        for (WebElement row : getRows()) {
            if (getShortLocale(row).equals(localeId)) {
                return row;
            }
        }
        throw new RuntimeException("Did not find locale " + localeId);
    }

    private String getShortLocale(WebElement row) {
        WebElement span = row.findElement(By.name("language-name"));
        return span.getText().substring(0, span.getText().indexOf('[')).trim();
    }

    private List<WebElement> getRows() {
        return readyElement(existingElement(By.id("languages-form")),
                By.id("languages-table"))
                        .findElements(By.name("language-entry"));
    }

    public List<String> getLanguageLocales() {
        log.info("Query list of languages");
        return getRows().stream().map(this::getShortLocale)
                .collect(Collectors.toList());
    }

    public AddLanguagePage clickAddNewLanguage() {
        log.info("Click add new language button");
        clickElement(addLanguageButton);
        return new AddLanguagePage(getDriver());
    }

    public LanguagePage gotoLanguagePage(String localeId) {
        log.info("Click language {}", localeId);
        WebElement element = findRowByLocale(localeId)
                .findElement(By.id("language-name-" + localeId));
        if (element != null) {
            element.click();
            return new LanguagePage(getDriver());
        }
        throw new RuntimeException("Did not find locale " + localeId);
    }

    public boolean languageIsEnabledByDefault(String localeId) {
        log.info("Query is language enabled by default {}", localeId);
        // Search for enabledByDefaultLabel label
        return !findRowByLocale(localeId).findElements(defaultLabel).isEmpty();
    }
}
