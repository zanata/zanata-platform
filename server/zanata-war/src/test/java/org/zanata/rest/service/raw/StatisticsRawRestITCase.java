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

import java.util.Arrays;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.RestTest;
import org.zanata.provider.DBUnitProvider;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import static org.assertj.core.api.Assertions.assertThat;
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);

                assertJaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("1.0");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats()).isNull();
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit())
                            .isEqualTo(TranslationStatistics.StatUnit.MESSAGE);
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved())
                            .isEqualTo(transStat.getTotal());
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("word", "true")
                        .queryParam("detail", "true")
                        .queryParam("locale", "en-US")
                        .queryParam("locale", "es").request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);

                assertJaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("1.0");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats().size()).isGreaterThan(0);
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated())
                            .isEqualTo(transStat.getTotal());
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales))
                            .contains(transStat.getLocale());
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved())
                            .isEqualTo(transStat.getTotal());
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
            protected Invocation.Builder prepareRequest(
                    ResteasyWebTarget webTarget) {
                return webTarget.request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("my/path/document.txt");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats()).isNull();
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit())
                            .isEqualTo(TranslationStatistics.StatUnit.MESSAGE);
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved())
                            .isEqualTo(transStat.getTotal());
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsXmlWithDetails() throws Exception {
        // Ok
        // assertThat(stats.getDetailedStats().size(), greaterThan(0));
        // No detailed stats (maybe later)
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("word", "true")
                        .queryParam("detail", "true")
                        .queryParam("locale", "en-US")
                        .queryParam("locale", "es").request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jaxbUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("my/path/document.txt");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved())
                            .isEqualTo(transStat.getTotal());
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales))
                            .contains(transStat.getLocale());
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated())
                            .isEqualTo(transStat.getTotal());
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(response.getStatus()).isEqualTo(200);
                ContainerTranslationStatistics stats = jsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("1.0");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats()).isNull();
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit())
                            .isEqualTo(TranslationStatistics.StatUnit.MESSAGE);
                    assertThat(
                            transStat.getUntranslated() + transStat.getDraft()
                                    + transStat.getTranslatedAndApproved())
                            .isEqualTo(transStat.getTotal());
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("word", "true")
                        .queryParam("detail", "true")
                        .queryParam("locale", "en-US")
                        .queryParam("locale", "es").request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);

                assertJsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("1.0");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats().size()).isGreaterThan(0);
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated())
                            .isEqualTo(transStat.getTotal());
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales))
                            .contains(transStat.getLocale());
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly())
                            .isEqualTo(transStat.getTotal());
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
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("my/path/document.txt");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getDetailedStats()).isNull();
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(transStat.getUnit())
                            .isEqualTo(TranslationStatistics.StatUnit.MESSAGE);
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly())
                            .isEqualTo(transStat.getTotal());
                }
            }
        }.run();
    }

    @Test
    @RunAsClient
    public void getDocumentStatisticsJsonWithDetails() throws Exception {
        // Ok
        // assertThat(stats.getDetailedStats().size(), greaterThan(0));
        // No detailed stats (maybe later)
        // make sure counts are sane
        // Results returned only for specified locales
        // make sure counts are sane
        new ResourceRequest(
                getRestEndpointUrl(
                        "/stats/proj/sample-project/iter/1.0/doc/my/path/document.txt"),
                "GET") {

            @Override
            protected Invocation.Builder prepareRequest(ResteasyWebTarget webTarget) {
                return webTarget.queryParam("word", "true")
                        .queryParam("detail", "true")
                        .queryParam("locale", "en-US")
                        .queryParam("locale", "as")
                        .queryParam("locale", "es").request()
                        .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            }

            @Override
            protected void onResponse(Response response) {
                assertThat(response.getStatus()).isEqualTo(200);
                String entityString = response.readEntity(String.class);
                assertJsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                ContainerTranslationStatistics stats = jsonUnmarshal(entityString,
                        ContainerTranslationStatistics.class);
                assertThat(stats.getId()).isEqualTo("my/path/document.txt");
                assertThat(stats.getRefs().size()).isGreaterThan(0);
                assertThat(stats.getStats().size()).isGreaterThan(0);
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly())
                            .isEqualTo(transStat.getTotal());
                }
                String[] expectedLocales = new String[] { "en-US", "as", "es" };
                for (TranslationStatistics transStat : stats.getStats()) {
                    assertThat(Arrays.asList(expectedLocales))
                            .contains(transStat.getLocale());
                    assertThat(
                            transStat.getDraft() + transStat.getApproved()
                                    + transStat.getUntranslated()
                                    + transStat.getTranslatedOnly())
                            .isEqualTo(transStat.getTotal());
                }
            }
        }.run();
    }
}
