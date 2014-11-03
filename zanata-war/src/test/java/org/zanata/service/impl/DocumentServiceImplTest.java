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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.events.DocumentMilestoneEvent;
import org.zanata.events.DocumentStatisticUpdatedEvent;
import org.zanata.i18n.Messages;
import org.zanata.model.HDocument;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.WebHook;
import org.zanata.service.DocumentService;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class DocumentServiceImplTest {

    @Mock
    private ProjectIterationDAO projectIterationDAO;

    @Mock
    private DocumentDAO documentDAO;

    @Mock
    private Messages msgs;

    private DocumentServiceImpl documentService;

    private Long docId = 1L, versionId = 1L, tfId = 1L;
    private LocaleId localeId = LocaleId.DE;
    private String docIdString = "documentId";
    private String projectSlug = "project-slug";
    private String versionSlug = "version-slug";

    private int milestone = DocumentService.DOC_EVENT_MILESTONE;

    List<WebHook> webHooks = Lists.newArrayList();

    @BeforeMethod(firstTimeOnly = true)
    public void setup() {
        MockitoAnnotations.initMocks(this);
        documentService = new DocumentServiceImpl();
        documentService.init(projectIterationDAO, documentDAO, msgs);

        HProjectIteration version = Mockito.mock(HProjectIteration.class);
        HProject project = Mockito.mock(HProject.class);
        HDocument document = Mockito.mock(HDocument.class);

        webHooks = Lists.newArrayList();
        webHooks.add(new WebHook(project, "http://test.com"));
        webHooks.add(new WebHook(project, "http://test1.com"));

        when(projectIterationDAO.findById(versionId)).thenReturn(version);
        when(version.getProject()).thenReturn(project);
        when(version.getSlug()).thenReturn(versionSlug);
        when(project.getSlug()).thenReturn(projectSlug);
        when(project.getWebHooks()).thenReturn(webHooks);
        when(documentDAO.getById(docId)).thenReturn(document);
        when(document.getDocId()).thenReturn(docIdString);

        when(msgs.format(anyString())).thenReturn("test message");
    }

    @Test
    public void documentMilestoneEventTranslatedTest() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);

        WordStatistic stats = new WordStatistic(0, 0, 0, 10, 0);
        runDocumentStatisticUpdatedTest(spyService, ContentState.New,
                ContentState.Translated, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug, versionSlug,
                        docIdString, localeId,
                        msgs.format("jsf.webhook.response.state", milestone,
                                ContentState.Translated));

        verify(spyService).publishDocumentMilestoneEvent(webHooks.get(0),
                milestoneEvent);
        verify(spyService).publishDocumentMilestoneEvent(webHooks.get(1),
                milestoneEvent);
    }

    @Test
    public void documentMilestoneEventTranslatedNot100Test() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);

        WordStatistic stats = new WordStatistic(0, 1, 0, 9, 0);
        runDocumentStatisticUpdatedTest(spyService, ContentState.New,
                ContentState.Translated, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug, versionSlug,
                        docIdString, localeId,
                        msgs.format("jsf.webhook.response.state", milestone,
                                ContentState.Translated));

        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(0), milestoneEvent);
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(1), milestoneEvent);
    }

    @Test
    public void documentMilestoneEventApprovedTest() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);

        WordStatistic stats = new WordStatistic(10, 0, 0, 0, 0);
        runDocumentStatisticUpdatedTest(spyService, ContentState.Translated,
                ContentState.Approved, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug,
                        versionSlug, docIdString,
                        localeId, msgs.format("jsf.webhook.response.state",
                                milestone, ContentState.Approved));
        verify(spyService).publishDocumentMilestoneEvent(webHooks.get(0),
                milestoneEvent);
        verify(spyService).publishDocumentMilestoneEvent(webHooks.get(1),
                milestoneEvent);
    }

    @Test
    public void documentMilestoneEventApprovedNot100Test() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);

        WordStatistic stats = new WordStatistic(9, 0, 0, 1, 0);
        runDocumentStatisticUpdatedTest(spyService, ContentState.Translated,
                ContentState.Approved, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug, versionSlug,
                        docIdString, localeId, msgs.format(
                                "jsf.webhook.response.state", milestone,
                                ContentState.Approved));

        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(0), milestoneEvent);
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(1), milestoneEvent);
    }

    @Test
    public void documentMilestoneEventSameStateTest1() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);
        WordStatistic stats = new WordStatistic(10, 0, 0, 0, 0);

        runDocumentStatisticUpdatedTest(spyService, ContentState.Approved,
                ContentState.Approved, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug, versionSlug,
                        docIdString, localeId, msgs.format(
                                "jsf.webhook.response.state", milestone,
                                ContentState.Approved));
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(0), milestoneEvent);
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(1), milestoneEvent);
    }

    @Test
    public void documentMilestoneEventSameStateTest2() {
        DocumentServiceImpl spyService = Mockito.spy(documentService);
        WordStatistic stats = new WordStatistic(0, 0, 0, 10, 0);

        runDocumentStatisticUpdatedTest(spyService, ContentState.Translated,
                ContentState.Translated, stats);

        DocumentMilestoneEvent milestoneEvent =
                new DocumentMilestoneEvent(projectSlug, versionSlug,
                        docIdString, localeId, msgs.format(
                                "jsf.webhook.response.state", milestone,
                                ContentState.Translated));
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(0), milestoneEvent);
        verify(spyService, never()).publishDocumentMilestoneEvent(
                webHooks.get(1), milestoneEvent);
    }

    private void runDocumentStatisticUpdatedTest(
            DocumentServiceImpl spyService,
            ContentState oldState, ContentState newState, WordStatistic stats) {

        int wordCount = 10;
        WordStatistic oldStats = StatisticsUtil.copyWordStatistic(stats);
        oldStats.decrement(newState, wordCount);
        oldStats.increment(oldState, wordCount);

        DocumentStatisticUpdatedEvent event =
                new DocumentStatisticUpdatedEvent(oldStats, stats, versionId,
                        docId, localeId, oldState, newState);

        spyService.documentStatisticUpdated(event);
    }
}
