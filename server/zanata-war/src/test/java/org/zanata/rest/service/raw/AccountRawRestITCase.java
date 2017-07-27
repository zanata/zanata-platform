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
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.Account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonMarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class AccountRawRestITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void xmlGetUnavailable() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/NOT_AVAILABLE"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void xmlGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("accounts/u/admin"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, Account.class);

                Account account = jaxbUnmarshal(entityString, Account.class);
                assertThat(account.getUsername()).isEqualTo("admin");
                assertThat(account.getPasswordHash()).isEqualTo("Eyox7xbNQ09MkIfRyH+rjg==");
                assertThat(account.getEmail()).isEqualTo("root@localhost");
                assertThat(account.getApiKey()).isEqualTo("b6d7044e9ee3b2447c28fb7c50d86d98");
                // 1 role
                assertThat(account.getRoles().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void jsonGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/admin"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Account.class);

                Account account = jsonUnmarshal(entityString, Account.class);
                assertThat(account.getUsername()).isEqualTo("admin");
                assertThat(account.getPasswordHash()).isEqualTo("Eyox7xbNQ09MkIfRyH+rjg==");
                assertThat(account.getEmail()).isEqualTo("root@localhost");
                assertThat(account.getApiKey()).isEqualTo("b6d7044e9ee3b2447c28fb7c50d86d98");
                // 1 role
                assertThat(account.getRoles().size()).isEqualTo(1);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void xmlPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jaxbMarhsal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void jsonPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedPut() throws Exception {
        final Account account =
                new Account("test@testing.com", "Test Account", "testuser",
                        "Eyox7xbNQ09MkIfRyH+rjg==");
        account.setEnabled(false);

        new ResourceRequest(getRestEndpointUrl("/accounts/u/testuser"), "PUT") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            public void invoke(Invocation.Builder builder) {
                Entity<String> entity = Entity
                        .entity(jsonMarshal(account), MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
                Response response = builder.buildPut(entity).invoke();
                onResponse(response);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(Status.UNAUTHORIZED.getStatusCode());
            }
        }.run();
    }

}
