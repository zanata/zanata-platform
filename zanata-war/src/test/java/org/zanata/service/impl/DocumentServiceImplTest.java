/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.service.impl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.model.type.WebhookType;
import org.zanata.i18n.Messages;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.WebHook;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationStateCache;
import org.zanata.ui.model.statistic.WordStatistic;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.zanata.util.UrlUtil;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DocumentServiceImplTest {

    @Mock
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Mock
    private TranslationStateCache translationStateCacheImpl;

    @Mock
    private Messages msgs;

    @Mock
    private UrlUtil urlUtil;

    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Mock
    private WebhookServiceImpl webhookService;

    private DocumentServiceImpl documentService;

    private Long docId = 1L, versionId = 1L;
    private LocaleId localeId = LocaleId.DE;
    private String docIdString = "documentId";
    private String projectSlug = "project-slug";
    private String versionSlug = "version-slug";

    private int milestone = DocumentService.DOC_EVENT_MILESTONE;

    List<WebHook> webHooks = Lists.newArrayList();

    private String testUrl = "http://localhost/test/doc/url";

    private String key = null;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        documentService = new DocumentServiceImpl();
        documentService.init(projectIterationDAO, documentDAO,
                translationStateCacheImpl, urlUtil, applicationConfiguration,
                msgs, webhookService);
        // TODO spy should only be needed for legacy code
        // avoid triggering HTTP requests in a unit test:
        doNothing().when(webhookService).processDocumentMilestone(
                any(String.class), any(String.class), any(String.class),
                any(LocaleId.class), any(String.class), any(String.class),
                any(List.class));

        HProjectIteration version = Mockito.mock(HProjectIteration.class);
        HProject project = Mockito.mock(HProject.class);
        HDocument document = Mockito.mock(HDocument.class);

        webHooks = Lists.newArrayList();
        Set<WebhookType> types =
            Sets.newHashSet(WebhookType.DocumentMilestoneEvent);
        webHooks.add(new WebHook(project, "http://test.example.com",
            types, key));
        webHooks.add(new WebHook(project, "http://test1.example.com",
            types, key));
        webHooks.add(new WebHook(project, "http://test1.example.com",
            Sets.newHashSet(WebhookType.DocumentStatsEvent), key));

        when(projectIterationDAO.findById(versionId)).thenReturn(version);
        when(version.getProject()).thenReturn(project);
        when(version.getSlug()).thenReturn(versionSlug);
        when(project.getSlug()).thenReturn(projectSlug);
        when(project.getWebHooks()).thenReturn(webHooks);
        when(documentDAO.getById(docId)).thenReturn(document);
        when(document.getDocId()).thenReturn(docIdString);

        when(urlUtil.fullEditorDocumentUrl(anyString(), anyString(),
                any(LocaleId.class), any(LocaleId.class), anyString()))
                .thenReturn(testUrl);
    }

    @Test
    public void documentMilestoneEventTranslatedTest() {
        WordStatistic stats = new WordStatistic(0, 0, 0, 10, 0);
        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);
        runDocumentStatisticUpdatedTest(documentService, ContentState.New,
                ContentState.Translated);

        String message = msgs.format("jsf.webhook.response.state", milestone,
                ContentState.Translated);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(webhookService).processDocumentMilestone(
            eq(projectSlug), eq(versionSlug), eq(docIdString),
            eq(localeId), eq(message), eq(testUrl), captor.capture());

        assertThat(captor.getValue().size(), is(2));
        assertThat(((WebHook) captor.getValue().get(0)).getTypes(),
            contains(WebhookType.DocumentMilestoneEvent));
        assertThat(((WebHook) captor.getValue().get(1)).getTypes(),
            contains(WebhookType.DocumentMilestoneEvent));
    }

    @Test
    public void documentMilestoneEventTranslatedNot100Test() {
        WordStatistic stats = new WordStatistic(0, 1, 0, 9, 0);
        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);
        runDocumentStatisticUpdatedTest(documentService, ContentState.New,
                ContentState.Translated);

        String message =  msgs.format("jsf.webhook.response.state", milestone,
            ContentState.Translated);

        verify(webhookService, never()).processDocumentMilestone(
            eq(projectSlug), eq(versionSlug), eq(docIdString),
            eq(localeId), eq(message), eq(testUrl), Mockito.anyList());
    }

    @Test
    public void documentMilestoneEventApprovedTest() {
        WordStatistic stats = new WordStatistic(10, 0, 0, 0, 0);
        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);
        runDocumentStatisticUpdatedTest(documentService, ContentState.Translated,
                ContentState.Approved);
        String message = msgs.format("jsf.webhook.response.state",
            milestone, ContentState.Approved);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);

        verify(webhookService).processDocumentMilestone(
            eq(projectSlug), eq(versionSlug), eq(docIdString),
            eq(localeId), eq(message), eq(testUrl), captor.capture());

        assertThat(captor.getValue().size(), is(2));
        assertThat(((WebHook) captor.getValue().get(0)).getTypes(),
            contains(WebhookType.DocumentMilestoneEvent));
        assertThat(((WebHook) captor.getValue().get(1)).getTypes(),
            contains(WebhookType.DocumentMilestoneEvent));
    }

    @Test
    public void documentMilestoneEventApprovedNot100Test() {
        WordStatistic stats = new WordStatistic(9, 0, 0, 1, 0);
        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);
        runDocumentStatisticUpdatedTest(documentService, ContentState.Translated,
                ContentState.Approved);
        String message = msgs.format(
            "jsf.webhook.response.state", milestone, ContentState.Approved);
        verify(webhookService, never()).processDocumentMilestone(
            eq(projectSlug), eq(versionSlug), eq(docIdString),
            eq(localeId), eq(message), eq(testUrl), Mockito.anyList());
    }

    @Test
    public void documentMilestoneEventSameStateTest1() {
        WordStatistic stats = new WordStatistic(10, 0, 0, 0, 0);
        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);

        runDocumentStatisticUpdatedTest(documentService, ContentState.Approved,
                ContentState.Approved);
        String message = msgs.format(
            "jsf.webhook.response.state", milestone, ContentState.Approved);
        verify(webhookService, never()).processDocumentMilestone(
                eq(projectSlug), eq(versionSlug), eq(docIdString),
                eq(localeId), eq(message), eq(testUrl), Mockito.anyList());
    }

    @Test
    public void documentMilestoneEventSameStateTest2() {
        WordStatistic stats = new WordStatistic(0, 0, 0, 10, 0);

        when(translationStateCacheImpl.getDocumentStatistics(docId, localeId))
            .thenReturn(stats);

        runDocumentStatisticUpdatedTest(documentService, ContentState.Translated,
            ContentState.Translated);

        String message = msgs.format(
            "jsf.webhook.response.state", milestone, ContentState.Translated);
        verify(webhookService, never()).processDocumentMilestone(
            eq(projectSlug), eq(versionSlug), eq(docIdString),
            eq(localeId), eq(message), eq(testUrl), Mockito.anyList());
    }

    private void runDocumentStatisticUpdatedTest(
            DocumentServiceImpl spyService,
            ContentState oldState, ContentState newState) {
        int wordCount = 10;
        DocumentLocaleKey key = new DocumentLocaleKey(docId, localeId);
        Map<ContentState, Long> map = new HashMap<>();

        Long oldStatCount = wordCount * -1L;
        map.put(oldState, oldStatCount);

        long newStatCount = wordCount + (map.get(newState) != null ? map.get(newState) : 0);
        map.put(newState, newStatCount);
        DocStatsEvent event =
                new DocStatsEvent(key, versionId, map, null);

        spyService.documentStatisticUpdated(event);
    }
}
