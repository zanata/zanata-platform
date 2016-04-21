package org.zanata.service.impl;

import java.util.List;
import java.util.Map;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.zanata.async.Async;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.DocumentStatisticUpdatedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.service.TranslationStateCache;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

import static org.zanata.events.TextFlowTargetStateEvent.TextFlowTargetState;

/**
 * Manager that handles post update of translation. Important:
 * TextFlowTargetStateEvent IS NOT asynchronous, that is why
 * DocumentStatisticUpdatedEvent is used for webhook processes. See
 * {@link org.zanata.events.TextFlowTargetStateEvent} See
 * {@link org.zanata.events.DocumentStatisticUpdatedEvent}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("translationUpdatedManager")
@RequestScoped
@Slf4j
public class TranslationUpdatedManager {

    @Inject
    private TranslationStateCache translationStateCacheImpl;

    @Inject
    private TextFlowDAO textFlowDAO;

    @Inject
    private Event<DocumentStatisticUpdatedEvent> documentStatisticUpdatedEvent;

    /**
     * This method contains all logic to be run immediately after a Text Flow
     * Target has been successfully translated.
     */
    @Async
    public void textFlowStateUpdated(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            TextFlowTargetStateEvent event) {
        translationStateCacheImpl.textFlowStateUpdated(event);
        publishAsyncEvent(event);
    }

    // Fire asynchronous event
    void publishAsyncEvent(TextFlowTargetStateEvent event) {
        if (BeanManagerProvider.isActive()) {
            Long versionId = event.getProjectIterationId();
            Long documentId = event.getKey().getDocumentId();
            LocaleId localeId = event.getKey().getLocaleId();

            for(TextFlowTargetState state: event.getStates()) {
                int wordCount =
                    textFlowDAO.getWordCount(state.getTextFlowId());
                // TODO PERF: generate one DocumentStatisticUpdatedEvent per
                // TextFlowTargetStateEvent. See
                // DocumentServiceImpl.documentStatisticUpdated
                documentStatisticUpdatedEvent
                    .fire(new DocumentStatisticUpdatedEvent(
                        versionId, documentId, localeId,
                        wordCount, state.getPreviousState(),
                        state.getNewState()));
            }
        }
    }

    @VisibleForTesting
    public void init(TranslationStateCache translationStateCacheImpl,
            TextFlowDAO textFlowDAO,
            Event<DocumentStatisticUpdatedEvent> documentStatisticUpdatedEvent) {
        this.translationStateCacheImpl = translationStateCacheImpl;
        this.textFlowDAO = textFlowDAO;
        this.documentStatisticUpdatedEvent = documentStatisticUpdatedEvent;
    }
}
