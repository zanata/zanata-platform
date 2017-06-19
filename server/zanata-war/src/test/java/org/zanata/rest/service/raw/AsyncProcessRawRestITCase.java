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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.rest.ResourceRequest;

public class AsyncProcessRawRestITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void notAdminCanNotGetAllProcessStatuses() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/process"), "GET",
                getTranslatorHeaders()) {
            @Override
            protected Invocation.Builder
                    prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("includeFinished", true).request();
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(401));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void adminCanGetAllProcessStatuses() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/process"), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder
                    prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("includeFinished", true).request();
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus(), is(200));
            }
        }.run();
    }

}
