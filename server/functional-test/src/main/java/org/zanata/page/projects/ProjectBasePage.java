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
package org.zanata.page.projects;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.projects.projectsettings.ProjectAboutTab;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab;
import org.zanata.page.projects.projectsettings.ProjectTranslationTab;
import org.zanata.page.projects.projectsettings.ProjectWebHooksTab;

public class ProjectBasePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectBasePage.class);
    private By versionsTab = By.id("versions_tab");
    private By peopleTab = By.id("people_tab");
    private By aboutTab = By.id("about_tab");
    private By settingsTab = By.id("settings_tab");
    private By versionsTabBody = By.id("versions");
    private By peopleTabBody = By.id("people");
    private By aboutTabBody = By.id("about");
    private By settingsTabBody = By.id("settings");
    private By settingsGeneralTab = By.id("settings-general_tab");
    private By settingsPermissionTab = By.id("settings-permissions_tab");
    private By settingsTranslationTab = By.id("settings-translation_tab");
    private By settingsLanguagesTab = By.id("settings-languages_tab");
    private By settingsAboutTab = By.id("settings-about_tab");
    private By projectInfo = By.id("project-info");
    private By settingsWebHooksTab = By.id("settings-webhooks_tab");

    public ProjectBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getProjectName() {
        log.info("Query Project name");
        return readyElement(projectInfo).findElement(By.tagName("h1"))
                .getText();
    }

    public ProjectVersionsPage gotoVersionsTab() {
        log.info("Click Versions tab");
        existingElement(versionsTabBody);
        clickWhenTabEnabled(readyElement(versionsTab));
        readyElement(By.id("versions"));
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectPeoplePage gotoPeopleTab() {
        log.info("Click People tab");
        existingElement(peopleTabBody);
        clickWhenTabEnabled(readyElement(peopleTab));
        readyElement(peopleTabBody);
        return new ProjectPeoplePage(getDriver());
    }

    public ProjectAboutPage gotoAboutTab() {
        log.info("Click About tab");
        existingElement(aboutTabBody);
        clickWhenTabEnabled(readyElement(aboutTab));
        readyElement(By.id("about"));
        return new ProjectAboutPage(getDriver());
    }

    public boolean settingsTabIsDisplayed() {
        log.info("Query Settings tab is displayed");
        return existingElement(settingsTab).isDisplayed();
    }

    public ProjectBasePage gotoSettingsTab() {
        log.info("Click Settings tab");
        existingElement(settingsTabBody);
        clickWhenTabEnabled(readyElement(settingsTab));
        readyElement(settingsTab);
        return new ProjectBasePage(getDriver());
    }

    public ProjectGeneralTab gotoSettingsGeneral() {
        log.info("Click General settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsGeneralTab));
        readyElement(By.id("settings-general"));
        return new ProjectGeneralTab(getDriver());
    }

    public ProjectPermissionsTab gotoSettingsPermissionsTab() {
        log.info("Click Permissions settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsPermissionTab));
        readyElement(By.id("settings-permissions"));
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectTranslationTab gotoSettingsTranslationTab() {
        log.info("Click Translation settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsTranslationTab));
        readyElement(By.id("settings-translation"));
        return new ProjectTranslationTab(getDriver());
    }

    public ProjectLanguagesTab gotoSettingsLanguagesTab() {
        log.info("Click Languages settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsLanguagesTab));
        readyElement(By.id("settings-languages"));
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectWebHooksTab gotoSettingsWebHooksTab() {
        log.info("Click WebHooks settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsWebHooksTab));
        readyElement(By.id("settings-webhooks"));
        return new ProjectWebHooksTab(getDriver());
    }

    public ProjectAboutTab gotoSettingsAboutTab() {
        log.info("Click About settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsAboutTab));
        readyElement(By.id("settings-about"));
        return new ProjectAboutTab(getDriver());
    }

    public List<String> getContentAreaParagraphs() {
        log.info("Query Project info");
        List<String> paragraphTexts = new ArrayList<String>();
        List<WebElement> paragraphs =
                readyElement(projectInfo).findElements(By.tagName("p"));
        for (WebElement element : paragraphs) {
            paragraphTexts.add(element.getText());
        }
        return paragraphTexts;
    }

    public String getHomepage() {
        log.info("Query Project homepage");
        for (WebElement element : readyElement(projectInfo)
                .findElements(By.tagName("li"))) {
            if (element.findElement(By.className("list__title")).getText()
                    .trim().equals("Home Page:")) {
                return element.findElement(By.tagName("a")).getText();
            }
        }
        return "";
    }

    public String getGitUrl() {
        log.info("Query Project repo");
        for (WebElement element : readyElement(projectInfo)
                .findElements(By.tagName("li"))) {
            if (element.findElement(By.className("list__title")).getText()
                    .trim().equals("Repository:")) {
                return element.findElement(By.tagName("input"))
                        .getAttribute("value");
            }
        }
        return "";
    }
}
