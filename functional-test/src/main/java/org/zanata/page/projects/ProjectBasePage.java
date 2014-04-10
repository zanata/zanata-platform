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
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;
import org.zanata.page.projects.projectsettings.*;

@Slf4j
public class ProjectBasePage extends BasePage {

    @FindBy(id = "versions")
    private WebElement versionsTab;

    @FindBy(id = "maintainers")
    private WebElement maintainersTab;

    @FindBy(id = "about")
    private WebElement aboutTab;

    @FindBy(id = "settings")
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
        return getDriver()
                .findElement(By.id("project-info"))
                .findElement(By.tagName("h1")).getText();
    }

    public ProjectVersionsPage gotoVersionsTab() {
        clickWhenTabEnabled(versionsTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("versions_content"))
                        .isDisplayed();
            }
        });
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectMaintainersPage gotoMaintainersTab() {
        clickWhenTabEnabled(maintainersTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("maintainers_content"))
                        .isDisplayed();
            }
        });
        return new ProjectMaintainersPage(getDriver());
    }

    public ProjectAboutPage gotoAboutTab() {
        clickWhenTabEnabled(aboutTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("about_content"))
                        .isDisplayed();
            }
        });
        return new ProjectAboutPage(getDriver());
    }
    public boolean settingsTabIsDisplayed() {
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
        clickWhenTabEnabled(settingsTab);
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver().findElement(By.id("settings_content"))
                        .isDisplayed();
            }
        });
        return new ProjectBasePage(getDriver());
    }

    public ProjectGeneralTab gotoSettingsGeneral() {
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
