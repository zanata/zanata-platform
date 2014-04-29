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
package org.zanata.feature.versionGroup;

import java.util.ArrayList;
import java.util.List;

import org.concordion.api.extension.Extensions;
import org.concordion.ext.ScreenshotExtension;
import org.concordion.ext.TimestampFormatterExtension;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebElement;
import org.zanata.concordion.CustomResourceExtension;
import org.zanata.feature.ConcordionTest;
import org.zanata.page.dashboard.DashboardBasePage;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.projects.ProjectBasePage;
import org.zanata.page.projects.ProjectVersionsPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RunWith(ConcordionRunner.class)
@Extensions({ ScreenshotExtension.class, TimestampFormatterExtension.class,
        CustomResourceExtension.class })
@Category(ConcordionTest.class)
@Ignore("at the moment direct input project version to add without auto-complete will not work")
public class VersionGroupBasicTest {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    private final ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
    private DashboardBasePage dashboardPage;
    private VersionGroupPage versionGroupPage;

    @Before
    public void before() {
        dashboardPage = new LoginWorkFlow().signIn("admin", "admin");
    }

    public VersionGroupsPage createNewVersionGroup(String groupId,
            String groupName, String groupDesc) {
        VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
        return versionGroupsPage.createNewGroup().inputGroupId(groupId)
                .inputGroupName(groupName).inputGroupDescription(groupDesc)
                .saveGroup();
    }

    public void clickProjectsTab() {
        versionGroupPage.clickOnTab("projects_tab");
    }

    public void clickSettingsTab() {
        versionGroupPage.clickOnTab("settings_tab");
    }

    public void clickSettingsProjectsTab() {
        versionGroupPage.clickOnTab("settings-projects_tab");
    }

    public CreateVersionGroupPage groupIDAlreadyExists(String groupId,
            String groupName, String groupDesc) {
        VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
        List<String> groupNames = versionGroupsPage.getGroupNames();
        assertThat("Group does not exist, preconditions not met",
                groupNames.contains(groupName));
        return versionGroupsPage.createNewGroup().inputGroupId(groupId)
                .inputGroupName(groupName).inputGroupDescription(groupDesc)
                .saveGroupFailure();
    }

    public CreateVersionGroupPage invalidCharacters(String groupId) {
        VersionGroupsPage versionGroupsPage = dashboardPage.goToGroups();
        return versionGroupsPage.createNewGroup().inputGroupId(groupId)
                .inputGroupName("");
    }

    public boolean groupNamesContain(VersionGroupsPage versionGroupsPage,
            String groupName) {
        return versionGroupsPage.getGroupNames().contains(groupName);
    }

    /**
     * This assumes there will be ONE error on screen.
     *
     * @param createVersionGroupPage
     *            page
     * @return the error
     */
    public String getFieldsValidationError(
            CreateVersionGroupPage createVersionGroupPage) {
        return createVersionGroupPage.getFieldValidationErrors().get(0);
    }

    public VersionGroupsPage
            toggleObsolete(VersionGroupsPage versionGroupsPage) {
        return versionGroupsPage.toggleObsolete(true);
    }

    public VersionGroupsPage groups() {
        VersionGroupsPage versionGroupsPage =
                projectWorkFlow.goToHome().goToGroups();
        return versionGroupsPage;
    }

    public void createProjectAndVersion(String projectId, String projectName,
            String version) {
        ProjectVersionsPage projectVersionsPage =
                projectWorkFlow.createNewSimpleProject(projectId, projectName);
        projectVersionsPage.clickCreateVersionLink().inputVersionId(version)
                .saveVersion();
    }

    public List<String> searchProjectToAddToVersionGroup(String searchTerm) {
        List<WebElement> elements =
                versionGroupPage.searchProject(searchTerm, 2);

        List<String> result = new ArrayList<String>();

        for (WebElement element : elements) {
            result.add(element.getText());
        }
        return result;
    }

    public VersionGroupPage addProjectToVersionGroup(int row) {
        return versionGroupPage.addToGroup(row - 1);
    }

    public void clickGroupName(VersionGroupsPage groupsPage, String groupName) {
        versionGroupPage = groupsPage.goToGroup(groupName);
    }

    public ProjectBasePage clickProjectLinkOnRow(int row) {
        return versionGroupPage.clickOnProjectLinkOnRow(row);
    }

    public VersionLanguagesPage clickVersionLinkOnRow(int row) {
        return versionGroupPage.clickOnProjectVersionLinkOnRow(row);
    }

    public String getProjectNameFromPage(ProjectBasePage projectPage) {
        return projectPage.getProjectName();
    }

    public boolean checkIfEquals(ProjectBasePage projectPage, String projectName) {
        return projectPage.getProjectName().trim().equals(projectName.trim());
    }

    public boolean checkIfEquals(VersionLanguagesPage versionPage,
            String versionId) {
        return versionPage.getProjectVersionName().trim().equals(versionId.trim());
    }
}
