package org.zanata.client.commands.init;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ProjectIterationPromptTest {
    private ProjectIterationPrompt prompt;
    private InitOptionsImpl opts;
    @Mock
    private ZanataProxyFactory requestFactory;
    @Mock
    private IProjectIterationResource iterationResource;
    @Mock
    private ClientResponse response;
    @Captor
    private ArgumentCaptor<ProjectIteration> iterationCaptor;
    @Mock
    private IProjectResource projectResource;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        opts = new InitOptionsImpl();
        prompt = null;

    }

    @Test
    public void willGuideUserToSelectVersion() {
        final String projectId = "gcc";
        final String projectType = "gettext";
        final Project project = new Project();
        project.setId(projectId);
        project.setName(projectId);
        project.setIterations(Lists.newArrayList(
                newIteration("master", EntityStatus.ACTIVE),
                newIteration("3.0", EntityStatus.OBSOLETE),
                newIteration("4.8.2", EntityStatus.ACTIVE)));
        opts.setProj(projectId);
        opts.setProjectType(projectType);

        when(requestFactory.getProject(projectId))
                .thenReturn(projectResource);
        when(projectResource.get()).thenReturn(response);
        when(response.getEntity(Project.class)).thenReturn(project);

        // we want to select the second active version
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("2");
        prompt = new ProjectIterationPrompt(console, opts, requestFactory);

        prompt.selectVersion();

        verify(projectResource).get();
        assertThat(opts.getProjectVersion(), Matchers.equalTo("4.8.2"));
    }

    private static ProjectIteration newIteration(String id, EntityStatus status) {
        ProjectIteration iteration = new ProjectIteration(id);
        iteration.setStatus(status);
        return iteration;
    }

    @Test
    public void willGuideUserToCreateNewVersion()
            throws IOException {
        final String projectId = "gcc";
        final String projectType = "gettext";
        final String versionId = "master";
        opts.setProj(projectId);
        opts.setProjectType(projectType);
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(versionId);
        prompt = new ProjectIterationPrompt(console, opts, requestFactory);

        when(requestFactory.getProjectIteration(projectId, versionId))
                .thenReturn(iterationResource);
        when(iterationResource.put(iterationCaptor.capture())).thenReturn(
                response);
        when(response.getStatus()).thenReturn(201);

        prompt.createNewVersion();

        verify(iterationResource).put(iterationCaptor.capture());
        ProjectIteration iteration = iterationCaptor.getValue();
        assertThat(iteration.getId(), Matchers.equalTo(versionId));
        assertThat(iteration.getProjectType(), Matchers.equalTo(projectType));
        assertThat(opts.getProjectVersion(), Matchers.equalTo("master"));
    }

}
