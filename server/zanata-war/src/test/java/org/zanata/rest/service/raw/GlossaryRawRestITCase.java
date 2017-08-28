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

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.GlossaryInfo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;

public class GlossaryRawRestITCase extends RestTest {

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/GlossaryData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    public void getInfo() throws Exception {
        String url = "/glossary/info";
        new ResourceRequest(getRestEndpointUrl(url), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString, GlossaryInfo.class);

                GlossaryInfo glossaryInfo =
                        jaxbUnmarshal(entityString, GlossaryInfo.class);

                assertThat(
                        glossaryInfo.getSrcLocale().getLocale().getLocaleId())
                                .isEqualTo(LocaleId.EN_US);
                assertThat(
                        glossaryInfo.getSrcLocale().getNumberOfTerms())
                                .isEqualTo(3);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getEntriesForLocale() throws Exception {
        LocaleId srcLocale = LocaleId.EN_US;
        LocaleId transLocale = LocaleId.DE;
        String url =
                "/glossary/entries?srcLocale=" + srcLocale.toString()
                        + "&transLocale=" + transLocale.toString();

        new ResourceRequest(getRestEndpointUrl(url), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                    MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getAllEntries() throws Exception {
        LocaleId srcLocale = LocaleId.EN_US;
        String url = "/glossary/entries?srcLocale=" + srcLocale.toString();

        new ResourceRequest(getRestEndpointUrl(url), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveNonExistingGlossarySrcLocale() throws Exception {
        LocaleId srcLocale = LocaleId.FR;
        String url = "/glossary/entries?srcLocale=" + srcLocale.toString();

        new ResourceRequest(getRestEndpointUrl(url), "GET",
                getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request().header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void deleteEntry() throws Exception {
        String id = "1";
        String url = "/glossary/entries/" + id;

        new ResourceRequest(getRestEndpointUrl(url), "DELETE",
            getAuthorizedEnvironment()) {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            protected void onResponse(Response response) {
                // OK
                assertThat(response.getStatus()).isEqualTo(200);
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedDeleteEntry() throws Exception {
        String id = "1";
        String url = "/glossary/entries/" + id;

        new ResourceRequest(getRestEndpointUrl(url), "DELETE") {
            @Override
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request();
            }

            @Override
            protected void onResponse(Response response) {
                // Unauthorized
                assertThat(response.getStatus()).isEqualTo(401);
            }
        }.run();
    }
}
