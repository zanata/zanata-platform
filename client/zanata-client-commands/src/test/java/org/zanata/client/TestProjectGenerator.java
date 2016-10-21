package org.zanata.client;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConfigurableProjectOptions;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.common.ProjectType;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;

/**
 * This class can be used to make use of a number of sample projects (located
 * under src/test/resources/testProjects). Each sub directory represents a
 * sample project for a project type. It has typical file structure and minimal
 * test data. Test class can then use project type to get to the sample project
 * root directory for testing. It can also be used to quickly set up data on
 * local running zanata instances in preparation for manual tests.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TestProjectGenerator {
    private static final Logger log =
            LoggerFactory.getLogger(TestProjectGenerator.class);
    private Map<ProjectType, File> projectRootMap;

    public TestProjectGenerator() {
        File projectsRoot = findTestProjectsRoot();
        File[] subDirs = listAllSubDirectories(projectsRoot);
        ImmutableMap.Builder<ProjectType, File> builder =
                ImmutableMap.builder();
        for (File dir : subDirs) {
            try {
                ProjectType projectType = ProjectType.getValueOf(dir.getName());
                builder.put(projectType, dir);
            } catch (Exception e) {
                log.warn(
                        "can not resolve {} as project type. Naming convention is to name the test project folder after its project type.",
                        dir.getName());
            }
        }
        projectRootMap = builder.build();
    }

    private static File findTestProjectsRoot() {
        try {
            URL testProjects = Thread.currentThread().getContextClassLoader()
                    .getResource("testProjects");
            Preconditions.checkArgument(testProjects != null,
                    "can not find test projects");
            return new File(testProjects.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static File[] listAllSubDirectories(File projectsRoot) {
        return projectsRoot.listFiles((dir, name) -> dir.isDirectory());
    }

    /**
     * Ensure a project and a version are created on an instance with source and
     * translation pushed. This should only be used in manual test preparation.
     *
     * @param opts
     *            options
     * @param projectType
     *            a sample project representing this project type
     * @param instance
     *            either a local or a cargo/functional-test instance
     * @throws Exception
     */
    public void ensureProjectOnServer(ConfigurableProjectOptions opts,
            ProjectType projectType, ServerInstance instance) throws Exception {
        Preconditions.checkState(projectRootMap.containsKey(projectType),
                "We don't have sample project type for %s", projectType);

        opts.setUrl(new URI(instance.getUrl()).toURL());
        opts.setUsername(instance.getUsername());
        opts.setKey(instance.getKey());

        RestClientFactory clientFactory = OptionsUtil.createClientFactory(opts);

        Project projectDTO = createProjectDTO(projectRootMap.get(projectType));
        String projectSlug = projectDTO.getId();
        ProjectIteration iteration = projectDTO.getIterations().get(0);
        String iterationSlug = iteration.getId();

        // create project and version
        clientFactory.getProjectClient(projectSlug).put(projectDTO);
        clientFactory.getProjectIterationClient(projectSlug, iterationSlug)
                .put(iteration);
    }

    /**
     * @param projectType
     *            project type
     * @return base dir for a sample project representing given project type
     */
    public File getProjectBaseDir(ProjectType projectType) {
        File dir = projectRootMap.get(projectType);
        Preconditions.checkState(dir != null);
        return dir;
    }

    private Project createProjectDTO(File dir) {
        Project project = new Project();
        project.setId(sampleProjectSlug(dir));
        project.setName(dir.getName() + " sample project");
        project.setDefaultType(dir.getName());
        ProjectIteration iteration = new ProjectIteration();
        iteration.setId(sampleIterationSlug());
        iteration.setProjectType(dir.getName());
        project.getIterations(true).add(iteration);
        return project;
    }

    public String sampleProjectSlug(File dir) {
        return dir.getName() + "-project";
    }

    public String sampleIterationSlug() {
        return "master";
    }


    public static class ServerInstance {
        public static final ServerInstance
                FunctionalTestCargo = new ServerInstance("http://localhost:8180/zanata/", "admin", "b6d7044e9ee3b2447c28fb7c50d86d98");
        public static final ServerInstance Local = new ServerInstance("http://localhost:8080/zanata/", "admin", "b6d7044e9ee3b2447c28fb7c50d86d98");

        private final String url;
        private final String username;
        private final String key;

        public ServerInstance(String url, String username, String key) {
            this.url = url;
            this.username = username;
            this.key = key;
        }

        public String getUrl() {
            return url;
        }

        public URL getURL() {
            try {
                return new URI(url).toURL();
            } catch (MalformedURLException | URISyntaxException e) {
                throw Throwables.propagate(e);
            }
        }

        public String getUsername() {
            return username;
        }

        public String getKey() {
            return key;
        }
    }
}
