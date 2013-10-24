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

import org.hamcrest.Matchers;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.DetailedTest;
import org.zanata.page.projects.CreateVersionPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Damian Jansen <a
 *      href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(DetailedTest.class)
public class ProjectVersionTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule =
            new ResetDatabaseRule(ResetDatabaseRule.Config.WithData);

    private String formatError = "must start and end with letter or number, "+
            "and contain only letters, numbers, underscores and hyphens.";

    @Test
    public void idFieldMustNotBeEmpty() {
        CreateVersionPage createVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("");

        assertThat("The empty value is rejected",
                createVersionPage.getErrors(),
                Matchers.hasItem("value is required"));
    }

    @Test
    public void idStartsAndEndsWithAlphanumeric() {
        CreateVersionPage createVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("-A");

        assertThat("The input is rejected",
                createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage = createVersionPage
                .inputVersionId("B-")
                .waitForNumErrors(1);


        assertThat("The input is rejected",
                createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage = createVersionPage
                .inputVersionId("_C_")
                .waitForNumErrors(1);

        assertThat("The input is rejected",
                createVersionPage.getErrors(),
                Matchers.hasItem(formatError));

        createVersionPage = createVersionPage
                .inputVersionId("A-B_C")
                .waitForNumErrors(0);

        assertThat("The input is acceptable",
                createVersionPage.getErrors(),
                Matchers.not(Matchers.hasItem(formatError)));
    }

    @Test
    public void overrideLanguages() {
        CreateVersionPage createVersionPage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToProjects()
                .goToProject("about fedora")
                .clickCreateVersionLink()
                .inputVersionId("overridetest")
                .showLocalesOverride();

        assertThat("The enabled list contains three languages",
                createVersionPage.getEnabledLanguages(),
                Matchers.contains("French [fr] français",
                        "Hindi [hi] हिन्दी",
                        "Polish [pl] polski"));

        assertThat("The disabled list contains one language",
                createVersionPage.getDisabledLanguages(),
                Matchers.contains("English (United States) [en-US] English "+
                        "(United States)"));

        createVersionPage = createVersionPage
                .selectEnabledLanguage("Polish [pl] polski")
                .clickRemoveLanguage()
                .waitForListCount(2, 2);

        assertThat("The disabled list contains two languages",
                createVersionPage.getDisabledLanguages(),
                Matchers.contains("English (United States) [en-US] English "+
                        "(United States)", "Polish [pl] polski"));

        createVersionPage = createVersionPage
                .selectDisabledLanguage("English (United States) [en-US] "+
                        "English (United States)")
                .clickAddLanguage()
                .waitForListCount(3, 1);

        assertThat("The disabled list contains one language",
                createVersionPage.getDisabledLanguages(),
                Matchers.contains("Polish [pl] polski"));

        assertThat("The enabled list contains three languages",
                createVersionPage.getEnabledLanguages(),
                Matchers.contains(
                        "English (United States) [en-US] English "+
                        "(United States)",
                        "French [fr] français",
                        "Hindi [hi] हिन्दी"));

        ProjectVersionPage projectVersionPage = createVersionPage.saveVersion();

        assertThat("Three languages are available to translate",
                projectVersionPage.getTranslatableLanguages(),
                Matchers.contains("français",
                        "हिन्दी", "English (United States)"));
    }
}
