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

package org.zanata.feature.project

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.zanata.util.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.workflow.BasicWorkFlow
import org.zanata.workflow.LoginWorkFlow
import org.zanata.workflow.ProjectWorkFlow
import org.assertj.core.api.Assertions.assertThat
import org.zanata.workflow.ProjectWorkFlow.projectDefaults

/**
 * @author Damian Jansen
 * [djansen@redhat.com](mailto:djansen@redhat.com)
 */
@DetailedTest
class CreateProjectTest : ZanataTestCase() {

    @BeforeEach
    fun before() {
        BasicWorkFlow().goToHome().deleteCookiesAndRefresh()
        assertThat(LoginWorkFlow().signIn("admin", "admin").loggedInAs())
                .describedAs("Admin is logged in")
                .isEqualTo("admin")
    }

    @Trace(summary = "The user can create a project")
    @Test
    fun createABasicProject() {
        val projectVersionsPage = ProjectWorkFlow()
                .createNewSimpleProject("basicproject", "basicproject")

        assertThat(projectVersionsPage.projectName.trim { it <= ' ' })
                .describedAs("The project name is correct")
                .isEqualTo("basicproject")
    }

    @Trace(summary = "The user can create a project with description")
    @Test
    fun createABasicProjectWithDescription() {
        val projectSettings = projectDefaults()
        projectSettings["Project ID"] = "descriptionproject"
        projectSettings["Name"] = "Project With Description Test"
        projectSettings["Description"] = "Project Description!"

        val projectPage = ProjectWorkFlow().createNewProject(projectSettings)

        assertThat(projectPage.projectName.trim { it <= ' ' })
                .describedAs("The project name is correct")
                .isEqualTo(projectSettings["Name"])

        assertThat(projectPage.contentAreaParagraphs)
                .describedAs("The project content area shows the description")
                .contains(projectSettings["Description"])
    }

}
