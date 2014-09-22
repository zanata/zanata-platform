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
package org.zanata.page.projectversion;

import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.page.projectversion.versionsettings.VersionGeneralTab;
import org.zanata.page.projectversion.versionsettings.VersionLanguagesTab;
import org.zanata.page.projectversion.versionsettings.VersionTranslationTab;

@Slf4j
public class VersionBasePage extends BasePage {

    @FindBy(id = "settings_tab")
    private WebElement settingsTab;

    @FindBy(id = "settings-general_tab")
    private WebElement settingsGeneralTab;

    @FindBy(id = "settings-languages_tab")
    private WebElement settingsLanguagesTab;

    @FindBy(id = "settings-documents_tab")
    private WebElement settingsDocumentsTab;

    @FindBy(id = "settings-translation_tab")
    private WebElement settingsTranslationTab;

    @FindBy(id = "documents")
    private WebElement documentsTab;

    @FindBy(id = "languages")
    private WebElement languageTab;

    public VersionBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getProjectVersionName() {
        log.info("Query Version name");
        return getDriver()
                .findElement(By.id("version-info"))
                .findElement(By.tagName("h1")).getText();
    }

    public ProjectVersionsPage clickProjectLink(String projectName) {
        log.info("Click Project link");
        getDriver().findElement(By.id("version-page"))
                .findElement(By.linkText(projectName))
                .click();
        return new ProjectVersionsPage(getDriver());
    }

    public boolean settingsTabIsDisplayed() {
        return settingsTab.isDisplayed();
    }

    public void reloadUntilSettingsIsDisplayed() {
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsTab.isDisplayed();
            }
        });
    }

    public VersionDocumentsPage gotoDocumentTab() {
        log.info("Click Documents tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("documents_tab")));
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("documents"))
                        .isDisplayed();
            }
        });
        return new VersionDocumentsPage(getDriver());
    }

    public VersionLanguagesPage gotoLanguageTab() {
        log.info("Click Languages tab");
        clickWhenTabEnabled(getDriver().findElement(By.id("languages_tab")));
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("languages"))
                        .isDisplayed();
            }
        });
        return new VersionLanguagesPage(getDriver());
    }

    public VersionBasePage gotoSettingsTab() {
        log.info("Click Settings tab");
        slightPause();
        clickWhenTabEnabled(getDriver().findElement(By.id("settings_tab")));
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("settings"))
                        .isDisplayed();
            }
        });
        return new VersionBasePage(getDriver());
    }

    public VersionGeneralTab gotoSettingsGeneral() {
        log.info("Click General settings sub-tab");
        clickWhenTabEnabled(settingsGeneralTab);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("settings-general_form"))
                        .isDisplayed();
            }
        });
        return new VersionGeneralTab(getDriver());
    }

    public VersionLanguagesTab gotoSettingsLanguagesTab() {
        log.info("Click Languages settings sub-tab");
        clickWhenTabEnabled(settingsLanguagesTab);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("settings-languages-form"))
                        .isDisplayed();
            }
        });
        return new VersionLanguagesTab(getDriver());
    }

    public VersionDocumentsTab gotoSettingsDocumentsTab() {
        log.info("Click Documents settings sub-tab");
        clickWhenTabEnabled(settingsDocumentsTab);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("settings-document_form"))
                        .isDisplayed();
            }
        });
        return new VersionDocumentsTab(getDriver());
    }

    public VersionTranslationTab gotoSettingsTranslationTab() {
        log.info("Click Translation settings sub-tab");
        clickWhenTabEnabled(settingsTranslationTab);
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("settings-translation-review-form"))
                        .isDisplayed();
            }
        });
        return new VersionTranslationTab(getDriver());
    }

}
