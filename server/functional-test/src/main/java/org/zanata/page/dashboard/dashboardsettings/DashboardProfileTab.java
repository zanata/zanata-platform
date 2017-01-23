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
package org.zanata.page.dashboard.dashboardsettings;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.dashboard.DashboardBasePage;

/**
 * @author Damian Jansen
 *         <a href="mailto:camunoz@redhat.com">djansen@redhat.com</a>
 */
public class DashboardProfileTab extends DashboardBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardProfileTab.class);
    private By accountNameField =
            By.id("profileForm:nameField:input:accountName");
    private By updateProfileButton = By.id("updateProfileButton");

    public DashboardProfileTab(WebDriver driver) {
        super(driver);
    }

    public String getUsername() {
        log.info("Query user name");
        return readyElement(By.id("profileForm"))
                .findElement(By.className("l--push-bottom-0")).getText();
    }

    public DashboardProfileTab enterName(String name) {
        log.info("Enter name {}", name);
        readyElement(accountNameField).clear();
        enterText(readyElement(accountNameField), name);
        return new DashboardProfileTab(getDriver());
    }

    public DashboardProfileTab clickUpdateProfileButton() {
        log.info("Click Update");
        clickElement(updateProfileButton);
        return new DashboardProfileTab(getDriver());
    }
}
