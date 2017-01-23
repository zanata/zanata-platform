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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import java.util.Arrays;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import static org.zanata.provider.DBUnitProvider.DataSetOperation;
import static org.zanata.util.RawRestTestUtils.assertJaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.assertJsonUnmarshal;
import static org.zanata.util.RawRestTestUtils.jaxbUnmarshal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

/**
 * @author Carlos Munoz
 *         <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class StatisticsRawRestITCase extends RestTest {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(StatisticsRawRestITCase.class);

    @Override
    protected void prepareDBUnitOperations() {
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/ProjectsData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/LocalesData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DataSetOperation(
                "org/zanata/test/model/TextFlowTestData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addBeforeTestOperation(new DBUnitProvider.DataSetOperation(
                "org/zanata/test/model/ApplicationConfigurationData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        addAfterTestOperation(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    @RunAsClient
    public void getIterationStatisticsXml() throws Exception {
        // Ok
        // No
        // detailed
        // stats
        // No word level stats
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl("/stats/proj/sample-project/iter/1.0"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("1.0"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats(), nullValue());
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit(),
                            is(TranslationStatistics.StatUnit.MESSAGE));
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getIterationStatisticsXmlWithDetails() throws Exception {
        // Ok
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl("/stats/proj/sample-project/iter/1.0"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
                request.queryParameter("word", "true")
                        .queryParameter("detail", "true")
                        .queryParameter("locale", "en-US")
                        .queryParameter("locale", "es");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("1.0"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats().size(), greaterThan(0));
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated(),
                            equalTo(transStat.getTotal()));
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales),
                            hasItem(transStat.getLocale()));
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsXml() throws Exception {
        // Ok
        // No
        // detailed
        // stats
        // No word level stats
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("my/path/document.txt"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats(), nullValue());
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit(),
                            is(TranslationStatistics.StatUnit.MESSAGE));
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsXmlWithDetails() throws Exception {
        // Ok
        // assertThat(stats.getDetailedStats().size(), greaterThan(0));
        // // No detailed stats (maybe later)
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
                request.queryParameter("word", "true")
                        .queryParameter("detail", "true")
                        .queryParameter("locale", "en-US")
                        .queryParameter("locale", "es");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("my/path/document.txt"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved(),
                            equalTo(transStat.getTotal()));
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales),
                            hasItem(transStat.getLocale()));
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getIterationStatisticsJson() throws Exception {
        // Ok
        // No
        // detailed
        // stats
        // No word level stats
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl("/stats/proj/sample-project/iter/1.0"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("1.0"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats(), nullValue());
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit(),
                            is(TranslationStatistics.StatUnit.MESSAGE));
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getIterationStatisticsJsonWithDetails() throws Exception {
        // Ok
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl("/stats/proj/sample-project/iter/1.0"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                request.queryParameter("word", "true")
                        .queryParameter("detail", "true")
                        .queryParameter("locale", "en-US")
                        .queryParameter("locale", "es");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("1.0"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats().size(), greaterThan(0));
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated(),
                            equalTo(transStat.getTotal()));
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales),
                            hasItem(transStat.getLocale()));
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsJson() throws Exception {
        // Ok
        // No
        // detailed
        // stats
        // No word level stats
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("my/path/document.txt"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getDetailedStats(), nullValue());
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit(),
                            is(TranslationStatistics.StatUnit.MESSAGE));
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsJsonWithDetails() throws Exception {
        // Ok
        // assertThat(stats.getDetailedStats().size(), greaterThan(0));
        // // No detailed stats (maybe later)
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected void prepareRequest(ClientRequest request) {
                request.header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
                request.queryParameter("word", "true")
                        .queryParameter("detail", "true")
                        .queryParameter("locale", "en-US")
                        .queryParameter("locale", "as")
                        .queryParameter("locale", "es");
            }

            @Override
            protected void onResponse(ClientResponse response) {
                assertThat(response.getStatus(), is(200));
                assertJsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(response,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId(), is("my/path/document.txt"));
                assertThat(stats.getRefs().size(), greaterThan(0));
                assertThat(stats.getStats().size(), greaterThan(0));
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly(),
                            equalTo(transStat.getTotal()));
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales),
                            hasItem(transStat.getLocale()));
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly(),
                            equalTo(transStat.getTotal()));
                }
            }
        }.run();
    }
}
