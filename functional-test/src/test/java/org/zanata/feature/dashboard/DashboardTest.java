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

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.ZanataRestCaller.buildSourceResource;
import static org.zanata.util.ZanataRestCaller.buildTextFlow;

import lombok.extern.slf4j.Slf4j;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.dashboard.DashboardActivityTab;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.dashboard.DashboardProjectsTab;
import org.zanata.page.projects.CreateProjectPage;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.HasEmailRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;

@Category(DetailedTest.class)
@Slf4j
public class DashboardTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();
    @ClassRule
    public static HasEmailRule emailRule = new HasEmailRule();

    private DashboardBasePage dashboard;

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
        dashboard = new LoginWorkFlow().signIn("admin", "admin");
    }

    @Test
    @Category(BasicAcceptanceTest.class)
    public void dashboardBasicTests() throws Exception {
        assertThat(dashboardPresentAfterLogin())
                .as("Dashboard is present").isTrue();
        assertThat(activityListExpands())
                .as("Activity list is present and expandable").isTrue();
        assertThat(projectListIsNotEmpty())
                .as("Project List is present and expandable").isTrue();
    }

    private boolean dashboardPresentAfterLogin() throws Exception {
        return dashboard.activityTabIsSelected();
    }

    private boolean activityListExpands() throws Exception {
        DashboardActivityTab activityTab = dashboard.gotoActivityTab();
        assertThat(activityTab.getMyActivityList()).isNotEmpty();
        int initialActivitySize = activityTab.getMyActivityList().size();
        activityTab.clickMoreActivity();
        return activityTab.getMyActivityList().size() > initialActivitySize;
    }

    private boolean projectListIsNotEmpty() throws Exception {
        DashboardProjectsTab projectsTab = dashboard.gotoProjectsTab();
        return projectsTab.getMaintainedProjectList().size() > 0;
    }

    @Test
    public void accountEmailModification() throws Exception {
        final String successMessage =
                "You will soon receive an email with a link to activate your email account change.";
        dashboard.goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeNewAccountEmailAddress("new@fakeemail.com")
                .clickUpdateEmailButton();
        assertThat(dashboard.expectNotification(successMessage));
    }

    @Test
    public void passwordChange() throws Exception {
        final String passwordChanged =
                "Your password has been successfully changed.";
        dashboard.goToSettingsTab()
                .gotoSettingsAccountTab()
                .typeOldPassword("admin")
                .typeNewPassword("admin2")
                .clickUpdatePasswordButton();
        assertThat(dashboard.expectNotification(passwordChanged));
    }

    @Test
    public void createProject() throws Exception {
        CreateProjectPage createProjectPage =
            dashboard.gotoProjectsTab().clickOnCreateProjectLink();
        assertThat(createProjectPage.getTitle())
                .isEqualTo("Projects - New Project");
    }
}
