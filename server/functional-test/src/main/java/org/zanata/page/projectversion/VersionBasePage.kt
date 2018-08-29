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
package org.zanata.page.projectversion

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.page.projects.ProjectVersionsPage
import org.zanata.page.projectversion.versionsettings.VersionDocumentsTab
import org.zanata.page.projectversion.versionsettings.VersionGeneralTab
import org.zanata.page.projectversion.versionsettings.VersionLanguagesTab
import org.zanata.page.projectversion.versionsettings.VersionTranslationTab

open class VersionBasePage(driver: WebDriver) : BasePage(driver) {
    private val settingsGeneralTab = By.id("settings-general_tab")
    private val settingsLanguagesTab = By.id("settings-languages_tab")
    private val settingsDocumentsTab = By.id("settings-documents_tab")
    private val settingsTranslationTab = By.id("settings-translation_tab")
    private val documentsTab = By.id("documents_tab")
    private val languageTab = By.id("languages_tab")
    private val settingsTab = By.id("settings_tab")
    private val documentsTabBody = By.id("documents")
    private val languageTabBody = By.id("languages")
    private val settingsTabBody = By.id("settings")
    private val versionInfo = By.id("version-info")
    private val versionPage = By.id("version-page")

    val projectVersionName: String
        get() {
            log.info("Query Version name")
            return readyElement(versionInfo).findElement(By.tagName("h1"))
                    .text
        }

    fun clickProjectLink(projectName: String): ProjectVersionsPage {
        log.info("Click Project link")
        readyElement(versionPage).findElement(By.linkText(projectName)).click()
        return ProjectVersionsPage(driver)
    }

    fun gotoDocumentTab(): VersionDocumentsPage {
        log.info("Click Documents tab")
        existingElement(documentsTabBody)
        clickWhenTabEnabled(readyElement(documentsTab))
        readyElement(By.id("documents"))
        return VersionDocumentsPage(driver)
    }

    fun gotoLanguageTab(): VersionLanguagesPage {
        log.info("Click Languages tab")
        existingElement(languageTabBody)
        clickWhenTabEnabled(readyElement(languageTab))
        readyElement(By.id("languages"))
        return VersionLanguagesPage(driver)
    }

    fun gotoSettingsTab(): VersionBasePage {
        log.info("Click Settings tab")
        slightPause()
        existingElement(settingsTabBody)
        clickWhenTabEnabled(readyElement(settingsTab))
        readyElement(settingsTabBody)
        return VersionBasePage(driver)
    }

    fun gotoSettingsGeneral(): VersionGeneralTab {
        log.info("Click General settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsGeneralTab))
        readyElement(By.id("settings-general_form"))
        return VersionGeneralTab(driver)
    }

    fun gotoSettingsLanguagesTab(): VersionLanguagesTab {
        log.info("Click Languages settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsLanguagesTab))
        readyElement(By.id("settings-languages-form"))
        return VersionLanguagesTab(driver)
    }

    fun gotoSettingsDocumentsTab(): VersionDocumentsTab {
        log.info("Click Documents settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsDocumentsTab))
        readyElement(By.id("settings-document_form"))
        return VersionDocumentsTab(driver)
    }

    fun gotoSettingsTranslationTab(): VersionTranslationTab {
        log.info("Click Translation settings sub-tab")
        clickWhenTabEnabled(readyElement(settingsTranslationTab))
        readyElement(By.id("settings-translation-validation-form"))
        return VersionTranslationTab(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(VersionBasePage::class.java)
    }
}
