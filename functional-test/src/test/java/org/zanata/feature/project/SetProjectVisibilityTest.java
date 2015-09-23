package org.zanata.feature.project;

import org.junit.Test;
import org.zanata.feature.Feature;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.projects.ProjectsPage;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SetProjectVisibilityTest extends ZanataTestCase {

    @Feature(summary = "The administrator can delete a project",
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
                .enterProjectNameToConfirmDelete("about fedora")
                .confirmDeleteProject()
                .goToProjects();

        assertThat(projectsPage.getProjectNamesOnCurrentPage())
                .doesNotContain("about fedora")
                .as("The project is not displayed");
    }

}
