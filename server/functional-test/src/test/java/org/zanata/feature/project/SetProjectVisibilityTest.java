package org.zanata.feature.project;

import org.junit.Test;
import org.zanata.feature.Trace;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.explore.ExplorePage;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class SetProjectVisibilityTest extends ZanataTestCase {

    @Trace(summary = "The administrator can delete a project")
    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void deleteAProject() throws Exception {
        ExplorePage explore = new LoginWorkFlow()
                .signIn("admin", "admin")
                .gotoExplore()
                .searchAndGotoProjectByName("about fedora")
                .gotoSettingsTab()
                .gotoSettingsGeneral()
                .deleteProject()
                .enterProjectNameToConfirmDelete("about fedora")
                .confirmDeleteProject()
                .gotoExplore();

        assertThat(explore.getProjectSearchResults())
                .doesNotContain("about fedora")
                .as("The project is not displayed");
    }

}
