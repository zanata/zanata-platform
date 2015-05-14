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

    @Feature(summary = "The administrator can set a project to archived",
            tcmsTestPlanIds = 5316, tcmsTestCaseIds = 135846)
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void deleteAProject() throws Exception {
        ProjectsPage projectsPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .deleteProject()
                .confirmDeleteProject()
                .goToProjects();

        assertThat(projectsPage.getProjectNamesOnCurrentPage())
                .doesNotContain("about fedora")
                .as("The project is not displayed");

        projectsPage = projectsPage.setActiveFilterEnabled(false)
                .setReadOnlyFilterEnabled(false);

        projectsPage.expectProjectVisible("about fedora");

        assertThat(projectsPage.getProjectNamesOnCurrentPage())
                .contains("about fedora")
                .as("The project is now displayed");

        projectsPage.logout();

        assertThat(new LoginWorkFlow()
                .signIn("translator", "translator")
                .goToProjects()
                .getProjectNamesOnCurrentPage())
                .doesNotContain("about fedora")
                .as("User cannot navigate to the archived project");
    }

}
