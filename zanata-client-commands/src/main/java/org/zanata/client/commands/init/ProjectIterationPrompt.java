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

import java.util.List;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Confirmation;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Hint;
import static org.zanata.client.commands.ConsoleInteractor.DisplayMode.Question;
import static org.zanata.client.commands.ConsoleInteractorImpl.AnswerValidatorImpl.expect;
import static org.zanata.client.commands.Messages._;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class ProjectIterationPrompt {
    private final ConsoleInteractor consoleInteractor;
    private final InitOptions opts;
    private final ZanataProxyFactory proxyFactory;

    ProjectIterationPrompt(ConsoleInteractor consoleInteractor,
            InitOptions opts, ZanataProxyFactory proxyFactory) {
        this.consoleInteractor = consoleInteractor;
        this.opts = opts;
        this.proxyFactory = proxyFactory;
    }

    public void selectOrCreateNewVersion() {
        consoleInteractor.printfln(_("do.you.want.to"));
        consoleInteractor.printf("1)")
                .printfln(Hint, _("project.version.select"));
        consoleInteractor.printf("2)")
                .printfln(Hint, _("project.version.create"));
        consoleInteractor.printf(Question, _("select.or.create"));
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
        Project project = proxyFactory.getProject(opts.getProj()).get()
                .getEntity(Project.class);
        consoleInteractor.printfln(_("available.versions"), project.getName());
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
        }
        consoleInteractor.printf(Question, _("select.version.prompt"));
        String selection =
                consoleInteractor.expectAnswerWithRetry(expect(versionIndexes));
        String versionId =
                Iterables.get(activeIterations,
                        (Integer.valueOf(selection) - 1))
                        .getId();
        opts.setProjectVersion(versionId);
    }

    @VisibleForTesting
    protected void createNewVersion() {
        consoleInteractor.printfln(Question, _("project.version.id.prompt"));
        String versionId = consoleInteractor.expectAnyAnswer();
        ProjectIteration iteration = new ProjectIteration(versionId);
        iteration.setProjectType(opts.getProjectType());
        ClientResponse response =
                proxyFactory.getProjectIteration(opts.getProj(), versionId)
                        .put(iteration);
        if (response.getStatus() >= 399) {
            InitCommand.offerRetryOnServerError(response, consoleInteractor);
            createNewVersion();
        }
        response.releaseConnection();
        opts.setProjectVersion(versionId);
        consoleInteractor.printfln(Confirmation, _("project.version.created"));

    }

    private static class ActiveIterationPredicate
            implements Predicate<ProjectIteration> {
        @Override
        public boolean apply(ProjectIteration input) {
            return input.getStatus() == EntityStatus.ACTIVE;
        }
    }
}
