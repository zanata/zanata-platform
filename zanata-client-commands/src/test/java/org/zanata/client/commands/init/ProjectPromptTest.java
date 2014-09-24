package org.zanata.client.commands.init;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.zanata.client.commands.HTTPMockContainer.Builder.readFromClasspath;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;

import org.hamcrest.Matchers;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.zanata.client.commands.ConsoleInteractor;
import org.zanata.client.commands.HTTPMockContainer;
import org.zanata.client.commands.MockConsoleInteractor;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.client.IProjectsResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.Project;
import com.google.common.collect.Lists;

public class ProjectPromptTest {
    private ProjectPrompt prompt;
    private Connection connection;
    private InitOptions opts;
    private ZanataProxyFactory proxyFactory;
    @Mock
    private ProjectIterationPrompt iterationPrompt;
    @Mock
    private ClientResponse response;
    @Mock
    private IProjectResource projectResource;
    @Captor
    private ArgumentCaptor<Project> projectCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        opts = new InitOptionsImpl();
    }

    @After
    public void cleanUp() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    private void startMockServer(Container container) throws IOException {
        ContainerServer server = new ContainerServer(container);
        connection = new SocketConnection(server);
        InetSocketAddress address = (InetSocketAddress) connection
                .connect(new InetSocketAddress(0));
        int port = address.getPort();

        opts.setUrl(URI.create("http://localhost:" + port).toURL());
        opts.setUsername("admin");
        opts.setKey("abcde");
        opts.setLogHttp(false);
        proxyFactory = OptionsUtil.createRequestFactory(opts);
    }

    // this test sets up a localhost mock server and simulate full REST round
    // trip.
    @Test
    public void willShowAllActiveProjectsIfUserChooseToSelect()
            throws IOException {
        HTTPMockContainer container =
                HTTPMockContainer.Builder
                        .builder()
                        .onPathReturnOk(
                                Matchers.endsWith("/version"),
                                readFromClasspath("serverresponse/version.xml"))
                        .onPathReturnOk(
                                Matchers.endsWith("/projects"),
                                readFromClasspath("serverresponse/projects.xml"))
                        .onPathReturnOk(
                                Matchers.endsWith("/projects/p/gcc"),
                                readFromClasspath("serverresponse/iteration.xml"))
                        .build();
        startMockServer(container);
        ConsoleInteractor console =
                MockConsoleInteractor.predefineAnswers("1", "1", "1",
                        "1");
        prompt =
                new ProjectPrompt(console, opts, proxyFactory,
                        new ProjectIterationPrompt(console, opts, proxyFactory));

        prompt.selectOrCreateNewProjectAndVersion();

        assertThat(opts.getProj(), Matchers.equalTo("gcc"));
        assertThat(opts.getProjectType(), Matchers.equalTo("gettext"));
        assertThat(opts.getProjectVersion(), Matchers.equalTo("master"));
    }

    @Test
    public void willFilterAllProjectsIfUserTypeLetters() {
        proxyFactory = mock(ZanataProxyFactory.class);

        IProjectsResource projectsResource = mock(IProjectsResource.class,
                Answers.RETURNS_DEEP_STUBS.get());
        when(proxyFactory.getProjectsResource()).thenReturn(projectsResource);
        when(projectsResource.get().getEntity(Project[].class))
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
                new ProjectPrompt(console, opts, proxyFactory,
                        mock(ProjectIterationPrompt.class));
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
                        mock(ZanataProxyFactory.class),
                        mock(ProjectIterationPrompt.class));

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
        proxyFactory = mock(ZanataProxyFactory.class);

        when(proxyFactory.getProject(projectId)).thenReturn(projectResource);
        when(projectResource.put(projectCaptor.capture())).thenReturn(response);
        when(response.getStatus()).thenReturn(201);
        prompt =
                new ProjectPrompt(console, opts, proxyFactory,
                        iterationPrompt);

        prompt.createNewProject();

        verify(proxyFactory).getProject(projectId);
        verify(projectResource).put(projectCaptor.capture());
        verify(iterationPrompt).createNewVersion();
        Project project = projectCaptor.getValue();
        assertThat(project.getId(), Matchers.equalTo(projectId));
        assertThat(project.getName(), Matchers.equalTo("C compiler"));
        assertThat(project.getDefaultType(), Matchers.equalTo(projectType));
    }

}
