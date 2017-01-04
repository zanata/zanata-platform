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
package org.zanata.rest.service.raw;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.EntityStatus;
import org.zanata.common.ProjectType;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertHeaderPresent;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class ProjectRawRestITCase extends RestTest {

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
                "GET") {
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
    public void getXml() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects/p/sample-project"),
                "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJaxbUnmarshal(response, Project.class);

                Project project = jaxbUnmarshal(response, Project.class);
                assertThat(project.getId(), is("sample-project"));
                assertThat(project.getDescription(), is("An example Project"));
                assertThat(project.getStatus(), is(EntityStatus.ACTIVE));
                assertThat(project.getName(), is("Sample Project"));
                // assertThat(project.getType(),
                // is(ProjectType.IterationProject));
                assertThat(project.getIterations().size(), is(3));

                // Iteration 1
                ProjectIteration iteration = project.getIterations().get(0);
                assertThat(iteration.getId(), is("1.0"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));

                // Iteration 2
                iteration = project.getIterations().get(1);
                assertThat(iteration.getId(), is("1.1"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveNonExistingProject() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects/p/do-not-exist-project"),
                "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(404));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveObsoleteProject() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects/p/obsolete-project"),
                "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                // Obsolete projects are not found
                assertThat(response.getStatus(), is(404));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveRetiredProject() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects/p/retired-project"),
                "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                // Retired projects can be read
                assertThat(response.getStatus(), is(200));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getJson() throws Exception {
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
                assertThat(project.getId(), is("sample-project"));
                assertThat(project.getDescription(), is("An example Project"));
                assertThat(project.getStatus(), is(EntityStatus.ACTIVE));
                assertThat(project.getName(), is("Sample Project"));
                // assertThat(project.getType(),
                // is(ProjectType.IterationProject));
                assertThat(project.getIterations().size(), is(3));

                // Iteration 1
                ProjectIteration iteration = project.getIterations().get(0);
                assertThat(iteration.getId(), is("1.0"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));

                // Iteration 2
                iteration = project.getIterations().get(1);
                assertThat(iteration.getId(), is("1.1"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putXml() throws Exception {
        final Project project =
                new Project("test-project", "Test Project",
                        ProjectType.Gettext.toString(),
                        "This is a Test project");
        project.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(new ProjectIteration("test-1.0"));
        project.getIterations(true).add(new ProjectIteration("test-2.0"));

        new ResourceRequest(getRestEndpointUrl("/projects/p/test-project"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
                        jaxbMarhsal(project).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(201)); // Created
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void createProjectWithInvalidSlug() throws Exception {
        final Project project =
                new Project("test-project", "Test Project",
                        ProjectType.Gettext.toString(),
                        "This is a Test project");
        project.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(new ProjectIteration("test-1.0"));
        project.getIterations(true).add(new ProjectIteration("test-2.0"));

        new ResourceRequest(getRestEndpointUrl("/projects/p/my,new,project"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
                        jaxbMarhsal(project).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(404));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void createProjectWithInvalidData() throws Exception {
        final String invalidProjectName =
                "My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project";
        final Project project =
                new Project("my-new-project", invalidProjectName,
                        ProjectType.Gettext.toString(),
                        "This is a Test project");
        project.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(new ProjectIteration("test-1.0"));
        project.getIterations(true).add(new ProjectIteration("test-2.0"));

        new ResourceRequest(getRestEndpointUrl("/projects/p/my-new-project"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
                        jaxbMarhsal(project).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getResponseStatus(), is(
                        Response.Status.BAD_REQUEST));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void updateProjectWithInvalidData() throws Exception {
        final String invalidProjectName =
                "My test ProjectMy test ProjectMy test ProjectMy test ProjectMy test ProjectMy test Project";
        final Project project =
                new Project("test-project", invalidProjectName,
                        ProjectType.Gettext.toString(),
                        "This is a Test project");
        project.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(new ProjectIteration("test-1.0"));
        project.getIterations(true).add(new ProjectIteration("test-2.0"));

        new ResourceRequest(getRestEndpointUrl("/projects/p/test-project"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_XML,
                        jaxbMarhsal(project).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getResponseStatus(), is(
                        Response.Status.BAD_REQUEST));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putJson() throws Exception {
        final Project project =
                new Project("test-project", "Test Project",
                        ProjectType.Gettext.toString(),
                        "This is a Test project");
        project.setStatus(EntityStatus.ACTIVE);
        project.getIterations(true).add(new ProjectIteration("test-1.0"));
        project.getIterations(true).add(new ProjectIteration("test-2.0"));

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/test-project-json"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_PROJECT_JSON,
                        jsonMarshal(project).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(201)); // Created
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllXml() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertThat((String) response.getEntity(String.class),
                        containsString("sample-project"));
                assertThat((String) response.getEntity(String.class),
                        containsString("retired-project"));
                assertThat((String) response.getEntity(String.class),
                        not(containsString("obsolete-project")));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllJson() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/projects"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECTS_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertThat((String) response.getEntity(String.class),
                        containsString("sample-project"));
                assertThat((String) response.getEntity(String.class),
                        containsString("retired-project"));
                assertThat((String) response.getEntity(String.class),
                        not(containsString("obsolete-project")));
            }
        }.run();
    }

}
