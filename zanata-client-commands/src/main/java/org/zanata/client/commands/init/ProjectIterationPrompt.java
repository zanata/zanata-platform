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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Confirmation;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.expect;
import static org.zanata.client.commands.Messages.get;

import java.util.List;

import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class ProjectIterationPrompt {
    private final ConsoleInteractor consoleInteractor;
    private final InitOptions opts;
    private final RestClientFactory clientFactory;

    ProjectIterationPrompt(ConsoleInteractor consoleInteractor,
            InitOptions opts, RestClientFactory clientFactory) {
        this.consoleInteractor = consoleInteractor;
        this.opts = opts;
        this.clientFactory = clientFactory;
    }

    public void selectOrCreateNewVersion() {
        consoleInteractor.printfln(get("do.you.want.to"));
        consoleInteractor.printf("1)")
                .printfln(Hint, get("project.version.select"));
        consoleInteractor.printf("2)")
                .printfln(Hint, get("project.version.create"));
        consoleInteractor.printf(Question, get("select.or.create"));
        String answer = consoleInteractor.expectAnswerWithRetry(
                expect("1", "2"));
        if (answer.equals("1")) {
            selectVersion();
        } else {
            createNewVersion();
        }
    }

    @VisibleForTesting
    protected void selectVersion() {
        Project project = clientFactory.getProjectClient(opts.getProj()).get();
        consoleInteractor.printfln(get("available.versions"), project.getName());
        int oneBasedIndex = 1;
        List<String> versionIndexes = Lists.newArrayList();
        Iterable<ProjectIteration> activeIterations = Iterables
                .filter(project.getIterations(),
                        new ActiveIterationPredicate());
        for (ProjectIteration iteration : activeIterations) {
            versionIndexes.add(oneBasedIndex + "");
            consoleInteractor
                    .printf("%d)", oneBasedIndex)
                    .printfln(Hint, iteration.getId());
            oneBasedIndex++;
        }
        consoleInteractor.printf(Question, get("select.version.prompt"));
        String selection =
                consoleInteractor.expectAnswerWithRetry(expect(versionIndexes));
        ProjectIteration projectIteration = Iterables.get(activeIterations,
                (Integer.parseInt(selection) - 1));
        String versionId = projectIteration.getId();
        opts.setProjectVersion(versionId);
        opts.setProjectType(resolveProjectType(project, projectIteration));
    }

    private String resolveProjectType(Project project,
            ProjectIteration projectIteration) {
        if (!isNullOrEmpty(projectIteration.getProjectType())) {
            return projectIteration.getProjectType().toLowerCase();
        } else if (!isNullOrEmpty(project.getDefaultType())) {
            return project.getDefaultType().toLowerCase();
        } else {
            String projectTypes =
                    Joiner.on(", ").join(ProjectPrompt.PROJECT_TYPE_LIST);
            consoleInteractor.printfln(Question, get("project.type.prompt"),
                    projectTypes);
            return consoleInteractor.expectAnswerWithRetry(
                    ConsoleInteractorImpl.AnswerValidatorImpl
                            .expect(ProjectPrompt.PROJECT_TYPE_LIST));
        }
    }

    @VisibleForTesting
    protected void createNewVersion() {
        consoleInteractor.printfln(Question, get("project.version.id.prompt"));
        String versionId = consoleInteractor.expectAnyAnswer();
        ProjectIteration iteration = new ProjectIteration(versionId);
        iteration.setProjectType(opts.getProjectType());
        try {
            clientFactory.getProjectIterationClient(opts.getProj(), versionId)
                    .put(iteration);
        } catch (UniformInterfaceException ex) {
            InitCommand.offerRetryOnServerError(ex, consoleInteractor);
            createNewVersion();
        }
        opts.setProjectVersion(versionId);
        consoleInteractor.printfln(Confirmation, get("project.version.created"));

    }

    private static class ActiveIterationPredicate
            implements Predicate<ProjectIteration> {
        @Override
        public boolean apply(ProjectIteration input) {
            return input.getStatus() == EntityStatus.ACTIVE;
        }
    }
}
