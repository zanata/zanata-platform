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
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.ProjectIteration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertHeaderPresent;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class ProjectIterationRawRestITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void head() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "GET", getAuthorizedEnvironment()) {
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
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJaxbUnmarshal(response, ProjectIteration.class);

                ProjectIteration iteration =
                        jaxbUnmarshal(response, ProjectIteration.class);
                assertThat(iteration.getId(), is("1.0"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getJson() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJsonUnmarshal(response, ProjectIteration.class);

                ProjectIteration iteration =
                        jsonUnmarshal(response, ProjectIteration.class);
                assertThat(iteration.getId(), is("1.0"));
                assertThat(iteration.getStatus(), is(EntityStatus.ACTIVE));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getCurrentIterationOnObsoleteProject() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/obsolete-project/iterations/i/obsolete-current"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Response.Status.NOT_FOUND.getStatusCode())); // Iteration
                                                                        // not
                                                                        // found
                                                                        // because
                                                                        // project
                                                                        // is
                                                                        // obsolete
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getCurrentIterationOnRetiredProject() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/retired-project/iterations/i/retired-current"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Response.Status.OK.getStatusCode())); // 200 (Retired
                                                                 // projects are
                                                                 // readable)
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getNotExistProjectIteration() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/obsolete-project/iterations/i/i-dont-exist"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Response.Status.NOT_FOUND.getStatusCode()));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getObsoleteIterationOnCurrentProject() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/current-project/iterations/i/current-obsolete"),
                "GET", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Response.Status.NOT_FOUND.getStatusCode())); // 404
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putXml() throws Exception {
        final ProjectIteration iteration =
                new ProjectIteration("test-iteration");
        iteration.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/test-iteration"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
                        jaxbMarhsal(iteration).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(201)); // Created
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void createWithInvalidSlug() throws Exception {
        final ProjectIteration iteration =
                new ProjectIteration("test-iteration");
        iteration.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/my,new,iteration"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
                        jaxbMarhsal(iteration).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(404));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putSameProject() throws Exception {
        final ProjectIteration iteration =
                new ProjectIteration("1.0");
        iteration.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML,
                        jaxbMarhsal(iteration).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putJson() throws Exception {
        final ProjectIteration iteration =
                new ProjectIteration("test-iteration");
        iteration.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/test-iteration-json"),
                "PUT", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON,
                        jsonMarshal(iteration).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(201)); // Created
            }
        }.run();
    }

}
