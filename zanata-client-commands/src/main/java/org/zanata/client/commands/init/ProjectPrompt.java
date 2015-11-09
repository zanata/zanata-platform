/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.client.commands.init;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Confirmation;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl;
import static org.zanata.client.commands.Messages.get;

import java.util.Collections;
import java.util.List;

import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.common.EntityStatus;
import org.zanata.common.ProjectType;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Project;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class ProjectPrompt {
    private final ConsoleInteractor consoleInteractor;
    private final InitOptions opts;
    private final ProjectIterationPrompt
            projectIterationPrompt;
    private final RestClientFactory clientFactory;
    // state variables
    private List<Project> allProjects = Collections.emptyList();
    private List<Project> filteredProjects = Collections.emptyList();
    public static final List<String> PROJECT_TYPE_LIST =
            Lists.transform(Lists.newArrayList(ProjectType.values()),
                    new ProjectTypeToStringFunction());

    ProjectPrompt(ConsoleInteractor consoleInteractor, InitOptions opts,
            ProjectIterationPrompt projectIterationPrompt,
            RestClientFactory clientFactory) {
        this.consoleInteractor = consoleInteractor;
        this.opts = opts;
        this.projectIterationPrompt = projectIterationPrompt;
        this.clientFactory = clientFactory;
    }

    /**
     * Select or create a project and version.
     * If creating, also ask for Project type.
     */
    public void selectOrCreateNewProjectAndVersion() {
        consoleInteractor.printfln(get("do.you.want.to"));
        consoleInteractor.printf("1)").printfln(Hint, get("project.select"));
        consoleInteractor.printf("2)").printfln(Hint, get("project.create"));
        consoleInteractor.printf(Question, get("select.or.create"));
        String answer = consoleInteractor.expectAnswerWithRetry(
                AnswerValidatorImpl.expect("1", "2"));
        if (answer.equals("1")) {
            selectProject();
        } else {
            createNewProject();
        }
    }

    @VisibleForTesting
    protected void selectProject() {
        ensureActiveProjects();
        consoleInteractor.printfln(get("available.projects"));
        listFilteredProjectsPrefixedByNumber();
        consoleInteractor.printf(Question, get("select.project.prompt"));

        String selection = consoleInteractor.expectAnyAnswer();
        if (selectionIsFilter(selection, filteredProjects)) {
            filteredProjects = filterBy(selection);
            selectProject();
            return;
        }
        Project project = filteredProjects.get(Integer.parseInt(selection) - 1);
        String projectId = project.getId();
        opts.setProj(projectId);
        // TODO server returns Upper case project type!!!
        opts.setProjectType(project.getDefaultType().toLowerCase());
        consoleInteractor.printfln(Confirmation, get("project.confirmation"),
                opts.getProj());
        projectIterationPrompt.selectOrCreateNewVersion();
    }

    private void listFilteredProjectsPrefixedByNumber() {
        int oneBasedIndex = 1;
        for (Project project : filteredProjects) {
            consoleInteractor
                    .printf("%d)", oneBasedIndex++)
                    .printfln(Hint, "%s (%s)",
                            project.getId(), project.getName());
        }
    }

    private boolean selectionIsFilter(String selection,
            List<Project> filteredProjects) {
        boolean isNumber = selection.matches("\\d+");
        if (isNumber) {
            Integer indexNum = Integer.parseInt(selection) - 1;
            // if input is a valid index number
            boolean isValidIndex =
                    indexNum >= 0 && indexNum < filteredProjects.size();
            if (isValidIndex) {
                return false;
            }
        }
        return true;
    }

    @VisibleForTesting
    protected List<Project> filterBy(final String selection) {
        if (Strings.isNullOrEmpty(selection)) {
            return allProjects;
        }
        Iterable<Project> filter =
                Iterables.filter(filteredProjects, new Predicate<Project>() {
                    @Override
                    public boolean apply(Project input) {
                        String lowerCase = selection.toLowerCase();
                        return input.getId().toLowerCase()
                                .contains(lowerCase) ||
                                input.getName().toLowerCase()
                                        .contains(lowerCase);
                    }
                });
        return ImmutableList.copyOf(filter);
    }

    // we only talk to server once (unless server has empty project list)
    private void ensureActiveProjects() {
        if (filteredProjects.isEmpty()) {
            // TODO add optional query param to search projects (limit return
            // values)
            Project[] projectsArray =
                    clientFactory.getProjectsClient().getProjects();
            allProjects = ImmutableList.copyOf(Iterables
                    .filter(Lists.newArrayList(projectsArray),
                            new Predicate<Project>() {
                                @Override
                                public boolean apply(Project input) {
                                    return input.getStatus() ==
                                            EntityStatus.ACTIVE;
                                }
                            }));
            filteredProjects = ImmutableList.copyOf(allProjects);
        }
    }

    @VisibleForTesting
    protected void setAllProjectsAndFilteredProjects(List<Project> allProjects,
            List<Project> filteredProjects) {
        this.allProjects = allProjects;
        this.filteredProjects = filteredProjects;
    }

    @VisibleForTesting
    protected void createNewProject() {
        consoleInteractor.printfln(get("create.project.help"));
        consoleInteractor.printfln(Hint, get("project.id.constraint"));
        consoleInteractor.printfln(Question, get("project.id.prompt"));
        String projectId = consoleInteractor.expectAnyAnswer();
        consoleInteractor.printfln(Question, get("project.name.prompt"));
        String projectName = consoleInteractor.expectAnyAnswer();
        String projectTypes = Joiner.on(", ").join(PROJECT_TYPE_LIST);
        consoleInteractor.printfln(Question, get("project.type.prompt"), projectTypes);
        String projectType =
                consoleInteractor.expectAnswerWithRetry(
                        AnswerValidatorImpl.expect(PROJECT_TYPE_LIST));
        ProjectClient projectClient = clientFactory.getProjectClient(projectId);
        Project project = new Project(projectId, projectName, projectType);
        try {
            projectClient.put(project);
        } catch (UniformInterfaceException e) {
            if (e.getResponse().getStatus() >= 399) {
                InitCommand.offerRetryOnServerError(e, consoleInteractor);
                createNewProject();
            }
        }
        consoleInteractor.printfln(Confirmation, get("project.created"));
        opts.setProj(projectId);
        opts.setProjectType(projectType);
        projectIterationPrompt.createNewVersion();
    }

    private static class ProjectTypeToStringFunction
            implements Function<ProjectType, String> {
        @Override
        public String apply(ProjectType input) {
            return input.name().toLowerCase();
        }
    }
}
