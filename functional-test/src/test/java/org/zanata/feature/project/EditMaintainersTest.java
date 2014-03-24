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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeTrue;

import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.ProjectPage;
import org.zanata.util.NoScreenshot;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
@NoScreenshot
public class EditMaintainersTest {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test
    public void maintainerDetailsAreDisplayed() {
        ProjectPage projectPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoSettingsTab()
                        .gotoSettingsMaintainersTab();

        assertThat("The admin user is shown in the list",
                projectPage.getSettingsMaintainersList(),
                Matchers.hasItem("admin"));
    }

    @Test
    public void addMaintainer() {
        ProjectPage projectPage =
                new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                        .goToProject("about fedora").gotoSettingsTab()
                        .gotoSettingsMaintainersTab();

        assertThat("The translator user is not a maintainer",
                projectPage.getSettingsMaintainersList(),
                Matchers.not(Matchers.hasItem("translator")));

        projectPage = addMaintainer("translator");

        assertThat("The translator user is a maintainer",
                projectPage.getSettingsMaintainersList(),
                Matchers.hasItem("translator"));
    }

    @Test
    public void removeMaintainer() {
        // Precondition
        ProjectPage projectPage = addMaintainer("translator");

        assertThat("The translator user is a maintainer",
                projectPage.getSettingsMaintainersList(),
                Matchers.hasItem("translator"));

        projectPage = projectPage.removeMaintainer("translator");

        assertThat("The translator user is no longer a maintainer",
                projectPage.getSettingsMaintainersList(),
                Matchers.not(Matchers.hasItem("translator")));
    }

    private ProjectPage addMaintainer(String username) {
        return new LoginWorkFlow().signIn("admin", "admin").goToProjects()
                .goToProject("about fedora").gotoSettingsTab()
                .gotoSettingsMaintainersTab().enterSearchMaintainer(username)
                .addMaintainer(username);

    }
}
