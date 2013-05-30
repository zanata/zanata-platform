package org.zanata.service.impl

import org.concordion.api.extension.Extensions
import org.concordion.ext.TimestampFormatterExtension
import org.concordion.integration.junit4.ConcordionRunner
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.BDDMockito
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.zanata.common.ContentState
import org.zanata.common.MergeType
import org.zanata.dao.TextFlowTargetHistoryDAO
import org.zanata.model.HTextFlowTarget
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.service.TranslationMergeService

import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyListOf

@RunWith(ConcordionRunner)
@Extensions([TimestampFormatterExtension])
class TranslationMergeServiceTest {

    private TranslationMergeService mergeService;

    @Mock
    TextFlowTargetHistoryDAO historyDao

    @Before
    void setUp() {
        MockitoAnnotations.initMocks(this)
        mergeService = null;
    }

    def given(String mergeType)
    {
        def factory = new TranslationMergeServiceFactory(textFlowTargetHistoryDAO: historyDao)
        mergeService = factory.getMergeService(MergeType.valueOf(mergeType))
    }

    Result merge(String contentFromClient, String stateFromClient, String contentOnServer, String stateOnServer, String contentInHistory)
    {
        BDDMockito.given(historyDao.findContentInHistory(any(HTextFlowTarget), anyListOf(String))).willReturn(Boolean.valueOf(contentInHistory));

        def client = new TextFlowTarget(contents: [contentFromClient], state: ContentState.valueOf(stateFromClient))
        def server = new HTextFlowTarget(contents: [contentOnServer], state: ContentState.valueOf(stateOnServer))

        def changed = mergeService.merge(client, server, Collections.emptySet())

        new Result(changed: changed, endContent: server.getContents()[0], endState: server.getState())
    }

    class Result
    {
        boolean changed
        String endContent
        ContentState endState
    }
}
