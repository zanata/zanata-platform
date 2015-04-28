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

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.apicompat.rest.service.AccountResource;
import org.zanata.rest.ResourceRequest;
import org.zanata.apicompat.rest.MediaTypes;
import org.zanata.apicompat.rest.dto.Account;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
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
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJsonUnmarshal(response, Account.class);
                Account account = jsonUnmarshal(response, Account.class);

                // Assert correct parsing of all properties
                assertThat(account.getUsername(), is("demo"));
                assertThat(account.getApiKey(),
                        is("23456789012345678901234567890123"));
                assertThat(account.getEmail(), is("user1@localhost"));
                assertThat(account.getName(), is("Sample User"));
                assertThat(account.getPasswordHash(),
                        is("/9Se/pfHeUH8FJ4asBD6jQ=="));
                assertThat(account.getRoles().size(), is(1));
                // assertThat(account.getTribes().size(), is(1)); // Language
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
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
                assertJaxbUnmarshal(response, Account.class);
                Account account = jaxbUnmarshal(response, Account.class);

                // Assert correct parsing of all properties
                assertThat(account.getUsername(), is("demo"));
                assertThat(account.getApiKey(),
                        is("23456789012345678901234567890123"));
                assertThat(account.getEmail(), is("user1@localhost"));
                assertThat(account.getName(), is("Sample User"));
                assertThat(account.getPasswordHash(),
                        is("/9Se/pfHeUH8FJ4asBD6jQ=="));
                assertThat(account.getRoles().size(), is(1));
                // assertThat(account.getTribes().size(), is(1)); // Language
                // teams are not being returned
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAccountJsonUnauthorized() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/accounts/u/demo"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_ACCOUNT_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(),
                        is(Status.UNAUTHORIZED.getStatusCode()));
                response.releaseConnection();
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
        assertThat(putResponse.getStatus(), is(Status.CREATED.getStatusCode()));
        releaseConnection(putResponse);

        // Modified Account
        a.setName("New Account Name");
        putResponse = accountClient.put(a);
        releaseConnection(putResponse);

        // Assert modification
        assertThat(putResponse.getStatus(), is(Status.OK.getStatusCode()));

        // Retrieve again
        Response response = accountClient.get();
        Account a2 = jsonUnmarshal((ClientResponse) response, Account.class);
        assertThat(a2.getUsername(), is(a.getUsername()));
        assertThat(a2.getApiKey(), is(a.getApiKey()));
        assertThat(a2.getEmail(), is(a.getEmail()));
        assertThat(a2.getName(), is(a.getName()));
        assertThat(a2.getPasswordHash(), is(a.getPasswordHash()));
        assertThat(a2.getRoles().size(), is(0));
        // assertThat(a2.getTribes().size(), is(1)); // Language teams are not
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
        assertThat(putResponse.getStatus(), is(Status.CREATED.getStatusCode()));
        releaseConnection(putResponse);

        // Modified Account
        a.setName("New Account Name");
        putResponse = accountClient.put(a);
        releaseConnection(putResponse);

        // Assert modification
        assertThat(putResponse.getStatus(), is(Status.OK.getStatusCode()));

        // Retrieve again
        Response response = accountClient.get();
        Account a2 = jaxbUnmarshal((ClientResponse) response, Account.class);
        assertThat(a2.getUsername(), is(a.getUsername()));
        assertThat(a2.getApiKey(), is(a.getApiKey()));
        assertThat(a2.getEmail(), is(a.getEmail()));
        assertThat(a2.getName(), is(a.getName()));
        assertThat(a2.getPasswordHash(), is(a.getPasswordHash()));
        assertThat(a2.getRoles().size(), is(0));
        // assertThat(a2.getTribes().size(), is(1)); // Language teams are not
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
        assertThat(putResponse.getStatus(), is(Status.UNAUTHORIZED.getStatusCode()));
        releaseConnection(putResponse);
    }

}
