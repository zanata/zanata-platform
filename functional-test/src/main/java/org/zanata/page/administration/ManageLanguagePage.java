/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ManageLanguagePage extends BasePage {

    public static final int LOCALE_COLUMN = 0;
    public static final int ENABLED_COLUMN = 3;

    private By languageTable = By.id("languageForm");
    private By addLanguageButton = By.linkText("Add New Language");
    private By moreActions = By.id("more-actions");
    private By enableByDefault = By.linkText("Enable by default");
    private By disableByDefault = By.linkText("Disable by default");
    private By disabledIcon = By.className("i--cancel");

    public ManageLanguagePage(WebDriver driver) {
        super(driver);
    }

    public ManageLanguagePage clickMoreActions() {
        log.info("Click More Actions dropdown");
        waitForWebElement(moreActions).click();
        return new ManageLanguagePage(getDriver());
    }

    public AddLanguagePage addNewLanguage() {
        log.info("Click Add New Language");
        waitForWebElement(addLanguageButton).click();
        return new AddLanguagePage(getDriver());
    }

    public ManageLanguageTeamMemberPage manageTeamMembersFor(
            final String localeId) {
        log.info("Click team {}", localeId);
        findRowByLocale(localeId).click();
        return new ManageLanguageTeamMemberPage(getDriver());
    }

    public ManageLanguagePage clickOptions(String localeId) {
        findRowByLocale(localeId).findElement(By.className("dropdown__toggle"))
                .click();
        return new ManageLanguagePage(getDriver());
    }

    public ManageLanguagePage enableLanguageByDefault(String localeId) {
        log.info("Click to enable {}", localeId);
        findRowByLocale(localeId).findElement(enableByDefault).click();
        switchToAlert().accept();
        return new ManageLanguagePage(getDriver());
    }

    public boolean languageIsEnabled(String localeId) {
        log.info("Query is language enabled {}", localeId);
        // Search for visibility of the disabled icon
        for (WebElement langDisabled : findRowByLocale(localeId)
                .findElements(disabledIcon)) {
            if (langDisabled.isDisplayed()) {
                return false;
            }
        }
        return true;
    }

    public List<String> getLanguageLocales() {
        log.info("Query list of languages");
        return getNames();
    }

    private String getShortLocale(WebElement row) {
        String locale = getListEntryLocale(row);
        return locale.substring(0, locale.indexOf('[')).trim();
    }

    private WebElement findRowByLocale(final String localeId) {
        for (WebElement row : getRows()) {
            if (getShortLocale(row).equals(localeId)) {
                return row;
            }
        }
        throw new RuntimeException("Did not find locale " + localeId);
    }

    private List<WebElement> getRows() {
        return waitForWebElement(waitForElementExists(By.id("languageForm")),
                By.className("list--stats"))
                .findElements(By.className("list__item--actionable"));
    }

    private List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (WebElement listItem : getRows()) {
            names.add(getShortLocale(listItem));
        }
        return names;
    }

    private String getListEntryName(WebElement listElement) {
        String listTitle = listElement.findElement(By.className("list__title"))
                .getText();
        return listTitle.substring(0, listTitle.lastIndexOf(getListEntryUsers(listElement))).trim();
    }

    private String getListEntryLocale(WebElement listElement) {
        return listElement.findElement(By.className("list__item__meta")).getText().trim();
    }

    private String getListEntryUsers(WebElement listElement) {
        return listElement.findElement(By.className("list__title"))
                .findElement(By.className("txt--meta")).getText().trim();
     }
}
