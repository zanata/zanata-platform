/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands.pull;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.common.MinContentState;
import org.zanata.common.ProjectType;
import org.zanata.common.TransUnitCount;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.client.SourceDocResourceClient;
import org.zanata.rest.client.StatisticsResourceClient;
import org.zanata.rest.client.TransDocResourceClient;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;

import com.google.common.collect.Lists;

public class PullCommandTest {
    public static final StringSet EXTENSIONS = new StringSet("comment");
    @Mock
    private RestClientFactory restClientFactory;
    private PullOptionsImpl opts;
    private final String projectSlug = "project";
    private final String versionSlug = "master";
    @Mock
    private SourceDocResourceClient sourceClient;
    @Mock
    private TransDocResourceClient transClient;
    @Mock
    private StatisticsResourceClient statsClient;
    private LocaleList locales;
    private PullCommand pullCommand;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        opts = new PullOptionsImpl();
        opts.setProj(projectSlug);
        opts.setProjectVersion(versionSlug);
        opts.setProjectType(ProjectType.Properties.name().toLowerCase());
        opts.setBatchMode(true);

        when(
                restClientFactory.getSourceDocResourceClient(projectSlug,
                        versionSlug)).thenReturn(
                sourceClient);
        when(restClientFactory.getTransDocResourceClient(projectSlug,
                versionSlug)).thenReturn(transClient);
        when(restClientFactory.getStatisticsClient()).thenReturn(statsClient);

        locales = new LocaleList();
        opts.setLocaleMapList(locales);
        pullCommand = new PullCommand(opts, restClientFactory);
    }

    @Test
    public void pullSourceOnlyWillIgnoreMinimumPercent() throws Exception {
        locales.add(new LocaleMapping("zh"));
        locales.add(new LocaleMapping("de"));
        opts.setDryRun(true);
        // Given: pull-type is source only and minimum-doc-percent is set to 80
        opts.setPullType("source");
        opts.setMinDocPercent(80);
        when(sourceClient
                .getResource("file1", EXTENSIONS)).thenReturn(
                new Resource());
        pullCommand = new PullCommand(opts, restClientFactory) {
            @Override
            protected List<String>
                    getQualifiedDocNamesForCurrentModuleFromServer() {
                return Lists.newArrayList("file1");
            }
        };

        // When:
        pullCommand.run();

        // Then:
        verifyZeroInteractions(statsClient, transClient);
    }

    @Test
    public void pullTransOnlyWillIgnoreMinimumPercentIfItIsZero()
            throws Exception {
        locales.add(new LocaleMapping("zh"));
        locales.add(new LocaleMapping("de"));
        opts.setDryRun(true);
        // Given: pull-type is trans only and minimum-doc-percent is set to 0
        opts.setPullType("trans");
        opts.setMinDocPercent(0);

        pullCommand = new PullCommand(opts, restClientFactory) {
            @Override
            protected List<String>
                    getQualifiedDocNamesForCurrentModuleFromServer() {
                return Lists.newArrayList("file1");
            }

            @Override
            protected void pullDocForLocale(PullStrategy strat, Resource doc,
                    String localDocName, String docUri,
                    boolean createSkeletons,
                    LocaleMapping locMapping, MinContentState minContentState,
                                            File transFile)
                    throws IOException {
                // pretend we are pulling
                transClient.getTranslations(docUri,
                        new LocaleId(locMapping.getLocale()), EXTENSIONS,
                        createSkeletons, minContentState, null);
            }
        };

        // When:
        pullCommand.run();

        // Then:
        verifyZeroInteractions(statsClient);
        verify(transClient).getTranslations("file1", new LocaleId("zh"),
                EXTENSIONS, false, MinContentState.Translated
                , null);
        verify(transClient).getTranslations("file1", new LocaleId("de"),
                EXTENSIONS, false, MinContentState.Translated
                , null);
    }

    @Test
    public void pullTransOnlyWillUseMinimumPercentIfItIsNotZero()
            throws Exception {
        locales.add(new LocaleMapping("zh"));
        locales.add(new LocaleMapping("de"));
        opts.setDryRun(true);
        // Given: pull-type is trans only and minimum-doc-percent is set to 80
        opts.setPullType("trans");
        opts.setMinDocPercent(80);

        ContainerTranslationStatistics statistics =
                new ContainerTranslationStatistics();
        ContainerTranslationStatistics docStats =
                new ContainerTranslationStatistics();
        docStats.setId("file1");
        statistics.addDetailedStats(docStats);
        // zh has 100 approved
        TranslationStatistics zhLocaleStats =
                new TranslationStatistics(new TransUnitCount(100, 0, 0, 0, 0),
                        "zh");
        // de has 39 approved, 21 fuzzy and 40 translated so 79% translated
        TranslationStatistics deLocaleStats =
                new TranslationStatistics(new TransUnitCount(39, 21, 0, 40, 0),
                        "de");
        docStats.addStats(zhLocaleStats);
        docStats.addStats(deLocaleStats);

        when(statsClient
                .getStatistics(projectSlug, versionSlug, true, false, new String[] {"zh", "de"}))
                .thenReturn(statistics);

        pullCommand = new PullCommand(opts, restClientFactory) {
            @Override
            protected List<String>
                    getQualifiedDocNamesForCurrentModuleFromServer() {
                return Lists.newArrayList("file1");
            }

            @Override
            protected void pullDocForLocale(PullStrategy strat, Resource doc,
                    String localDocName, String docUri,
                    boolean createSkeletons,
                    LocaleMapping locMapping, MinContentState minContentState,
                                            File transFile)
                    throws IOException {
                // pretend we are pulling
                transClient.getTranslations(docUri,
                        new LocaleId(locMapping.getLocale()), EXTENSIONS,
                        createSkeletons, minContentState, null);
            }
        };

        // When:
        pullCommand.run();

        // Then: translation for "de" will not be pulled
        verify(statsClient).getStatistics(projectSlug, versionSlug, true,
                false, new String[] {"zh", "de"});
        verify(transClient).getTranslations("file1", new LocaleId("zh"),
                EXTENSIONS, false,
                MinContentState.Translated, null);
        verifyNoMoreInteractions(transClient);
    }

    @Test
    public void whenMinimumPercentIsSetTo100ItWillUseTotalNumber()
            throws Exception {
        locales.add(new LocaleMapping("zh"));
        locales.add(new LocaleMapping("de"));
        opts.setDryRun(true);
        // Given: pull-type is trans only and minimum-doc-percent is set to 100
        opts.setPullType("trans");
        opts.setMinDocPercent(100);

        ContainerTranslationStatistics statistics =
                new ContainerTranslationStatistics();
        ContainerTranslationStatistics docStats =
                new ContainerTranslationStatistics();
        docStats.setId("file1");
        statistics.addDetailedStats(docStats);
        // zh has 100000 approved
        TranslationStatistics zhLocaleStats =
                new TranslationStatistics(new TransUnitCount(100000, 0, 0, 0, 0),
                        "zh");
        // de has 99999 approved, 1 untranslated so 99.999% translated
        TranslationStatistics deLocaleStats =
                new TranslationStatistics(new TransUnitCount(99999, 0, 1, 0, 0),
                        "de");
        docStats.addStats(zhLocaleStats);
        docStats.addStats(deLocaleStats);

        when(statsClient
                .getStatistics(projectSlug, versionSlug, true, false, new String[] {"zh", "de"}))
                .thenReturn(statistics);

        pullCommand = new PullCommand(opts, restClientFactory) {
            @Override
            protected List<String>
            getQualifiedDocNamesForCurrentModuleFromServer() {
                return Lists.newArrayList("file1");
            }

            @Override
            protected void pullDocForLocale(PullStrategy strat, Resource doc,
                    String localDocName, String docUri,
                    boolean createSkeletons,
                    LocaleMapping locMapping, MinContentState minContentState,
                                            File transFile)
                    throws IOException {
                // pretend we are pulling
                transClient.getTranslations(docUri,
                        new LocaleId(locMapping.getLocale()), EXTENSIONS,
                        createSkeletons, minContentState, null);
            }
        };

        // When:
        pullCommand.run();

        // Then: translation for "de" will not be pulled
        verify(statsClient).getStatistics(projectSlug, versionSlug, true,
                false, new String[] {"zh", "de"});
        verify(transClient).getTranslations("file1", new LocaleId("zh"),
                EXTENSIONS, false,
                MinContentState.Translated, null);
        verifyNoMoreInteractions(transClient);
    }

}
