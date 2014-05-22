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

import java.util.HashMap;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.BasicAcceptanceTest;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.zanata.workflow.ProjectWorkFlow.projectDefaults;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class CreateProjectTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    @Category(BasicAcceptanceTest.class)
    public void createABasicProject() {

        assertThat("User logs in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                equalTo("admin"));

        ProjectVersionsPage projectVersionsPage = new ProjectWorkFlow()
                .createNewSimpleProject("basicproject", "basicproject");

        assertThat("The project name is correct",
                projectVersionsPage.getProjectName().trim(),
                equalTo("basicproject"));
    }

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void createABasicProjectWithHomepageContent() {

        HashMap<String, String> projectSettings = projectDefaults();
        projectSettings.put("Project ID", "homepageproject");
        projectSettings.put("Name", "Project With Homepage Test");
        projectSettings.put("Description", "Project Description!");

        assertThat("Admin can log in",
                new LoginWorkFlow().signIn("admin", "admin").loggedInAs(),
                equalTo("admin"));

        ProjectBasePage projectPage =
                new ProjectWorkFlow().createNewProject(projectSettings);

        assertThat("The project name is correct",
                projectPage.getProjectName().trim(),
                equalTo(projectSettings.get("Name")));

        List<String> paragraphs = projectPage.getContentAreaParagraphs();

        assertThat("The project content area shows the description",
                paragraphs,
                hasItem(projectSettings.get("Description")));

    }

}
