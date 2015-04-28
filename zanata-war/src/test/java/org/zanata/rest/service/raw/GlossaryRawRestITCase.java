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

import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.HttpHeaders;

import org.assertj.core.api.Assertions;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.common.LocaleId;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.Glossary;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

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
    public void xmlGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response, Glossary.class);

                Glossary glossary = jaxbUnmarshal(response, Glossary.class);
                assertThat(glossary.getGlossaryEntries().size(), is(1));

                // Glossary Entry
                GlossaryEntry entry = glossary.getGlossaryEntries().get(0);
                assertThat(entry.getSourcereference(), is("source reference"));
                assertThat(entry.getSrcLang(), is(LocaleId.EN_US));
                assertThat(entry.getGlossaryTerms().size(), is(3));

                // Expected / Actual glossary terms
                Set<GlossaryTerm> actualTerms =
                        new HashSet<GlossaryTerm>(entry.getGlossaryTerms());

                Set<GlossaryTerm> expectedTerms = new HashSet<GlossaryTerm>();
                GlossaryTerm expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.EN_US);
                expTerm.setContent("test data content 1 (source lang)");
                expTerm.getComments().add("test data comment 1");
                expectedTerms.add(expTerm);

                expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.DE);
                expTerm.setContent("test data content 2");
                expTerm.getComments().add("test data comment 2");
                expectedTerms.add(expTerm);

                expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.ES);
                expTerm.setContent("test data content 3");
                expTerm.getComments().add("test data comment 3");
                expectedTerms.add(expTerm);

                assertThat(actualTerms, is(expectedTerms));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveGlossaryTermWithLocale() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary/de"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response, Glossary.class);

                Glossary glossary = jaxbUnmarshal(response, Glossary.class);
                assertThat(glossary.getGlossaryEntries().size(), is(1));

                // Glossary Entry
                GlossaryEntry entry = glossary.getGlossaryEntries().get(0);
                assertThat(entry.getSourcereference(), is("source reference"));
                assertThat(entry.getSrcLang(), is(LocaleId.EN_US));
                assertThat(entry.getGlossaryTerms().size(), is(1));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void retrieveNonExistingGlossaryTermLocale() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary/fr"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response, Glossary.class);

                Glossary glossary = jaxbUnmarshal(response, Glossary.class);
                assertThat(glossary.getGlossaryEntries().size(), is(0));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void jsonGet() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJsonUnmarshal(response, Glossary.class);

                Glossary glossary = jsonUnmarshal(response, Glossary.class);
                assertThat(glossary.getGlossaryEntries().size(), is(1));

                // Glossary Entry
                GlossaryEntry entry = glossary.getGlossaryEntries().get(0);
                assertThat(entry.getSourcereference(), is("source reference"));
                assertThat(entry.getSrcLang(), is(LocaleId.EN_US));
                assertThat(entry.getGlossaryTerms().size(), is(3));

                // Expected / Actual glossary terms
                Set<GlossaryTerm> actualTerms =
                        new HashSet<GlossaryTerm>(entry.getGlossaryTerms());

                Set<GlossaryTerm> expectedTerms = new HashSet<GlossaryTerm>();
                GlossaryTerm expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.EN_US);
                expTerm.setContent("test data content 1 (source lang)");
                expTerm.getComments().add("test data comment 1");
                expectedTerms.add(expTerm);

                expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.DE);
                expTerm.setContent("test data content 2");
                expTerm.getComments().add("test data comment 2");
                expectedTerms.add(expTerm);

                expTerm = new GlossaryTerm();
                expTerm.setLocale(LocaleId.ES);
                expTerm.setContent("test data content 3");
                expTerm.getComments().add("test data comment 3");
                expectedTerms.add(expTerm);

                assertThat(actualTerms, is(expectedTerms));
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void unauthorizedDelete() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary"), "DELETE") {
            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(401)); // Unauthorized
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void putXml() throws Exception {
        final Glossary glossary = this.getSampleGlossary();

        new ResourceRequest(getRestEndpointUrl("/glossary"), "PUT",
                getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.body(MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML,
                        jaxbMarhsal(glossary).getBytes());
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(201)); // Created
            }
        }.run();
    }

    /*
     * @Test public void putJson() throws Exception { final Glossary glossary =
     * this.getSampleGlossary();
     *
     * new ResourceRequest(sharedEnvironment, Method.PUT, "/restv1/glossary") {
     *
     * @Override protected void prepareRequest(ClientRequest request) {
     * request.setContentType(MediaTypes.APPLICATION_ZANATA_GLOSSARY_JSON);
     * request.setContent( jsonMarshal(glossary).getBytes() ); }
     *
     * @Override protected void onResponse(ClientResponse response) {
     * assertThat(response.getStatus(), is(201)); // Created } }.run(); }
     */

    @Test
    @RunAsClient
    public void delete() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary"), "DELETE",
                getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void deleteGlossaryTermWithLocale() throws Exception {
        new ResourceRequest(getRestEndpointUrl("/glossary/es"), "DELETE",
                getAuthorizedEnvironment()) {
            @Override
            protected void prepareRequest(ClientRequest request) {
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200)); // Ok
            }
        }.run();

        new ResourceRequest(getRestEndpointUrl("/glossary"), "GET") {
            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT,
                        MediaTypes.APPLICATION_ZANATA_GLOSSARY_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {

                Glossary glossary = jaxbUnmarshal(response, Glossary.class);
                Assertions.assertThat(glossary.getGlossaryEntries()).hasSize(1);
                Assertions
                        .assertThat(
                                glossary.getGlossaryEntries().get(0)
                                        .getGlossaryTerms()).hasSize(2);
            }
        }.run();
    }

    private Glossary getSampleGlossary() {
        Glossary glossary = new Glossary();
        GlossaryEntry glossaryEntry1 = new GlossaryEntry();
        glossaryEntry1.setSrcLang(LocaleId.EN_US);
        glossaryEntry1.setSourcereference("TEST SOURCE REF DATA");

        GlossaryTerm glossaryTerm1 = new GlossaryTerm();
        glossaryTerm1.setLocale(LocaleId.EN_US);
        glossaryTerm1.setContent("TEST DATA 1 EN_US");
        glossaryTerm1.getComments().add("COMMENT 1");

        GlossaryTerm glossaryTerm2 = new GlossaryTerm();
        glossaryTerm2.setLocale(LocaleId.DE);
        glossaryTerm2.setContent("TEST DATA 2 DE");
        glossaryTerm2.getComments().add("COMMENT 2");

        glossaryEntry1.getGlossaryTerms().add(glossaryTerm1);
        glossaryEntry1.getGlossaryTerms().add(glossaryTerm2);

        GlossaryEntry glossaryEntry2 = new GlossaryEntry();
        glossaryEntry2.setSrcLang(LocaleId.EN_US);
        glossaryEntry2.setSourcereference("TEST SOURCE REF DATA2");

        GlossaryTerm glossaryTerm3 = new GlossaryTerm();
        glossaryTerm3.setLocale(LocaleId.EN_US);
        glossaryTerm3.setContent("TEST DATA 3 EN_US");
        glossaryTerm3.getComments().add("COMMENT 3");

        GlossaryTerm glossaryTerm4 = new GlossaryTerm();
        glossaryTerm4.setLocale(LocaleId.DE);
        glossaryTerm4.setContent("TEST DATA 4 DE");
        glossaryTerm4.getComments().add("COMMENT 4");

        glossaryEntry2.getGlossaryTerms().add(glossaryTerm3);
        glossaryEntry2.getGlossaryTerms().add(glossaryTerm4);

        glossary.getGlossaryEntries().add(glossaryEntry1);
        glossary.getGlossaryEntries().add(glossaryEntry2);

        return glossary;
    }

}
