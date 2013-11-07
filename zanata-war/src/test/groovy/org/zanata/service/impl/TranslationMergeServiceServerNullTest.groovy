package org.zanata.service.impl

import org.concordion.api.extension.Extensions
import org.concordion.ext.TimestampFormatterExtension
import org.concordion.integration.junit4.ConcordionRunner
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.zanata.common.ContentState
import org.zanata.common.LocaleId
import org.zanata.model.HLocale
import org.zanata.model.HTextFlow
import org.zanata.model.HTextFlowTarget
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.service.TranslationMergeService

@RunWith(ConcordionRunner)
@Extensions([TimestampFormatterExtension])
class TranslationMergeServiceServerNullTest {

    private TranslationMergeService mergeService;

    private TranslationMergeServiceFactory.MergeContext context

    void given(String locale) {
        MockitoAnnotations.initMocks(this)

        def hLocale = new HLocale(id: 1, localeId: new LocaleId(locale))
        context = new TranslationMergeServiceFactory.MergeContext(null, new HTextFlow(), hLocale, null, 1)
        mergeService = new TranslationMergeServiceFactory().getMergeService(context)
    }

    Result merge(String contentFromClient, String stateFromClient) {
        def client = new TextFlowTarget(contents: [contentFromClient], state: ContentState.valueOf(stateFromClient))

        def changed = mergeService.merge(client, null, Collections.emptySet())

        def updatedTarget = context.getHTextFlow().getTargets().get(context.getHLocale().getId())
        new Result(changed: changed, endContent: getFirstContentOrNull(updatedTarget), endVersion: updatedTarget.getVersionNum(), endState: updatedTarget.getState())
    }

    String getFirstContentOrNull(HTextFlowTarget updatedTarget) {
        def contents = updatedTarget.getContents()
        (contents.isEmpty()) ? null : contents[0]
    }

    class Result {
        boolean changed
        String endContent
        int endVersion
        ContentState endState
    }
}
