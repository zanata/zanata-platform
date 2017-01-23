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
package org.zanata.page.dashboard;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.CreateProjectPage;
import java.util.List;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class DashboardProjectsTab extends DashboardBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(DashboardProjectsTab.class);
    private By maintainedProjectsList = By.id("maintainedProjects");
    private By createProjectLink = By.id("create-project-link");

    public DashboardProjectsTab(WebDriver driver) {
        super(driver);
    }

    public List<WebElement> getMaintainedProjectList() {
        log.info("Query maintained projects list");
        return readyElement(maintainedProjectsList)
                .findElement(By.tagName("ul")).findElements(By.xpath("./li"));
    }

    public CreateProjectPage clickOnCreateProjectLink() {
        log.info("Click Create Project");
        clickElement(createProjectLink);
        return new CreateProjectPage(getDriver());
    }
}
