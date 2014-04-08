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
import org.zanata.feature.DetailedTest;
import org.zanata.page.dashboard.DashboardActivityTab;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.dashboard.DashboardProjectsTab;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.util.HasEmailRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.util.ZanataRestCaller;
import org.zanata.workflow.LoginWorkFlow;

@Category(DetailedTest.class)
@Slf4j
public class DashboardTest {

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
    public void dashboardBasicTests() throws Exception {
        dashboardPresentAfterLogin();
        activityListExpands();
        projectListIsPresent();
    }

    public void dashboardPresentAfterLogin() throws Exception {
        assertThat(dashboard.activityTabIsSelected()).isTrue();
    }

    public void activityListExpands() throws Exception {
        DashboardActivityTab activityTab = dashboard.gotoActivityTab();
        assertThat(activityTab.getMyActivityList()).isNotEmpty();
        int initialActivitySize = activityTab.getMyActivityList().size();
        activityTab.clickMoreActivity();
        assertThat(activityTab.getMyActivityList().size())
                .isGreaterThan(initialActivitySize);
    }

    public void projectListIsPresent() throws Exception {
        DashboardProjectsTab projectsTab = dashboard.gotoProjectsTab();
        assertThat(projectsTab.getMaintainedProjectList()).isNotEmpty();
    }

    @Test
    public void accountEmailModification() throws Exception {
        dashboard.gotoSettingsTab()
                 .typeNewAccountEmailAddress("new@fakeemail.com")
                 .clickUpdateEmailButton();
        assertThat(dashboard.getNotificationMessage()).startsWith(
                "You will soon receive an email");
    }

    @Test
    public void passwordChange() throws Exception {
        dashboard.gotoSettingsTab()
                 .typeOldPassword("admin")
                 .typeNewPassword("admin2")
                 .clickUpdatePasswordButton();
        assertThat(dashboard.getNotificationMessage()).isEqualTo(
                "Your password has been successfully changed.");
    }

    public boolean signInAs(String username, String password) {
        dashboard = new LoginWorkFlow().signIn(username, password);

        return dashboard.hasLoggedIn();
    }
}
