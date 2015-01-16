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
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class LanguagesPage extends BasePage {

    private By languagesList = By.id("languageForm");

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
        return waitForWebElement(waitForElementExists(By.id("languageForm")),
                By.className("list--stats"))
                .findElements(By.tagName("li"));
    }

    private String getListEntryLocale(WebElement listElement) {
        return listElement.findElement(By.className("list__item__meta")).getText().trim();
    }

}
