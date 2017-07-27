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

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.apicompat.rest.service.AccountResource;
import org.zanata.rest.ResourceRequest;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.Account;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class AccountRawCompatibilityITCase extends CompatibilityBase {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void getAccountJson() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/demo"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);

                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString, Account.class);
                Account account = jsonUnmarshal(entityString, Account.class);

                // Assert correct parsing of all properties
                assertThat(account.getUsername()).isEqualTo("demo");
                assertThat(account.getApiKey())
                        .isEqualTo("23456789012345678901234567890123");
                assertThat(account.getEmail()).isEqualTo("user1@localhost");
                assertThat(account.getName()).isEqualTo("Sample User");
                assertThat(account.getPasswordHash())
                        .isEqualTo("/9Se/pfHeUH8FJ4asBD6jQ==");
                assertThat(account.getRoles().size()).isEqualTo(1);
                // assertThat(account.getLanguages().size(), is(1)); // Language
                // teams are not being returned
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAccountXml() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/demo"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(Response response) {
                // Ok
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, Account.class);
                Account account = jaxbUnmarshal(entityString, Account.class);

                // Assert correct parsing of all properties
                assertThat(account.getUsername()).isEqualTo("demo");
                assertThat(account.getApiKey())
                        .isEqualTo("23456789012345678901234567890123");
                assertThat(account.getEmail()).isEqualTo("user1@localhost");
                assertThat(account.getName()).isEqualTo("Sample User");
                assertThat(account.getPasswordHash())
                        .isEqualTo("/9Se/pfHeUH8FJ4asBD6jQ==");
                assertThat(account.getRoles().size()).isEqualTo(1);
                // assertThat(account.getLanguages().size(), is(1)); // Language
                // teams are not being returned
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAccountJsonUnauthorized() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/demo"), "GET") {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(Response response)
                    throws IOException {
                assertThat(response.getStatus())
                        .isEqualTo(Status.UNAUTHORIZED.getStatusCode());
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putAccountJson() throws Exception {
        // New Account
        Account a =
                new Account("aacount2@localhost.com", "Sample Account",
                        "sampleaccount", "/9Se/pfHeUH8FJ4asBD6jQ==");

        final String resourceUrl = "/accounts/u/sampleaccount";

        AccountResource accountClient = getAccountResource(resourceUrl,
                AuthenticatedAsUser.Admin,
                MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);

        Response putResponse = accountClient.put(a);

        // Assert initial put
        assertThat(putResponse.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
        putResponse.close();

        // Modified Account
        a.setName("New Account Name");
        putResponse = accountClient.put(a);
        putResponse.close();

        // Assert modification
        assertThat(putResponse.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // Retrieve again
        String entityString = accountClient.get().readEntity(String.class);
        Account a2 = jsonUnmarshal(entityString, Account.class);
        assertThat(a2.getUsername()).isEqualTo(a.getUsername());
        assertThat(a2.getApiKey()).isEqualTo(a.getApiKey());
        assertThat(a2.getEmail()).isEqualTo(a.getEmail());
        assertThat(a2.getName()).isEqualTo(a.getName());
        assertThat(a2.getPasswordHash()).isEqualTo(a.getPasswordHash());
        assertThat(a2.getRoles().size()).isEqualTo(0);
        // assertThat(a2.getLanguages().size(), is(1)); // Language teams are not
        // being returned
    }

    @Test
    @RunAsClient
    public void putAccountXml() throws Exception {
        // New Account
        Account a =
                new Account("aacount2@localhost.com", "Sample Account",
                        "sampleaccount", "/9Se/pfHeUH8FJ4asBD6jQ==");

        final String resourceUrl = "/accounts/u/sampleaccount";

        AccountResource accountClient = getAccountResource(resourceUrl,
                AuthenticatedAsUser.Admin,
                MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);

        Response putResponse = accountClient.put(a);

        // Assert initial put
        assertThat(putResponse.getStatus()).isEqualTo(Status.CREATED.getStatusCode());

        // Modified Account
        a.setName("New Account Name");
        putResponse = accountClient.put(a);

        // Assert modification
        assertThat(putResponse.getStatus()).isEqualTo(Status.OK.getStatusCode());

        // Retrieve again
        String entityString = accountClient.get().readEntity(String.class);
        Account a2 = jaxbUnmarshal(entityString, Account.class);
        assertThat(a2.getUsername()).isEqualTo(a.getUsername());
        assertThat(a2.getApiKey()).isEqualTo(a.getApiKey());
        assertThat(a2.getEmail()).isEqualTo(a.getEmail());
        assertThat(a2.getName()).isEqualTo(a.getName());
        assertThat(a2.getPasswordHash()).isEqualTo(a.getPasswordHash());
        assertThat(a2.getRoles().size()).isEqualTo(0);
        // assertThat(a2.getLanguages().size(), is(1)); // Language teams are not
        // being returned
    }

    @Test
    @RunAsClient
    public void putAccountXmlUnauthorized() throws Exception {
        // New Account
        Account a =
                new Account("aacount2@localhost.com", "Sample Account",
                        "sampleaccount", "/9Se/pfHeUH8FJ4asBD6jQ==");

        AccountResource accountClient =
                getAccountResource("/accounts/u/sampleaccount",
                        AuthenticatedAsUser.Translator,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
        Response putResponse = accountClient.put(a);

        // Assert initial put
        assertThat(putResponse.getStatus()).isEqualTo(Status.FORBIDDEN.getStatusCode());
        putResponse.close();
    }

}
