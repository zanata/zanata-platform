package org.zanata.mock;

import org.zanata.common.ContentState;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;

import java.util.Set;

/**
 * This strategy is used when HTextFlowTarget already exists and sets the State to Approved if it is Translated
 * @author Dragos Varovici <a
 *         href="mailto:dvarovici.work@gmail.com">dvarovici.work@gmail.com</a>
 */
class MockTranslationMergeApproved implements TranslationMergeService {
    private final TranslationMergeService original;

    public MockTranslationMergeApproved(TranslationMergeService original) {
        this.original = original;
    }

    @Override
    public boolean merge(TextFlowTarget incomingTarget,
                         HTextFlowTarget hTarget, Set<String> extensions) {
        boolean targetChanged = original.merge(incomingTarget, hTarget, extensions);

        if (targetChanged && hTarget != null && hTarget.getState().isTranslated()) {
            hTarget.setState(ContentState.Approved);
        }

        return targetChanged;
    }
}
