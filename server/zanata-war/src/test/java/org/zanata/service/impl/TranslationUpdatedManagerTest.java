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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.DocumentDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.model.type.WebhookType;
import org.zanata.webhook.events.DocumentStatsEvent;
import org.zanata.model.HAccount;
import org.zanata.model.HDocument;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.WebHook;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import com.google.common.collect.Lists;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TranslationUpdatedManagerTest {

    @Mock
    private DocumentDAO documentDAO;

    @Mock
    private TextFlowTargetDAO textFlowTargetDAO;

    TranslationUpdatedManager manager;

    List<WebHook> webHooks = Lists.newArrayList();

    private String key = null;

    private String strDocId = "doc/test.txt";
    private Long docId = 1L;
    private Long tfId = 1L;
    private Long tftId = 1L;
    private Long versionId = 1L;
    private Long personId = 1L;
    private LocaleId localeId = LocaleId.DE;
    private String versionSlug = "versionSlug";
    private String projectSlug = "projectSlug";
    private int wordCount = 10;
    private ContentState oldState = ContentState.New;
    private ContentState newState = ContentState.Translated;
    private String username = "username1";

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        manager = new TranslationUpdatedManager();
        manager.init(documentDAO, textFlowTargetDAO);

        HProjectIteration version = Mockito.mock(HProjectIteration.class);
        HProject project = Mockito.mock(HProject.class);
        HDocument document = Mockito.mock(HDocument.class);
        HPerson person = Mockito.mock(HPerson.class);
        HAccount account = Mockito.mock(HAccount.class);
        HTextFlowTarget target = Mockito.mock(HTextFlowTarget.class);

        webHooks = Lists
                .newArrayList(new WebHook(project, "http://test.example.com",
                        WebhookType.DocumentMilestoneEvent, key),
                        new WebHook(project, "http://test.example.com",
                                WebhookType.DocumentStatsEvent, key));

        when(person.getAccount()).thenReturn(account);
        when(account.getUsername()).thenReturn(username);
        when(documentDAO.findById(docId)).thenReturn(document);
        when(document.getDocId()).thenReturn(strDocId);
        when(document.getProjectIteration()).thenReturn(version);
        when(version.getSlug()).thenReturn(versionSlug);
        when(version.getProject()).thenReturn(project);
        when(project.getSlug()).thenReturn(projectSlug);
        when(project.getWebHooks()).thenReturn(webHooks);
        when(textFlowTargetDAO.findById(tftId)).thenReturn(target);
        when(target.getLastModifiedBy()).thenReturn(person);
    }

    @Test
    public void onDocStatUpdateTest() {
        TranslationUpdatedManager spyManager = Mockito.spy(manager);

        WordStatistic stats = new WordStatistic(10, 10, 10, 10, 10);
        WordStatistic oldStats = StatisticsUtil.copyWordStatistic(stats);
        oldStats.decrement(newState, wordCount);
        oldStats.increment(oldState, wordCount);

        DocumentLocaleKey key =
                new DocumentLocaleKey(docId, localeId);

        Map<ContentState, Long> contentStates = new HashMap<>();
        Long longWordCount = new Long(wordCount);
        contentStates.put(newState, longWordCount);
        contentStates.put(oldState, longWordCount);

        DocStatsEvent event =
                new DocStatsEvent(key, versionId, contentStates, tftId);

        DocumentStatsEvent webhookEvent =
                new DocumentStatsEvent(username, projectSlug,
                        versionSlug, strDocId, event.getKey().getLocaleId(),
                        contentStates);

        spyManager.docStatsUpdated(event);

        verify(spyManager).processWebHookEvent(event);
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(spyManager).publishWebhookEvent(captor.capture(),
                eq(webhookEvent));
        assertThat(captor.getValue().size(), is(1));
        assertThat(((WebHook) captor.getValue().get(0)).getWebhookType(),
                is(WebhookType.DocumentStatsEvent));
    }
}
