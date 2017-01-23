/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.search;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.page.projects.CreateProjectPage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Function;

public class SearchPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SearchPage.class);
    private By searchProjectForm = By.id("search-project_form");
    private By projectTabSearchPage = By.id("projects_tab");
    private By userTabSearchPage = By.id("users_tab");

    public SearchPage(final WebDriver driver) {
        super(driver);
    }

    public List<String> getProjectNamesOnSearchPage() {
        log.info("Query Projects list");
        waitForPageSilence();
        gotoProjectTabInSearchPage();
        log.info("Query project name");
        List<String> names = new ArrayList<>();
        for (WebElement row : readyElement(searchProjectForm)
                .findElements(By.xpath("//h3[@class=\'list__title\']"))) {
            names.add(row.getText());
        }
        return names;
    }

    public void gotoProjectTabInSearchPage() {
        clickElement(projectTabSearchPage);
    }

    public void gotoUserTabInSearchPage() {
        clickElement(userTabSearchPage);
    }
}
