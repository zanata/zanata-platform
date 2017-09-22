package org.zanata.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;
import org.zanata.transformer.TargetTransformer;
import com.google.common.base.Preconditions;

/**
 * This strategy is used when HTextFlowTarget does not exist yet, regardless
 * of the selected merge-type.
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TranslationMergeFirstTran implements TranslationMergeService {
    private static final long serialVersionUID = -2380476843887300315L;
    private final int nPlurals;
    private final HLocale hLocale;
    private final HTextFlow textFlow;

    public TranslationMergeFirstTran(int nPlurals, HLocale hLocale,
            HTextFlow textFlow) {
        this.nPlurals = nPlurals;
        this.hLocale = hLocale;
        this.textFlow = textFlow;
    }

    @Override
    public boolean merge(TextFlowTarget incomingTarget,
            HTextFlowTarget hTarget, Set<String> extensions) {
        Preconditions.checkArgument(hTarget == null,
                "This merge service only handles null HTextFlowTarget");
        hTarget = new HTextFlowTarget(textFlow, hLocale);
        List<String> contents = Collections.nCopies(nPlurals, "");
        hTarget.setContents(contents);
        hTarget.setVersionNum(0); // incremented when content is set
        textFlow.getTargets().put(hLocale.getId(), hTarget);
        new TargetTransformer(extensions).transform(incomingTarget,
                hTarget);
        if (incomingTarget.getState().isTranslated()) {
            hTarget.setState(ContentState.Translated);
        }
        return true;
    }

}
