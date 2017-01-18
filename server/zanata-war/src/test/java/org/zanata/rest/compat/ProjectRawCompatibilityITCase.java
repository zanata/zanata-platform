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
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.apicompat.common.ProjectType;
import org.zanata.rest.ResourceRequest;
import org.zanata.apicompat.common.Namespaces;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.Project;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
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
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
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
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJsonUnmarshal(response, Project.class);
                Project project = jsonUnmarshal(response, Project.class);

                // Assert correct parsing of all properties
                assertThat(project.getId(), is("sample-project"));
                assertThat(project.getName(), is("Sample Project"));
                assertThat(project.getDescription(), is("An example Project"));
                assertThat(project.getIterations().size(), is(3));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllProjectsJson() throws Exception {
        // No client method for Json Get, so testing raw compatibility
        new ResourceRequest(getRestEndpointUrl("/projects/"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                List<Project> projects = jsonParse(response);
                Project sampleProject = null;

                // find sample project
                for (Project p : projects) {
                    if (p.getId().equals("sample-project")) {
                        sampleProject = p;
                    }
                }

                // Assertions on individual project
                assertThat(sampleProject, notNullValue());
                assertThat(sampleProject.getId(), is("sample-project"));
                assertThat(sampleProject.getName(), is("Sample Project"));
                assertThat(sampleProject.getLinks().size(), is(1));
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
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJaxbUnmarshal(response, Projects.class);
                Projects projects = jaxbUnmarshal(response, Projects.class);
                Project sampleProject = null;

                // find sample project
                for (Project p : projects.projects) {
                    if (p.getId().equals("sample-project")) {
                        sampleProject = p;
                    }
                }

                // Assertions on individual project
                assertThat(sampleProject, notNullValue());
                assertThat(sampleProject.getId(), is("sample-project"));
                assertThat(sampleProject.getName(), is("Sample Project"));
                assertThat(sampleProject.getLinks().size(), is(1));
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
            protected void prepareRequest(ClientRequest request) {
                // New Project
                Project p =
                        new Project("new-project", "New Project",
                                ProjectType.Podir.toString(),
                                "This is a New Sample Project");

                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
                        jsonMarshal(p));
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Status.CREATED.getStatusCode())); // 201
            }
        }.run();
    }

    private List<Project> jsonParse(ClientResponse response) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue((String) response.getEntity(String.class),
                    new TypeReference<List<Project>>() {
                    });
        } catch (JsonParseException e) {
            throw new AssertionError(e);
        } catch (JsonMappingException e) {
            throw new AssertionError(e);
        } catch (IllegalStateException e) {
            throw new AssertionError(e);
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
