/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.feature.testharness.TestPlan.DetailedTest;
import org.zanata.page.projects.ProjectAboutPage;
import org.zanata.page.projects.projectsettings.ProjectAboutTab;
import org.zanata.util.SampleProjectRule;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isEmptyOrNullString;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class EditProjectAboutTest extends ZanataTestCase {

    @Rule
    public SampleProjectRule sampleProjectRule = new SampleProjectRule();

    @Test(timeout = ZanataTestCase.MAX_SHORT_TEST_DURATION)
    public void addAboutPageDetails() {

        String aboutText = "This is my about text for AF";
        assertThat("Admin is logged in", new LoginWorkFlow()
                .signIn("admin", "admin")
                .loggedInAs(),
                equalTo("admin"));

        ProjectAboutTab projectAboutTab = new ProjectWorkFlow()
                .createNewSimpleProject("aboutpagetest", "aboutpagetest")
                .gotoSettingsTab()
                .gotoSettingsAboutTab();

        assertThat("The text is empty",
                projectAboutTab.getAboutText(),
                isEmptyOrNullString());

        projectAboutTab = projectAboutTab.clearAboutText()
                .enterAboutText(aboutText)
                .pressSave();

        projectAboutTab.expectNotification("Successfully updated");
        ProjectAboutPage projectAboutPage = projectAboutTab.gotoAboutTab();

        assertThat("The text in the About tab is correct",
                projectAboutPage.getAboutText(),
                equalTo(aboutText));
    }
}
