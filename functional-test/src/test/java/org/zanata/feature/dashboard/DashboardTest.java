/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.dashboard;

import lombok.extern.slf4j.Slf4j;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.utility.DashboardPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;

import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class,
        CustomResourceExtension.class })
@Category(ConcordionTest.class)
@Slf4j
public class DashboardTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    private DashboardPage dashboardPage;

    @Before
    public void setUp() {
        ZanataRestCaller restCaller = new ZanataRestCaller();
        Resource resource =
                buildSourceResource("a", buildTextFlow("res1", "content"));
        // create 6 activities
        for (int i = 0; i < 6; i++) {
            String projectSlug = "activity" + i;
            String iterationSlug = "v" + i;
            restCaller.createProjectAndVersion(projectSlug, iterationSlug,
                    "gettext");
            restCaller.postSourceDocResource(projectSlug, iterationSlug,
                    resource, false);
        }
    }

    public boolean signInAs(String username, String password) {
        dashboardPage = new LoginWorkFlow().signIn(username, password);

        return dashboardPage.hasLoggedIn();
    }

    public boolean hasMyActivitiesSection() {
        return dashboardPage.containActivityListSection();
    }

    public boolean hasMaintainedProjectsSection() {
        return dashboardPage.containMyMaintainedProjectsSection();
    }

    public void gotoDashboard() {
        dashboardPage = new BasicWorkFlow().goToDashboard();
    }

    public boolean myActivitiesListNotEmpty() {
        return !dashboardPage.getMyActivityList().isEmpty();
    }

    public int myActivitiesCount() {
        return dashboardPage.getMyActivityList().size();
    }

    public boolean myActivitiesCountIsMoreThan(int compareTo) {
        return dashboardPage.getMyActivityList().size() > compareTo;
    }

    public boolean maintainedProjectNotEmpty() {
        return !dashboardPage.getMyMaintainedProject().isEmpty();
    }

    public void clickMoreActivity() {
        dashboardPage.clickMoreActivity();
    }
}
