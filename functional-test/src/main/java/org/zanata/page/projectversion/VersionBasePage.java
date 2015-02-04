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
import org.zanata.page.BasePage;

import lombok.extern.slf4j.Slf4j;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab;
import org.zanata.page.projectversion.versionsettings.VersionGeneralTab;
import org.zanata.page.projectversion.versionsettings.VersionLanguagesTab;
import org.zanata.page.projectversion.versionsettings.VersionTranslationTab;

@Slf4j
public class VersionBasePage extends BasePage {

    private By settingsGeneralTab = By.id("settings-general_tab");
    private By settingsLanguagesTab = By.id("settings-languages_tab");
    private By settingsDocumentsTab = By.id("settings-documents_tab");
    private By settingsTranslationTab = By.id("settings-translation_tab");
    private By documentsTab = By.id("documents_tab");
    private By languageTab = By.id("languages_tab");
    private By settingsTab = By.id("settings_tab");

    private By documentsTabBody = By.id("documents");
    private By languageTabBody = By.id("languages");
    private By settingsTabBody = By.id("settings");

    private By versionInfo = By.id("version-info");
    private By versionPage = By.id("version-page");

    public VersionBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getProjectVersionName() {
        log.info("Query Version name");
        return waitForWebElement(versionInfo)
                .findElement(By.tagName("h1")).getText();
    }

    public ProjectVersionsPage clickProjectLink(String projectName) {
        log.info("Click Project link");
        waitForWebElement(versionPage)
                .findElement(By.linkText(projectName))
                .click();
        return new ProjectVersionsPage(getDriver());
    }

    public VersionDocumentsPage gotoDocumentTab() {
        log.info("Click Documents tab");
        waitForElementExists(documentsTabBody);
        clickWhenTabEnabled(waitForWebElement(documentsTab));
        waitForWebElement(By.id("documents"));
        return new VersionDocumentsPage(getDriver());
    }

    public VersionLanguagesPage gotoLanguageTab() {
        log.info("Click Languages tab");
        waitForElementExists(languageTabBody);
        clickWhenTabEnabled(waitForWebElement(languageTab));
        waitForWebElement(By.id("languages"));
        return new VersionLanguagesPage(getDriver());
    }

    public VersionBasePage gotoSettingsTab() {
        log.info("Click Settings tab");
        slightPause();
        waitForElementExists(settingsTabBody);
        clickWhenTabEnabled(waitForWebElement(settingsTab));
        waitForWebElement(settingsTabBody);
        return new VersionBasePage(getDriver());
    }

    public VersionGeneralTab gotoSettingsGeneral() {
        log.info("Click General settings sub-tab");
        clickWhenTabEnabled(waitForWebElement(settingsGeneralTab));
        waitForWebElement(By.id("settings-general_form"));
        return new VersionGeneralTab(getDriver());
    }

    public VersionLanguagesTab gotoSettingsLanguagesTab() {
        log.info("Click Languages settings sub-tab");
        clickWhenTabEnabled(waitForWebElement(settingsLanguagesTab));
        waitForWebElement(By.id("settings-languages-form"));
        return new VersionLanguagesTab(getDriver());
    }

    public VersionDocumentsTab gotoSettingsDocumentsTab() {
        log.info("Click Documents settings sub-tab");
        clickWhenTabEnabled(waitForWebElement(settingsDocumentsTab));
        waitForWebElement(By.id("settings-document_form"));
        return new VersionDocumentsTab(getDriver());
    }

    public VersionTranslationTab gotoSettingsTranslationTab() {
        log.info("Click Translation settings sub-tab");
        clickWhenTabEnabled(waitForWebElement(settingsTranslationTab));
        waitForWebElement(By.id("settings-translation-review-form"));
        return new VersionTranslationTab(getDriver());
    }

}
