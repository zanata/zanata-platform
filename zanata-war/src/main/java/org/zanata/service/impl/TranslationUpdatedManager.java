package org.zanata.service.impl;

import com.google.common.annotations.VisibleForTesting;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.core.Events;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.DocumentStatisticUpdatedEvent;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.service.DocumentService;
import org.zanata.service.TranslationStateCache;
import org.zanata.ui.model.statistic.WordStatistic;
import org.zanata.util.StatisticsUtil;

import lombok.extern.slf4j.Slf4j;

import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;

/**
 * Manager that handles post update of translation. Important:
 * TextFlowTargetStateEvent IS NOT asynchronous, that is why
 * DocumentStatisticUpdatedEvent is used for webhook processes. See
 * {@link org.zanata.events.TextFlowTargetStateEvent} See
 * {@link org.zanata.events.DocumentStatisticUpdatedEvent}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Name("translationUpdatedManager")
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationUpdatedManager {

    @In
    private TranslationStateCache translationStateCacheImpl;

    @In
    private TextFlowDAO textFlowDAO;

    /**
     * This method contains all logic to be run immediately after a Text Flow
     * Target has been successfully translated.
     */
    @Observer(TextFlowTargetStateEvent.EVENT_NAME)
    public void textFlowStateUpdated(
            @Observes(during = TransactionPhase.AFTER_SUCCESS)
            TextFlowTargetStateEvent event) {
        translationStateCacheImpl.textFlowStateUpdated(event);
        publishAsyncEvent(event);
    }

    // Fire asynchronous event
    public void publishAsyncEvent(TextFlowTargetStateEvent event) {
        if (Events.exists()) {
            WordStatistic stats =
                    translationStateCacheImpl.getDocumentStatistics(
                            event.getDocumentId(), event.getLocaleId());

            int wordCount = textFlowDAO.getWordCount(event.getTextFlowId());

            WordStatistic oldStats = StatisticsUtil.copyWordStatistic(stats);
            oldStats.decrement(event.getNewState(), wordCount);
            oldStats.increment(event.getPreviousState(), wordCount);

            Events.instance().raiseAsynchronousEvent(
                    DocumentStatisticUpdatedEvent.EVENT_NAME,
                    new DocumentStatisticUpdatedEvent(oldStats, stats,
                            event.getProjectIterationId(),
                            event.getDocumentId(), event.getLocaleId(),
                            event.getPreviousState(), event.getNewState()));
        }
    }

    @VisibleForTesting
    public void init(TranslationStateCache translationStateCacheImpl,
            TextFlowDAO textFlowDAO) {
        this.translationStateCacheImpl = translationStateCacheImpl;
        this.textFlowDAO = textFlowDAO;
    }
}
