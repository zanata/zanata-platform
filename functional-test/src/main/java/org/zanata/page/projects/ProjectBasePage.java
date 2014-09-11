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
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.projects.projectsettings.ProjectAboutTab;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.page.projects.projectsettings.ProjectLanguagesTab;
import org.zanata.page.projects.projectsettings.ProjectPermissionsTab;
import org.zanata.page.projects.projectsettings.ProjectTranslationTab;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectBasePage extends BasePage {

    @FindBy(id = "versions_tab")
    private WebElement versionsTab;

    @FindBy(id = "maintainers_tab")
    private WebElement maintainersTab;

    @FindBy(id = "about_tab")
    private WebElement aboutTab;

    @FindBy(id = "settings_tab")
    private WebElement settingsTab;

    @FindBy(id = "settings-general_tab")
    private WebElement settingsGeneralTab;

    @FindBy(id = "settings-permissions_tab")
    private WebElement settingsPermissionTab;

    @FindBy(id = "settings-translation_tab")
    private WebElement settingsTranslationTab;

    @FindBy(id = "settings-languages_tab")
    private WebElement settingsLanguagesTab;

    @FindBy(id = "settings-about_tab")
    private WebElement settingsAboutTab;

    public ProjectBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getProjectName() {
        log.info("Query Project name");
        return getDriver()
                .findElement(By.id("project-info"))
                .findElement(By.tagName("h1")).getText();
    }

    public ProjectVersionsPage gotoVersionsTab() {
        log.info("Click Versions tab");
        clickWhenTabEnabled(versionsTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("versions"))
                        .isDisplayed();
            }
        });
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectMaintainersPage gotoMaintainersTab() {
        log.info("Click Maintainers tab");
        clickWhenTabEnabled(maintainersTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("maintainers"))
                        .isDisplayed();
            }
        });
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectAboutPage gotoAboutTab() {
        log.info("Click About tab");
        clickWhenTabEnabled(aboutTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("about"))
                        .isDisplayed();
            }
        });
        return new ProjectAboutPage(getDriver());
    }

    public boolean settingsTabIsDisplayed() {
        log.info("Query Settings tab is displayed");
        return settingsTab.isDisplayed();
    }

    public void reloadUntilSettingsIsDisplayed() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsTab.isDisplayed();
            }
        });
    }

    public ProjectBasePage gotoSettingsTab() {
        log.info("Click Settings tab");
        slightPause();
        clickWhenTabEnabled(settingsTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings_tab"))
                        .isDisplayed();
            }
        });
        return new ProjectBasePage(getDriver());
    }

    public ProjectGeneralTab gotoSettingsGeneral() {
        log.info("Click General settings sub-tab");
        clickWhenTabEnabled(settingsGeneralTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings-general"))
                        .isDisplayed();
            }
        });
        return new ProjectGeneralTab(getDriver());
    }

    public ProjectPermissionsTab gotoSettingsPermissionsTab() {
        log.info("Click Permissions settings sub-tab");
        clickWhenTabEnabled(settingsPermissionTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings-permissions"))
                        .isDisplayed();
            }
        });
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectTranslationTab gotoSettingsTranslationTab() {
        log.info("Click Translation settings sub-tab");
        clickWhenTabEnabled(settingsTranslationTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings-translation"))
                        .isDisplayed();
            }
        });
        return new ProjectTranslationTab(getDriver());
    }

    public ProjectLanguagesTab gotoSettingsLanguagesTab() {
        log.info("Click Languages settings sub-tab");
        clickWhenTabEnabled(settingsLanguagesTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings-languages"))
                        .isDisplayed();
            }
        });
        return new ProjectLanguagesTab(getDriver());
    }

    public ProjectAboutTab gotoSettingsAboutTab() {
        log.info("Click About settings sub-tab");
        clickWhenTabEnabled(settingsAboutTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings-about"))
                        .isDisplayed();
            }
        });
        return new ProjectAboutTab(getDriver());
    }

    public List<String> getContentAreaParagraphs() {
        log.info("Query Project info");
        List<String> paragraphTexts = new ArrayList<String>();
        List<WebElement> paragraphs =
                getDriver().findElement(By.id("project-info"))
                        .findElements(By.tagName("p"));
        for (WebElement element : paragraphs) {
            paragraphTexts.add(element.getText());
        }
        return paragraphTexts;
    }

    public String getHomepage() {
        log.info("Query Project homepage");
        for (WebElement element : getDriver()
                .findElement(By.id("project-info"))
                .findElements(By.tagName("li"))) {
            if (element.findElement(By.className("list__title"))
                    .getText().trim()
                    .equals("Home Page:")) {
                return element.findElement(By.tagName("a")).getText();
            }
        }
        return "";
    }

    public String getGitUrl() {
        log.info("Query Project repo");
        for (WebElement element : getDriver()
                .findElement(By.id("project-info"))
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
