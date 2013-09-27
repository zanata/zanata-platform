package org.zanata.service.impl;

import java.util.List;
import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;
import org.zanata.transformer.TargetTransformer;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class TranslationMergeAuto implements TranslationMergeService {
    private final TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

    TranslationMergeAuto(TextFlowTargetHistoryDAO textFlowTargetHistoryDAO) {
        this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
    }

    @Override
    public boolean merge(TextFlowTarget incomingTarget,
            HTextFlowTarget hTarget, Set<String> extensions) {
        TargetTransformer targetTransformer = new TargetTransformer(extensions);
        if (incomingTarget.getState().isUntranslated()) {
            return false;
        }

        if (hTarget.getState() == ContentState.New) {
            return serverIsUntranslated(incomingTarget, hTarget,
                    targetTransformer);
        } else if (incomingTarget.getState().isTranslated()) {
            return clientIsTranslated(incomingTarget, hTarget,
                    targetTransformer);
        } else if (incomingTarget.getState().isRejectedOrFuzzy()) {
            return clientIsFuzzyOrRejected(incomingTarget, hTarget,
                    targetTransformer);
        }

        throw new RuntimeException("unexpected content state"
                + incomingTarget.getState());
    }

    private boolean clientIsFuzzyOrRejected(TextFlowTarget incomingTarget,
            HTextFlowTarget hTarget, TargetTransformer targetTransformer) {
        boolean targetChanged = false;
        if (incomingTarget.getState() == ContentState.NeedReview
                && hTarget.getState() == ContentState.NeedReview) {
            List<String> incomingContents = incomingTarget.getContents();
            boolean contentInHistory =
                    incomingContents.equals(hTarget.getContents())
                            || textFlowTargetHistoryDAO.findContentInHistory(
                                    hTarget, incomingContents);
            if (!contentInHistory) {
                targetChanged |=
                        targetTransformer.transform(incomingTarget, hTarget);
            }
        }
        return targetChanged;
    }

    private boolean clientIsTranslated(TextFlowTarget incomingTarget,
            HTextFlowTarget hTarget, TargetTransformer targetTransformer) {
        boolean targetChanged = false;
        List<String> incomingContents = incomingTarget.getContents();
        boolean contentInHistory =
                incomingContents.equals(hTarget.getContents())
                        || textFlowTargetHistoryDAO.findContentInHistory(
                                hTarget, incomingContents);
        if (!contentInHistory) {
            // content has changed
            targetChanged |=
                    targetTransformer.transform(incomingTarget, hTarget);
            hTarget.setState(ContentState.Translated);
        }
        return targetChanged;
    }

    private boolean serverIsUntranslated(TextFlowTarget incomingTarget,
            HTextFlowTarget hTarget, TargetTransformer targetTransformer) {
        boolean targetChanged = false;
        targetChanged |= targetTransformer.transform(incomingTarget, hTarget);
        if (incomingTarget.getState() == ContentState.Approved) {
            hTarget.setState(ContentState.Translated);
        }
        return targetChanged;
    }

}
