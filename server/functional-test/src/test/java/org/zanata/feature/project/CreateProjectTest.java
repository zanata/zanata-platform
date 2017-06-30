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

package org.zanata.feature.project;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.workflow.BasicWorkFlow;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.workflow.ProjectWorkFlow.projectDefaults;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectTest extends ZanataTestCase {

    @Before
    public void before() {
        new BasicWorkFlow().goToHome().deleteCookiesAndRefresh();
        assertThat(new LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .isEqualTo("admin")
                .as("Admin is logged in");
    }

    @Trace(summary = "The user can create a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createABasicProject() throws Exception {
        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .createNewSimpleProject("basicproject", "basicproject");

        assertThat(projectVersionsPage.getProjectName().trim())
                .isEqualTo("basicproject")
                .as("The project name is correct");
    }

    @Trace(summary = "The user can create a project with description")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createABasicProjectWithDescription() throws Exception {
        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "descriptionproject");
        projectSettings.put("Name", "Project With Description Test");
        projectSettings.put("Description", "Project Description!");

        ProjectBasePage projectPage =
                new ProjectWorkFlow().createNewProject(projectSettings);

        assertThat(projectPage.getProjectName().trim())
                .isEqualTo(projectSettings.get("Name"))
                .as("The project name is correct");

        assertThat(projectPage.getContentAreaParagraphs())
                .contains(projectSettings.get("Description"))
                .as("The project content area shows the description");
    }

}
