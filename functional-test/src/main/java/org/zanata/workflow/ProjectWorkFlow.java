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
package org.zanata.workflow;

import java.util.HashMap;
import java.util.List;

import org.zanata.page.projects.CreateVersionPage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.page.projects.ProjectsPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectWorkFlow extends AbstractWebWorkFlow {

    /**
     * Creates a new project using minimal input, all other items are default.
     * This function is lenient, i.e. will not fail if the project exists.
     * @param projectId Project identifier
     * @param projectName Project short name
     * @return new Project page for created project
     */
    public ProjectPage createNewSimpleProject(String projectId,
                                              String projectName) {
        ProjectsPage projectsPage = goToHome().goToProjects();
        List<String> projects = projectsPage.getProjectNamesOnCurrentPage();
        log.info("current projects: {}", projects);

        if (projects.contains(projectName)) {
            log.warn("{} already exists. This test environment is not clean.",
                    projectId);
            // since we can't create same project multiple times,
            // if we run this test more than once manually, we don't want it to
            // fail
            return projectsPage.goToProject(projectName);
        }
        return projectsPage.clickOnCreateProjectLink()
                .inputProjectId(projectId).inputProjectName(projectName)
                .saveProject();
    }

    /**
     * Create a project in full, using the details given in settings.<br/>
     * All items must be defined.
     * @param settings A HashMap of project identifiers and settings
     * @return a new Project page for the created project
     * @see {@link #projectDefaults()}
     */
    public ProjectPage createNewProject(HashMap<String, String> settings) {
        ProjectsPage projectsPage = goToHome().goToProjects();
        List<String> projects = projectsPage.getProjectNamesOnCurrentPage();
        log.info("current projects: {}", projects);
        return projectsPage.clickOnCreateProjectLink()
                .inputProjectId(settings.get("Project ID"))
                .inputProjectName(settings.get("Name"))
                .enterDescription(settings.get("Description"))
                .selectProjectType(settings.get("Project Type"))
                .enterHomepageContent(settings.get("Homepage Content"))
                .enterViewSourceURL(settings.get("View source files"))
                .enterDownloadSourceURL(settings.get("Source Download/Checkout"))
                .selectStatus(settings.get("Status"))
                .saveProject();
    }

    public static HashMap<String, String> projectDefaults() {
        HashMap<String, String> defaults = new HashMap<String, String>();
        defaults.put("Project ID", "");
        defaults.put("Name", "");
        defaults.put("Description", "");
        defaults.put("Project Type", "None");
        defaults.put("Homepage Content", "");
        defaults.put("View source files", "");
        defaults.put("Source Download/Checkout", "");
        defaults.put("Status", "ACTIVE");
        return defaults;
    }

    /**
     * By default this will create a podir project version
     *
     * @param projectName
     *            project name
     * @param projectVersion
     *            project version id
     * @return project version page
     */
    public ProjectVersionPage createNewProjectVersion(String projectName,
            String projectVersion) {
        ProjectPage projectPage = goToProjectByName(projectName);
        if (projectPage.getVersions().contains(projectVersion)) {
            log.warn(
                    "{} has already been created. Presumably you are running test manually and more than once.",
                    projectVersion);
            return projectPage.goToVersion(projectVersion);
        }
        CreateVersionPage createVersionPage =
                projectPage.clickCreateVersionLink().inputVersionId(
                        projectVersion);
        createVersionPage.selectProjectType("Podir");
        createVersionPage.selectStatus("READONLY");
        createVersionPage.selectStatus("ACTIVE");
        return createVersionPage.saveVersion();
    }

    public ProjectPage goToProjectByName(String projectName) {
        ProjectsPage projects =
                goToHome().goToPage("Projects", ProjectsPage.class);
        log.info("go to project by name with current projects: {}, name: {}",
                projects.getProjectNamesOnCurrentPage(), projectName);
        return projects.goToProject(projectName);
    }
}
