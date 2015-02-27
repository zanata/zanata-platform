package org.zanata.feature.project;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.page.projects.projectsettings.ProjectGeneralTab;
import org.zanata.util.AddUsersRule;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SetProjectVisibilityTest extends ZanataTestCase {

    @ClassRule
    public static AddUsersRule addUsersRule = new AddUsersRule();

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Feature(summary = "The administrator can set a project to obsolete",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 135846)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setAProjectObsolete() throws Exception {
        ProjectsPage projectsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .goToProjects();

        assertThat(projectsPage.getProjectNamesOnCurrentPage())
                .doesNotContain("about fedora")
                .as("The project is not displayed");

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(false)
                .setObsoleteFilterEnabled(true);

        projectsPage.waitForProjectVisibility("about fedora", true);

        assertThat(projectsPage.getProjectNamesOnCurrentPage())
                .contains("about fedora")
                .as("The project is now displayed");

        projectsPage.logout();

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .getProjectNamesOnCurrentPage())
                .doesNotContain("about fedora")
                .as("User cannot navigate to the obsolete project");
    }

    @Feature(summary = "The administrator can set an obsolete project " +
            "to active",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 0)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void setAnObsoleteProjectAsActive() throws Exception {
        ProjectGeneralTab projectGeneralTab = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .archiveProject()
                .goToProjects()
                .setObsoleteFilterEnabled(true)
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .unarchiveProject();

        assertThat(projectGeneralTab.isArchiveButtonAvailable())
                .isTrue()
                .as("The archive button is now available");

        projectGeneralTab.logout();

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .goToProject("about fedora")
                .getProjectName())
                .isEqualTo("about fedora")
                .as("Translator can view the project");
    }
}
