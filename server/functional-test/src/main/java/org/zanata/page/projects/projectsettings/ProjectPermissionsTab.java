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
package org.zanata.page.projects.projectsettings;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.util.WebElementUtil;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectPermissionsTab extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectPermissionsTab.class);
    private By maintainersForm = By.id("settings-permissions-form");

    public ProjectPermissionsTab(WebDriver driver) {
        super(driver);
    }

    public ProjectPermissionsTab enterSearchMaintainer(String maintainerQuery) {
        log.info("Enter user search {}", maintainerQuery);
        WebElementUtil.searchAutocomplete(getDriver(), "maintainerAutocomplete",
                maintainerQuery);
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectPermissionsTab
            selectSearchMaintainer(final String maintainer) {
        log.info("Select user {}", maintainer);
        waitForAMoment().until((Predicate<WebDriver>) driver -> {
            List<WebElement> searchResults =
                    WebElementUtil.getSearchAutocompleteResults(driver,
                            "settings-permissions-form",
                            "maintainerAutocomplete");
            boolean clickedUser = false;
            for (WebElement searchResult : searchResults) {
                if (searchResult.getText().contains(maintainer)) {
                    searchResult.click();
                    clickedUser = true;
                    break;
                }
            }
            return clickedUser;
        });
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectPermissionsTab clickRemoveOn(String maintainer) {
        log.info("Click Remove on {}", maintainer);
        getMaintainerElementFromList(maintainer).click();
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectBasePage clickRemoveOnSelf(String maintainer) {
        log.info("Click Remove on (self) {}", maintainer);
        getMaintainerElementFromList(maintainer).click();
        return new ProjectBasePage(getDriver());
    }

    private String getUsername(WebElement maintainersLi) {
        return maintainersLi.findElement(By.className("txt--meta")).getText()
                .replace("@", "");
    }

    private WebElement getMaintainerElementFromList(final String maintainer) {
        return waitForAMoment()
                .until((Function<WebDriver, WebElement>) webDriver -> {
                    for (WebElement maintainersLi : getSettingsMaintainersElement()) {
                        String displayedUsername = getUsername(maintainersLi);
                        if (displayedUsername.equals(maintainer)) {
                            return maintainersLi.findElement(By.tagName("a"));
                        }
                    }
                    return null;
                });
    }

    public ProjectPermissionsTab
            expectMaintainersContains(final String username) {
        log.info("Wait for maintainers contains {}", username);
        waitForPageSilence();
        assertThat(getSettingsMaintainersList()).contains(username);
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectPermissionsTab
            expectMaintainersNotContains(final String username) {
        log.info("Wait for maintainers does not contain {}", username);
        waitForPageSilence();
        assertThat(getSettingsMaintainersList()).doesNotContain(username);
        return new ProjectPermissionsTab(getDriver());
    }

    private List<WebElement> getSettingsMaintainersElement() {
        return readyElement(maintainersForm)
                .findElement(By.id("maintainers-list"))
                .findElements(By.className("reveal--list-item"));
    }

    public List<String> getSettingsMaintainersList() {
        log.info("Query maintainers list");
        List<WebElement> items = getSettingsMaintainersElement();
        List<String> rows = Lists.transform(items, this::getUsername);
        return ImmutableList.copyOf(rows);
    }
}
