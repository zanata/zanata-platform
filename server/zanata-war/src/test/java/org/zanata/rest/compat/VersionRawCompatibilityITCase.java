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

import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.apicompat.rest.dto.VersionInfo;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;

public class VersionRawCompatibilityITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));

        addAfterTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    /**
     * JSON is the default return type if no header is passed to REST service.
     * @see org.zanata.rest.service.VersionResource
     */
    @Test
    @RunAsClient
    public void getVersionXml() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/version"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.accept("application/vnd.zanata.Version+xml");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertJaxbUnmarshal(response, VersionInfo.class);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getVersionJson() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/version"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.accept("application/vnd.zanata.Version+json");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertJsonUnmarshal(response, VersionInfo.class);
            }
        }.run();
    }
}
