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
package org.zanata.page.dashboard;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardClientTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardProfileTab;

public class DashboardBasePage extends BasePage {

    @FindBy(id = "activity")
    private WebElement activityTab;

    @FindBy(id = "projects")
    private WebElement projectsTab;

    @FindBy(id = "settings")
    private WebElement settingsTab;

    @FindBy(id = "account-tab")
    private WebElement settingsAccountTab;

    @FindBy(id = "profile-tab")
    private WebElement settingsProfileTab;

    @FindBy(id = "client-tab")
    private WebElement settingsClientTab;

    @FindBy(id = "activity-today")
    private WebElement todaysActivityTab;

    @FindBy(id = "activity-week")
    private WebElement thisWeeksActivityTab;

    @FindBy(id = "activity-month")
    private WebElement thisMonthsActivityTab;

    public DashboardBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getUserFullName() {
        return getDriver().findElement(By.id("profile-overview"))
                .findElement(By.tagName("h1")).getText();
    }

    public DashboardActivityTab gotoActivityTab() {
        clickWhenTabEnabled(activityTab);
        return new DashboardActivityTab(getDriver());
    }

    public boolean activityTabIsSelected() {
        return getDriver().findElement(
                By.cssSelector("#activity.is-active")) != null;
    }

    public List<WebElement> getMyActivityList() {
        WebElement listWrapper =
                getDriver().findElement(By.id("activity-list"));

        if (listWrapper != null) {
            return listWrapper.findElements(By.xpath("./li"));
        }
        return new ArrayList<WebElement>();
    }

    public DashboardProjectsTab gotoProjectsTab() {
        projectsTab.click();
        return new DashboardProjectsTab(getDriver());
    }

    public DashboardBasePage goToSettingsTab() {
        clickWhenTabEnabled(settingsTab);
        return new DashboardBasePage(getDriver());
    }

    public DashboardAccountTab gotoSettingsAccountTab() {
        clickWhenTabEnabled(settingsAccountTab);
        return new DashboardAccountTab(getDriver());
    }

    public DashboardProfileTab goToSettingsProfileTab() {
        clickWhenTabEnabled(settingsProfileTab);
        return new DashboardProfileTab(getDriver());
    }

    public DashboardClientTab goToSettingsClientTab() {
        clickWhenTabEnabled(settingsClientTab);
        return new DashboardClientTab(getDriver());
    }

    public void waitForLoaderFinished() {
        slightPause();
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return !isLoaderVisible();
            }
        });
    }

    public boolean isLoaderVisible() {
        return getDriver().findElement(By.className("loader__spinner"))
                .isDisplayed();
    }
}
