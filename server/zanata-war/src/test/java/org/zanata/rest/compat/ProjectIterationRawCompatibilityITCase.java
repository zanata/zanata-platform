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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.ProjectIteration;
import org.zanata.rest.ResourceRequest;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.*;

public class ProjectIterationRawCompatibilityITCase extends RestTest {

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
    public void getJsonProjectIteration() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            }

            @Override
            protected void onResponse(Response response)
                    throws IOException {
                assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, ProjectIteration.class);

                ProjectIteration it =
                        jsonUnmarshal(entityString, ProjectIteration.class);
                assertThat(it.getId(), is("1.0"));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getXmlProjectIteration() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/1.0"),
                "GET") {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, ProjectIteration.class);

                ProjectIteration it =
                        jaxbUnmarshal(entityString, ProjectIteration.class);
                assertThat(it.getId(), is("1.0"));
            }

        }.run();
    }

    @Test
    @RunAsClient
    public void putJsonProjectIteration() throws Exception {
        final ProjectIteration newIteration =
                new ProjectIteration("new-iteration");

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/"
                        + newIteration.getId()), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(newIteration),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(),
                        is(Status.CREATED.getStatusCode())); // 201
            }

        }.run();

        // Retreive it again
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/"
                        + newIteration.getId()),
                "GET") {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, ProjectIteration.class);
                ProjectIteration it =
                        jsonUnmarshal(entityString, ProjectIteration.class);
                assertThat(it.getId(), is("new-iteration"));
            }

        }.run();
    }

    @Test
    @RunAsClient
    public void putXmlProjectIteration() throws Exception {
        final ProjectIteration newIteration =
                new ProjectIteration("new-iteration");

        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/"
                        + newIteration.getId()), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(newIteration),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(),
                        is(Status.CREATED.getStatusCode())); // 201
            }

        }.run();

        // Retreive it again
        new ResourceRequest(
                getRestEndpointUrl("/projects/p/sample-project/iterations/i/"
                        + newIteration.getId()),
                "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(Status.OK.getStatusCode())); // 200
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, ProjectIteration.class);

                ProjectIteration it =
                        jaxbUnmarshal(entityString, ProjectIteration.class);
                assertThat(it.getId(), is("new-iteration"));
            }

        }.run();
    }
}
