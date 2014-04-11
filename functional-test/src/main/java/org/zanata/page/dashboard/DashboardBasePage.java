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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.projects.CreateProjectPage;

import com.google.common.base.Predicate;

public class DashboardBasePage extends BasePage {

    @FindBy(id = "activity")
    private WebElement activityTab;

    @FindBy(id = "projects")
    private WebElement projectsTab;

    @FindBy(id = "settings")
    private WebElement settingsTab;

    @FindBy(id = "activity-today")
    private WebElement todaysActivityTab;

    @FindBy(id = "activity-week")
    private WebElement thisWeeksActivityTab;

    @FindBy(id = "activity-month")
    private WebElement thisMonthsActivityTab;

    public DashboardBasePage(final WebDriver driver) {
        super(driver);
    }

    public DashboardActivityTab gotoActivityTab() {
        clickWhenTabEnabled(activityTab);
        return new DashboardActivityTab(getDriver());
    }

    public boolean activityTabIsSelected() {
        return getDriver().findElement(
                By.cssSelector("#activity.is-active")) != null;
    }

    private void clickWhenTabEnabled(final WebElement tab) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                boolean clicked = false;
                if (tab.isDisplayed() && tab.isEnabled()) {
                    tab.click();
                    clicked = true;
                }
                return clicked;
            }
        });
    }

    public WebElement getActivityTab() {
        return activityTab;
    }

    public WebElement getProjectsTab() {
        return projectsTab;
    }

    public WebElement getSettingsTab() {
        return settingsTab;
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

    public DashboardSettingsTab gotoSettingsTab() {
        getSettingsTab().click();
        return new DashboardSettingsTab(getDriver());
    }


    public CreateProjectPage clickCreateYourOwn() {
        getDriver().findElement(By.linkText("create your own")).click();
        return new CreateProjectPage(getDriver());
    }

    public DashboardBasePage clickOnActivityTab() {
        getActivityTab().click();
        return this;
    }
}
