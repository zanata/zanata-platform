package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

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
import org.zanata.rest.client.ProjectsClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.collect.Lists;

public class ProjectPromptTest {
    private ProjectPrompt prompt;
    private InitOptions opts;
    @Mock
    private ProjectIterationPrompt iterationPrompt;

    @Captor
    private ArgumentCaptor<Project> projectCaptor;
    @Mock
    private RestClientFactory clientFactory;
    @Mock
    private ProjectsClient projectsClient;
    @Mock
    private ProjectClient projectClient;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        opts = new InitOptionsImpl();
        when(clientFactory.getProjectsClient()).thenReturn(projectsClient);
    }

    @Test
    public void willShowAllActiveProjectsIfUserChooseToSelect()
            throws IOException {
        Project project = new Project("gcc", "gnu C compiler", "gettext");
        ProjectIteration iteration = new ProjectIteration("master");
        iteration.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(iteration);
        when(projectsClient.getProjects()).thenReturn(
                new Project[] { project });
        when(clientFactory.getProjectClient("gcc")).thenReturn(projectClient);
        when(projectClient.get()).thenReturn(project);

        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("1", "1", "1",
                        "1");
        prompt =
                new ProjectPrompt(console, opts,
                        new ProjectIterationPrompt(console, opts,
                                clientFactory),
                        clientFactory);

        prompt.selectOrCreateNewProjectAndVersion();

        assertThat(opts.getProj(), Matchers.equalTo("gcc"));
        assertThat(opts.getProjectType(), Matchers.equalTo("gettext"));
        assertThat(opts.getProjectVersion(), Matchers.equalTo("master"));
    }

    @Test
    public void willFilterAllProjectsIfUserTypeLetters() {
        when(projectsClient.getProjects())
                .thenReturn(new Project[] {
                        makeProject("project-1", "project one"),
                        makeProject("project-2", "project two"),
                        makeProject("project-99", "project 99") });

        // Given: user input
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(
                        "99", // part of project name
                        "1");
        prompt =
                new ProjectPrompt(console, opts,
                        mock(ProjectIterationPrompt.class), clientFactory);
        prompt.selectProject();

        assertThat(opts.getProj(), Matchers.equalTo("project-99"));
        assertThat(opts.getProjectType(), Matchers.equalTo("gettext"));
    }

    private static Project makeProject(String slug, String name) {
        Project project = new Project();
        project.setId(slug);
        project.setName(name);
        project.setDefaultType("gettext");
        return project;
    }

    @Test
    public void canFilterProject() {
        prompt =
                new ProjectPrompt(mock(ConsoleInteractor.class), opts,
                        mock(ProjectIterationPrompt.class), clientFactory);

        Project gcc = makeProject("gcc", "gnu c compiler");
        Project aboutFedora = makeProject("about-fedora", "about fedora");
        Project ibus =
                makeProject("ibus-pinyin", "ibus pinyin input method");
        Project tar = makeProject("gnu-tar", "tar");
        List<Project> allProjects =
                Lists.newArrayList(gcc, aboutFedora, ibus, tar);
        List<Project> filteredProjects =
                Lists.newArrayList(gcc, ibus, tar);
        prompt.setAllProjectsAndFilteredProjects(allProjects, filteredProjects);

        assertThat(prompt.filterBy(""), Matchers.equalTo(allProjects));
        assertThat(prompt.filterBy("gnu"), Matchers.contains(gcc, tar));
        assertThat(prompt.filterBy("bus"), Matchers.contains(ibus));
    }

    // this test uses mock for REST call and everything.
    @Test
    public void willGuideUserIfUserChooseToCreateNewProjectAndVersion()
            throws IOException {
        final String projectId = "gcc";
        final String projectType = "gettext";
        final String versionId = "master";
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers(projectId,
                        "C compiler",
                        projectType, versionId);
        when(clientFactory.getProjectClient("gcc")).thenReturn(projectClient);
        doNothing().when(projectClient).put(projectCaptor.capture());
        prompt =
                new ProjectPrompt(console, opts,
                        iterationPrompt, clientFactory);

        prompt.createNewProject();

        verify(projectClient).put(projectCaptor.capture());
        verify(iterationPrompt).createNewVersion();
        Project project = projectCaptor.getValue();
        assertThat(project.getId(), Matchers.equalTo(projectId));
        assertThat(project.getName(), Matchers.equalTo("C compiler"));
        assertThat(project.getDefaultType(), Matchers.equalTo(projectType));
    }

}
