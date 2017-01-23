/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.util.WebElementUtil;
import java.util.List;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DashboardGroupsTab extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardGroupsTab.class);
    public static final int GROUP_NAME_COLUMN = 0;
    private By groupTable = By.id("groupForm:groupTable");
    private By createGroupButton = By.id("create-group-link");

    public DashboardGroupsTab(WebDriver driver) {
        super(driver);
    }

    public CreateVersionGroupPage createNewGroup() {
        log.info("Click New Group button");
        clickElement(createGroupButton);
        return new CreateVersionGroupPage(getDriver());
    }

    public List<String> getGroupNames() {
        return WebElementUtil.getColumnContents(getDriver(), groupTable,
                GROUP_NAME_COLUMN);
    }

    public VersionGroupPage goToGroup(String groupName) {
        log.info("Click group {}", groupName);
        clickElement(
                readyElement(groupTable).findElement(By.linkText(groupName)));
        return new VersionGroupPage(getDriver());
    }
}
