/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;

import javax.ws.rs.core.MediaType;

import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Before;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.service.ResourceTestObjectFactory;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CopyTransRestITCase extends RestTest {

    private Resource resource;

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Before
    public void setUp() throws Exception {
        // push a fresh new document because copyTrans is async and may affect other tests
        resource = new ResourceTestObjectFactory().getTextFlowTest();
        new ResourceRequest(
                getRestEndpointUrl("projects/p/sample-project/iterations/i/1.0/r/"),
                "POST", getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(
                    ClientRequest request) {
                request.queryParameter("copyTrans", Boolean.FALSE);
                request.body(MediaType.APPLICATION_XML_TYPE,
                        jaxbMarhsal(resource));
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isEqualTo(201);
                response.releaseConnection();
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void startCopyTrans() throws Exception {

        new ResourceRequest(
                getRestEndpointUrl(
                        String.format("copytrans/proj/%s/iter/%s/doc/%s",
                                "sample-project", "1.0",
                                resource.getName())),
                        "POST", getAuthorizedEnvironment()) {


            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isEqualTo(200);
                assertJaxbUnmarshal(response, CopyTransStatus.class);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void copyTransForUnknownDocument() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl(
                String.format("copytrans/proj/%s/iter/%s/doc/%s",
                        "sample-project", "1.0",
                        "/an/inexisting/document.txt")),
                "POST", getAuthorizedEnvironment()) {

            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isEqualTo(404);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedStartCopyTrans() throws Exception {

        new ResourceRequest(
                getRestEndpointUrl(
                String.format("copytrans/proj/%s/iter/%s/doc/%s",
                        "sample-project", "1.0",
                        resource.getName())),
                "POST") {

            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isEqualTo(401);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedCopyTransStatus() throws Exception {
        new ResourceRequest(
                getRestEndpointUrl(
                String.format("copytrans/proj/%s/iter/%s/doc/%s",
                        "sample-project", "1.0",
                        resource.getName())),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                Assertions.assertThat(response.getStatus()).isEqualTo(401);
            }
        }.run();
    }

}
