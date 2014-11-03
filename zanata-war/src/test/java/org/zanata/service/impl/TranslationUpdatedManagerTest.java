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

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.DocumentStatisticUpdatedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Test(groups = { "unit-tests" })
public class TranslationUpdatedManagerTest {

    @Mock
    private TranslationStateCache translationStateCache;

    @Mock
    private TextFlowDAO textFlowDAO;

    TranslationUpdatedManager manager;

    @BeforeMethod(firstTimeOnly = true)
    public void setup() {
        MockitoAnnotations.initMocks(this);
        manager = new TranslationUpdatedManager();
        manager.init(translationStateCache, textFlowDAO);
    }

    @Test
    public void onTranslationUpdateTest() {
        TranslationUpdatedManager spyManager = Mockito.spy(manager);

        Long docId = 1L;
        Long tfId = 1L;
        Long versionId = 1L;
        LocaleId localeId = LocaleId.DE;
        int wordCount = 10;
        ContentState oldState = ContentState.New;
        ContentState newState = ContentState.Translated;

        WordStatistic stats = new WordStatistic(10, 10, 10, 10, 10);
        WordStatistic oldStats = StatisticsUtil.copyWordStatistic(stats);
        oldStats.decrement(newState, wordCount);
        oldStats.increment(oldState, wordCount);

        when(translationStateCache.getDocumentStatistics(docId, localeId)).
                thenReturn(stats);
        when(textFlowDAO.getWordCount(tfId)).thenReturn(wordCount);

        TextFlowTargetStateEvent event =
                new TextFlowTargetStateEvent(null, versionId, docId, tfId,
                        localeId, 1L, newState, oldState);

        spyManager.textFlowStateUpdated(event);

        verify(translationStateCache).textFlowStateUpdated(event);
        verify(spyManager).publishAsyncEvent(event);
    }
}
