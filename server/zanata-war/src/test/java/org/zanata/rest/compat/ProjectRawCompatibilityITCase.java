/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.compat;

import java.io.IOException;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.apicompat.common.ProjectType;
import org.zanata.apicompat.common.Namespaces;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.Project;
import org.zanata.rest.ResourceRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertHeaderPresent;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

/**
 * Compatibility tests for the Project REST resource endpoints not exposed over
 * the Resteasy client.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 *
 */
public class ProjectRawCompatibilityITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void head() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects/p/sample-project"),
                "HEAD") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            protected void onResponse(Response response)
                    throws IOException {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                assertHeaderPresent(response, HttpHeaders.ETAG);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getProjectJson() throws Exception {
        // No client method for Json Get, so testing raw compatibility
        new ResourceRequest(getRestEndpointUrl("/projects/p/sample-project"),
                "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Project.class);
                Project project = jsonUnmarshal(entityString, Project.class);

                // Assert correct parsing of all properties
                assertThat(project.getId()).isEqualTo("sample-project");
                assertThat(project.getName()).isEqualTo("Sample Project");
                assertThat(project.getDescription()).isEqualTo("An example Project");
                assertThat(project.getIterations().size()).isEqualTo(3);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllProjectsJson() throws Exception {
        // No client method for Json Get, so testing raw compatibility
        new ResourceRequest(getRestEndpointUrl("/projects/"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                List<Project> projects = jsonParse(response);
                Project sampleProject = null;

                // find sample project
                for (Project p : projects) {
                    if (p.getId().equals("sample-project")) {
                        sampleProject = p;
                    }
                }

                // Assertions on individual project
                assertThat(sampleProject).isNotNull();
                assertThat(sampleProject.getId()).isEqualTo("sample-project");
                assertThat(sampleProject.getName()).isEqualTo("Sample Project");
                assertThat(sampleProject.getLinks().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllProjectsXml() throws Exception {
        // testing raw compatibility. The 1.4.4 client interface was not working
        // correctly
        // for this endpoint. Hence, just testing the server portion
        new ResourceRequest(getRestEndpointUrl("/projects/"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, Projects.class);
                Projects projects = jaxbUnmarshal(entityString, Projects.class);
                Project sampleProject = null;

                // find sample project
                for (Project p : projects.projects) {
                    if (p.getId().equals("sample-project")) {
                        sampleProject = p;
                    }
                }

                // Assertions on individual project
                assertThat(sampleProject).isNotNull();
                assertThat(sampleProject.getId()).isEqualTo("sample-project");
                assertThat(sampleProject.getName()).isEqualTo("Sample Project");
                assertThat(sampleProject.getLinks().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putProjectJson() throws Exception {
        // No client method for Json Put, so testing raw compatibility
        new ResourceRequest(getRestEndpointUrl("/projects/p/new-project"),
                "PUT", getAuthorizedEnvironment()) {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                // New Project
                Project p =
                        new Project("new-project", "New Project",
                                ProjectType.Podir.toString(),
                                "This is a New Sample Project");
                Entity<String> entity = Entity
                        .entity(jsonMarshal(p),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // 201
                assertThat(response.getStatus())
                        .isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();
    }

    private List<Project> jsonParse(Response response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(response.readEntity(String.class),
                    new TypeReference<List<Project>>() {
                    });
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * JAXB Wrapper class for a list of projects. Only used for testing.
     */
    @XmlRootElement(name = "projects", namespace = Namespaces.ZANATA_API)
    private static class Projects {
        @XmlElementRef
        List<Project> projects;
    }

}
