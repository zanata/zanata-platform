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

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.page.dashboard.dashboardsettings.DashboardAccountTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardClientTab;
import org.zanata.page.dashboard.dashboardsettings.DashboardProfileTab;
import static org.assertj.core.api.Assertions.assertThat;

public class DashboardBasePage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardBasePage.class);
    private By activityTab = By.id("activity_tab");
    private By projectsTab = By.id("projects_tab");
    private By groupsTab = By.id("groups_tab");
    private By settingsTab = By.id("settings_tab");
    private By activityTabBody = By.id("activity");
    private By projectsTabBody = By.id("projects");
    private By groupsTabBody = By.id("groups");
    private By settingsTabBody = By.id("settings");
    private By settingsAccountTab = By.id("account_tab");
    private By settingsProfileTab = By.id("profile_tab");
    private By settingsClientTab = By.id("client_tab");
    private By todaysActivityTab = By.id("activity-today_tab");
    private By thisWeeksActivityTab = By.id("activity-week_tab");
    private By thisMonthsActivityTab = By.id("activity-month_tab");
    private By profileOverview = By.id("profile-overview");
    public static final String EMAIL_SENT =
            "You will soon receive an email with a link to activate your email account change.";
    public static final String PASSWORD_UPDATE_SUCCESS =
            "Your password has been successfully changed.";

    public DashboardBasePage(final WebDriver driver) {
        super(driver);
    }

    public String getUserFullName() {
        log.info("Query user full name");
        return readyElement(profileOverview).findElement(By.tagName("h1"))
                .getText();
    }

    public DashboardActivityTab gotoActivityTab() {
        log.info("Click Activity tab");
        existingElement(activityTabBody);
        clickWhenTabEnabled(readyElement(activityTab));
        return new DashboardActivityTab(getDriver());
    }

    public boolean activityTabIsSelected() {
        log.info("Query is Activity tab displayed");
        return getDriver().findElements(By.cssSelector("#activity.is-active"))
                .size() > 0;
    }

    public DashboardProjectsTab gotoProjectsTab() {
        log.info("Click Projects tab");
        existingElement(projectsTabBody);
        clickWhenTabEnabled(readyElement(projectsTab));
        return new DashboardProjectsTab(getDriver());
    }

    public DashboardGroupsTab gotoGroupsTab() {
        log.info("Click Projects tab");
        existingElement(groupsTabBody);
        clickWhenTabEnabled(readyElement(groupsTab));
        return new DashboardGroupsTab(getDriver());
    }

    public DashboardBasePage goToSettingsTab() {
        log.info("Click Settings tab");
        existingElement(settingsTabBody);
        clickWhenTabEnabled(readyElement(settingsTab));
        return new DashboardBasePage(getDriver());
    }

    public DashboardAccountTab gotoSettingsAccountTab() {
        log.info("Click Account settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsAccountTab));
        return new DashboardAccountTab(getDriver());
    }

    public DashboardProfileTab goToSettingsProfileTab() {
        log.info("Click Profile settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsProfileTab));
        return new DashboardProfileTab(getDriver());
    }

    public DashboardClientTab goToSettingsClientTab() {
        log.info("Click Client settings sub-tab");
        clickWhenTabEnabled(readyElement(settingsClientTab));
        return new DashboardClientTab(getDriver());
    }

    public void expectUsernameChanged(final String current) {
        log.info("Wait for username change from {}", current);
        waitForPageSilence();
        assertThat(getUserFullName()).isNotEqualTo(current);
    }
}
