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
package org.zanata.service.impl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.core.Events;
import org.jboss.seam.util.Work;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowDAO;
import org.zanata.events.TextFlowTargetStateEvent;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationStateCache;
import org.zanata.util.MessageGenerator;

import com.google.common.base.Stopwatch;

/**
 * Merge translation and persist in transaction.
 *
 * Merge HTextFlowTargets from HProjectIteration(id=sourceVersionId) to
 * HProjectIteration(id=targetVersionId) in batches(batchStart, batchLength)
 *
 * @see org.zanata.service.impl.MergeTranslationsServiceImpl#startMergeTranslations
 * @see org.zanata.service.impl.MergeTranslationsServiceImpl#shouldMerge
 *
 * returns count of processed translations.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Slf4j
@AllArgsConstructor
public class MergeTranslationsWork extends Work<Integer> {
    private final Long sourceVersionId;
    private final Long targetVersionId;
    private final int batchStart;
    private final int batchLength;
    private final boolean useNewerTranslation;
    private final List<HLocale> supportedLocales;

    private final TextFlowDAO textFlowDAO;

    private final TranslationStateCache translationStateCacheImpl;

    private final Stopwatch stopwatch = Stopwatch.createUnstarted();

    @Override
    protected Integer work() throws Exception {
        stopwatch.start();

        List<HTextFlow[]> matches =
                textFlowDAO.getSourceByMatchedContext(
                        sourceVersionId, targetVersionId, batchStart,
                        batchLength);

        for (HTextFlow[] results : matches) {
            HTextFlow sourceTf = results[0];
            HTextFlow targetTf = results[1];
            boolean foundChange = false;

            for (HLocale hLocale : supportedLocales) {
                HTextFlowTarget sourceTft =
                        sourceTf.getTargets().get(hLocale.getId());
                // only process translated state
                if (sourceTft == null || !sourceTft.getState().isTranslated()) {
                    continue;
                }

                HTextFlowTarget targetTft =
                        targetTf.getTargets().get(hLocale.getId());
                if (targetTft == null) {
                    targetTft = new HTextFlowTarget(targetTf, hLocale);
                    targetTft.setVersionNum(0);
                    targetTf.getTargets().put(hLocale.getId(), targetTft);
                }

                if (MergeTranslationsServiceImpl.shouldMerge(sourceTft,
                        targetTft, useNewerTranslation)) {
                    foundChange = true;
                    mergeTextFlowTarget(sourceTft, targetTft);
                }
            }
            if (foundChange) {
                translationStateCacheImpl.clearDocumentStatistics(targetTf
                        .getDocument().getId());
                textFlowDAO.makePersistent(targetTf);
                textFlowDAO.flush();
            }
        }
        stopwatch.stop();
        log.info("Complete merge translations of {} in {}", matches.size()
                * supportedLocales.size(), stopwatch);
        return matches.size() * supportedLocales.size();
    }

    private void mergeTextFlowTarget(HTextFlowTarget sourceTft,
            HTextFlowTarget targetTft) {
        ContentState oldState = targetTft.getState();

        targetTft.setContents(sourceTft.getContents());
        targetTft.setState(sourceTft.getState());
        targetTft.setLastChanged(sourceTft.getLastChanged());
        targetTft.setLastModifiedBy(sourceTft.getLastModifiedBy());
        targetTft.setTranslator(sourceTft.getTranslator());

        if (sourceTft.getComment() == null) {
            targetTft.setComment(null);
        } else {
            HSimpleComment hComment = targetTft.getComment();
            if (hComment == null) {
                hComment = new HSimpleComment();
                targetTft.setComment(hComment);
            }
            hComment.setComment(sourceTft.getComment().getComment());
        }
        targetTft.setRevisionComment(MessageGenerator
            .getMergeTranslationMessage(sourceTft));

        raiseSuccessEvent(targetTft, oldState);
    }

    private void raiseSuccessEvent(HTextFlowTarget targetTft,
            ContentState oldState) {
        if (Events.exists()) {
            TextFlowTargetStateEvent event =
                    new TextFlowTargetStateEvent(null, targetVersionId,
                            targetTft.getTextFlow().getDocument().getId(),
                            targetTft.getTextFlow().getId(),
                            targetTft.getLocale().getLocaleId(),
                            targetTft.getId(), targetTft.getState(), oldState);

            Events.instance().raiseTransactionSuccessEvent(
                    TextFlowTargetStateEvent.EVENT_NAME, event);
        }
    }
}
