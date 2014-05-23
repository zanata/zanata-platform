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
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.util.WebElementUtil;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectPermissionsTab extends ProjectBasePage {

    public ProjectPermissionsTab(WebDriver driver) {
        super(driver);
    }

    public ProjectPermissionsTab enterSearchMaintainer(
            String maintainerQuery) {
        WebElementUtil.searchAutocomplete(getDriver(),
                "maintainerAutocomplete", maintainerQuery);
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectPermissionsTab selectSearchMaintainer(String maintainer) {
        List<WebElement> searchResults = waitForTenSec()
                .until(new Function<WebDriver, List<WebElement>>() {
                    @Override
                    public List<WebElement> apply(WebDriver driver) {
                        return WebElementUtil.getSearchAutocompleteResults(
                                driver,
                                "settings-permissions-form",
                                "maintainerAutocomplete");
                    }
                });

        boolean clickedUser = false;
        for (WebElement searchResult : searchResults) {
            if (searchResult.getText().contains(maintainer)) {
                searchResult.click();
                clickedUser = true;
                break;
            }
        }
        Preconditions.checkState(clickedUser, "Can not find username - %s",
                maintainer);

        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectPermissionsTab clickRemoveOn(String maintainer) {
        getMaintainerElementFromList(maintainer).click();
        return new ProjectPermissionsTab(getDriver());
    }

    public ProjectBasePage clickRemoveOnSelf(String maintainer) {
        getMaintainerElementFromList(maintainer).click();
        return new ProjectBasePage(getDriver());
    }

    private String getUsername(WebElement maintainersLi) {
        return maintainersLi
                .findElement(By.xpath(".//span[@class='txt--meta']")).getText()
                .replace("@", "");
    }

    private WebElement getMaintainerElementFromList(final String maintainer) {
        return waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver input) {
                for (WebElement maintainersLi : getSettingsMaintainersElement()) {
                    String displayedUsername = getUsername(maintainersLi);
                    if (displayedUsername.equals(maintainer)) {
                        return maintainersLi.findElement(By.tagName("a"));
                    }
                }
                return null;
            }
        });
    }

    public ProjectPermissionsTab waitForMaintainersContains(
            final String username) {
        return refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return getSettingsMaintainersList().contains(username);
            }
        });
    }

    public ProjectPermissionsTab waitForMaintainersNotContains(
            final String username) {
        return refreshPageUntil(this, new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver driver) {
                return !getSettingsMaintainersList().contains(username);
            }
        });
    }

    public List<WebElement> getSettingsMaintainersElement() {
        return getDriver()
                .findElement(By.id("settings-permissions-form"))
                .findElement(By.id("maintainers-list"))
                .findElements(By.tagName("li"));
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
}
