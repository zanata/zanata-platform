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

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.EntityStatus;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.ProjectIteration;

import static org.assertj.core.api.Assertions.assertThat;
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            protected void onResponse(Response response) {
                // OK
                assertThat(response.getStatus()).isEqualTo(200);
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // OK
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, ProjectIteration.class);

                ProjectIteration iteration =
                        jaxbUnmarshal(entityString, ProjectIteration.class);
                assertThat(iteration.getId()).isEqualTo("1.0");
                assertThat(iteration.getStatus()).isEqualTo(EntityStatus.ACTIVE);
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // OK
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, ProjectIteration.class);
                ProjectIteration iteration =
                        jsonUnmarshal(entityString, ProjectIteration.class);
                assertThat(iteration.getId()).isEqualTo("1.0");
                assertThat(iteration.getStatus()).isEqualTo(EntityStatus.ACTIVE);
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // Iteration not found because project is obsolete
                assertThat(response.getStatus())
                        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // 200 retired projects are readable
                assertThat(response.getStatus())
                        .isEqualTo(Response.Status.OK.getStatusCode());
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus())
                        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus())
                        // 404
                        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(iteration), MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // Created
                assertThat(response.getStatus()).isEqualTo(201);
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(iteration),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(404);
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(iteration),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(iteration),
                                MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                // Created
                assertThat(response.getStatus()).isEqualTo(201);
            }
        }.run();
    }

}
