/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.rest.service.raw;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.rest.ResourceRequest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;

public class TokenServiceRestITCase extends RestTest {

    private final String invalidAPI = "InvalidAPIKEY";

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void anonymousCanRegisterTokenEvenWithPrivateServer() throws Exception {
        // update system configuration to allow anonymous user
        new ResourceRequest(
                getRestEndpointUrl("/configurations/c/allow.anonymous.user"),
                "PUT", getAuthorizedEnvironment()) {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaType.APPLICATION_JSON_TYPE, "false");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), greaterThan(200));
            }
        }.run();
        new ResourceRequest(getRestEndpointUrl("/oauth/token"), "POST") {
            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isNotEqualTo(Status.UNAUTHORIZED);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void anonymousCanRefreshTokenEvenWithPrivateServer() throws Exception {
        // update system configuration to allow anonymous user
        new ResourceRequest(
                getRestEndpointUrl("/configurations/c/allow.anonymous.user"),
                "PUT", getAuthorizedEnvironment()) {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaType.APPLICATION_JSON_TYPE, "false");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), greaterThan(200));
            }
        }.run();
        new ResourceRequest(getRestEndpointUrl("/oauth/token/refresh"), "POST") {
            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isNotEqualTo(Status.UNAUTHORIZED);
            }
        }.run();
    }

}
