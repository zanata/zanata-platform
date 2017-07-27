package org.zanata.rest.service.raw;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.EntityStatus;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.ProjectIteration;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.RawRestTestUtils.assertHeaderPresent;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

/**
 * @author Alex Eng <a href="aeng@redhat.com">aeng@redhat.com</a>
 */
public class ProjectVersionRawRestITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
            "org/zanata/test/model/AccountData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
            "org/zanata/test/model/ProjectsData.dbunit.xml",
            DatabaseOperation.CLEAN_INSERT));

        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void head() throws Exception {
        new ResourceRequest(
            getRestEndpointUrl("/project/sample-project/version/1.0"), "GET") {
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
            getRestEndpointUrl("/project/sample-project/version/1.0"), "GET") {
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
            getRestEndpointUrl("/project/sample-project/version/1.0"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
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
    public void getCurrentVersionOnObsoleteProject() throws Exception {
        new ResourceRequest(
            getRestEndpointUrl(
                "/project/obsolete-project/version/obsolete-current"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // Version not found because project is obsolete
                assertThat(response.getStatus())
                        .isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getCurrentVersionOnRetiredProject() throws Exception {
        new ResourceRequest(
            getRestEndpointUrl(
                "/project/retired-project/version/retired-current"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                    MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // 200 (Retired projects are readable)
                assertThat(response.getStatus())
                    .isEqualTo(Response.Status.OK.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getNotExistProjectVersion() throws Exception {
        new ResourceRequest(
            getRestEndpointUrl(
                "/project/obsolete-project/version/i-dont-exist"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
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
    public void getObsoleteVersionOnCurrentProject() throws Exception {
        new ResourceRequest(
            getRestEndpointUrl(
                "/project/current-project/version/current-obsolete"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
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
        final ProjectIteration version = new ProjectIteration("test-iteration");
        version.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
            getRestEndpointUrl(
                "/project/sample-project/version/test-iteration"),
            "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
               return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(version), MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
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
        final ProjectIteration version = new ProjectIteration("test-iteration");
        version.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
            getRestEndpointUrl(
                "/project/sample-project/version/my,new,iteration"),
            "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(version), MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
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
        final ProjectIteration version = new ProjectIteration("1.0");
        version.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
            getRestEndpointUrl("/project/sample-project/version/1.0"),
            "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(version), MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML);
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
        final ProjectIteration version = new ProjectIteration("test-iteration");
        version.setStatus(EntityStatus.ACTIVE);

        new ResourceRequest(
            getRestEndpointUrl(
                "/project/sample-project/version/test-iteration-json"),
            "PUT", getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(version), MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_JSON);
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
