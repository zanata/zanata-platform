package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.common.EntityStatus;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.collect.Lists;

public class ProjectIterationPromptTest {
    private ProjectIterationPrompt prompt;
    private InitOptionsImpl opts;

    @Captor
    private ArgumentCaptor<ProjectIteration> iterationCaptor;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private ProjectIterationClient projectIterationClient;
    @Mock
    private ProjectClient projectClient;

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

        when(clientFactory.getProjectClient(opts.getProj()))
                .thenReturn(projectClient);
        when(projectClient.get()).thenReturn(project);
        when(clientFactory.getProjectIterationClient(opts.getProj(), "4.8.2"))
                .thenReturn(projectIterationClient);
        // we want to select the second active version
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("2");
        prompt = new ProjectIterationPrompt(console, opts, clientFactory);

        prompt.selectVersion();

        verify(projectClient).get();
        assertThat(opts.getProjectVersion(), Matchers.equalTo("4.8.2"));
        assertThat(opts.getProjectType(), Matchers.equalTo("gettext"));
    }

    private static ProjectIteration newIteration(String id, EntityStatus status) {
        ProjectIteration iteration = new ProjectIteration(id);
        iteration.setStatus(status);
        iteration.setProjectType("gettext");
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
        prompt = new ProjectIterationPrompt(console, opts, clientFactory);
        when(clientFactory.getProjectIterationClient(opts.getProj(), versionId))
                .thenReturn(projectIterationClient);

        prompt.createNewVersion();

        verify(projectIterationClient).put(iterationCaptor.capture());
        ProjectIteration iteration = iterationCaptor.getValue();
        assertThat(iteration.getId(), Matchers.equalTo(versionId));
        assertThat(iteration.getProjectType(), Matchers.equalTo(projectType));
        assertThat(opts.getProjectVersion(), Matchers.equalTo("master"));
    }

}
