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
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectPage extends BasePage {

    @FindBy(id = "versions_tab")
    private WebElement versionsTab;

    @FindBy(id = "settings_tab")
    private WebElement settingsTab;

    @FindBy(id = "settings-general_tab")
    private WebElement settingsGeneralTab;

    @FindBy(id = "settings-permissions_tab")
    private WebElement settingsPermissionTab;

    @FindBy(id = "versions-more-actions")
    private WebElement versionTabMoreAction;

    @FindBy(id = "new-version-link")
    private WebElement createNewVersion;

    public ProjectPage(final WebDriver driver) {
        super(driver);
    }

    @SuppressWarnings("unused")
    public String getProjectId() {
        return getLastBreadCrumbText();
    }

    public String getProjectName() {
        return getTitle().replaceAll("Projects - ", "");
    }

    public ProjectPage gotoVersionsTab() {
        versionsTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return versionsTab.isDisplayed();
            }
        });
        return new ProjectPage(getDriver());
    }

    public ProjectPage gotoSettingsTab() {
        settingsTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsTab.isDisplayed();
            }
        });
        return new ProjectPage(getDriver());
    }

    public CreateVersionPage clickCreateVersionLink() {
        gotoVersionsTab();

        clickLinkAfterAnimation(versionTabMoreAction);
        clickLinkAfterAnimation(createNewVersion);
        return new CreateVersionPage(getDriver());
    }

    public ProjectVersionPage gotoVersion(final String versionId) {
        return refreshPageUntil(this,
                new Function<WebDriver, ProjectVersionPage>() {
                    @Override
                    public ProjectVersionPage apply(WebDriver input) {
                        WebElement versionForm =
                                getDriver().findElement(By.id("versions_form"));

                        List<WebElement> versionLinks =
                                versionForm.findElement(
                                        By.className("list--stats"))
                                        .findElements(By.tagName("li"));

                        log.info("found {} active versions",
                                versionLinks.size());

                        Optional<WebElement> found =
                                Iterables.tryFind(versionLinks,
                                        new Predicate<WebElement>() {
                                            @Override
                                            public boolean apply(
                                                    WebElement input) {
                                                return input
                                                        .findElement(
                                                                By.tagName("a"))
                                                        .getText()
                                                        .contains(versionId);
                                            }
                                        });

                        Preconditions.checkState(found.isPresent(), versionId
                                + " not found");
                        found.get().findElement(By.tagName("a")).click();
                        return new ProjectVersionPage(getDriver());
                    };
                });
    }

    public List<String> getVersions() {
        return WebElementUtil.elementsToText(getDriver(),
                By.className("list__title"));
    }

    public CreateProjectPage gotoSettingsGeneral() {
        settingsGeneralTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsGeneralTab.isDisplayed();
            }
        });
        return new CreateProjectPage(getDriver());
    }

    public ProjectPage gotoSettingsMaintainersTab() {
        settingsPermissionTab.click();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return settingsPermissionTab.isDisplayed();
            }
        });
        return new ProjectPage(getDriver());
    }

    public ProjectPage enterSearchMaintainer(String maintainerQuery) {
        WebElementUtil.searchAutocomplete(getDriver(),
                "maintainerAutocomplete", maintainerQuery);
        return new ProjectPage(getDriver());
    }

    public ProjectPage addMaintainer(final String username) {
        waitForTenSec().until(new Function<WebDriver, List<WebElement>>() {
            @Override
            public List<WebElement> apply(WebDriver driver) {
                return WebElementUtil.getSearchAutocompleteResults(driver,
                        "settings-permissions-form", "maintainerAutocomplete");
            }
        });

        List<WebElement> searchResults =
                WebElementUtil.getSearchAutocompleteResults(getDriver(),
                        "settings-permissions-form", "maintainerAutocomplete");

        boolean clickedUser = false;
        for (WebElement searchResult : searchResults) {
            if (searchResult.getText().contains(username)) {
                searchResult.click();
                clickedUser = true;
                break;
            }
        }
        Preconditions.checkState(clickedUser, "can not find username - %s",
                username);

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getSettingsMaintainersList().contains(username);
            }
        });
        return new ProjectPage(getDriver());
    }

    private String getUsername(WebElement maintainersLi) {
        return maintainersLi
                .findElement(By.xpath(".//span[@class='txt--meta']")).getText()
                .replace("@", "");
    }

    public ProjectPage removeMaintainer(final String username) {
        boolean removedMaintainer = false;
        for (WebElement maintainersLi : getSettingsMaintainersElement()) {
            String displayedUsername = getUsername(maintainersLi);

            if (displayedUsername.equals(username)) {
                maintainersLi.findElement(By.tagName("a")).click();
                removedMaintainer = true;
                break;
            }
        }
        Preconditions.checkState(removedMaintainer,
                "can not remove maintainer: %s", username);

        refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return !getSettingsMaintainersList().contains(username);
            }
        });

        return new ProjectPage(getDriver());
    }

    public List<WebElement> getSettingsMaintainersElement() {
        return getDriver().findElement(By.id("settings-permissions-form"))
                .findElements(By.xpath(".//ul/li[@class='reveal--list-item']"));
    }

    public List<String> getSettingsMaintainersList() {
        List<WebElement> items = getSettingsMaintainersElement();

        List<String> rows =
                Lists.transform(items, new Function<WebElement, String>() {
                    @Override
                    public String apply(WebElement li) {
                        return getUsername(li);
                    }
                });

        return ImmutableList.copyOf(rows);
    }

    public List<String> getContentAreaParagraphs() {
        List<String> paragraphTexts = new ArrayList<String>();
        List<WebElement> paragraphs =
                getDriver().findElement(By.className("content_box"))
                        .findElements(By.tagName("p"));
        for (WebElement element : paragraphs) {
            paragraphTexts.add(element.getText());
        }
        return paragraphTexts;
    }
}
