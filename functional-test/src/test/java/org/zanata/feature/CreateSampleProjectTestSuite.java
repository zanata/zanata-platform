/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature;

import org.hamcrest.Matchers;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.workflow.ProjectWorkFlow;

import lombok.extern.slf4j.Slf4j;
import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@RunWith(Suite.class)
@Suite.SuiteClasses({
      CreateNewProjectTest.class,
      CreateVersionAndAddToProjectTest.class,
      AddLanguageTest.class,
      TranslatorJoinsLanguageTeamTest.class,
      PushPodirPluralProjectTest.class,
      DocumentListInWebTransTest.class
})
public class CreateSampleProjectTestSuite
{

//   @Test(expected = RuntimeException.class)
   public void cannotCreateProjectWithSameProjectId() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      ProjectPage projectPage = projectWorkFlow.createNewProject("project-a", "project a");
      assertThat(projectPage.getTitle(), Matchers.containsString("Zanata:project a"));

      //second time
      projectWorkFlow.createNewProject("project-a", "project with same slug/project id");
   }

//   @Test
   public void canCreateSameVersionIdOnDifferentProjects() {
      new LoginWorkFlow().signIn("admin", "admin");
      ProjectWorkFlow projectWorkFlow = new ProjectWorkFlow();
      projectWorkFlow.createNewProject("project-b", "project b")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      //second time
      ProjectVersionPage projectVersionPage = projectWorkFlow
            .createNewProject("project-c", "project with same version slug/version id")
            .clickCreateVersionLink().inputVersionId("master").saveVersion();

      assertThat(projectVersionPage.getTitle(), Matchers.equalTo("Zanata:project-c:master"));
   }

}
