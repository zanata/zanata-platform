package org.zanata.service.impl;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.MergeType;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationMergeService;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("translationMergeServiceFactory")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationMergeServiceFactory {
    @In
    private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

    private TranslationMergeAuto translationMergeAuto;

    public TranslationMergeService getMergeService(MergeContext mergeContext) {
        if (mergeContext.currentHTarget == null) {
            return new TranslationMergeFirstTran(mergeContext.nPlurals,
                    mergeContext.hLocale, mergeContext.hTextFlow);
        }
        if (mergeContext.mergeType == MergeType.AUTO) {
            return ensureMergeAuto();
        } else if (mergeContext.mergeType == MergeType.IMPORT) {
            return TranslationMergeImport.INSTANCE;
        }
        throw new UnsupportedOperationException("merge unsupported: "
                + mergeContext);
    }

    private TranslationMergeService ensureMergeAuto() {
        if (translationMergeAuto == null) {
            translationMergeAuto =
                    new TranslationMergeAuto(textFlowTargetHistoryDAO);
        }
        return translationMergeAuto;
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class MergeContext {
        private final MergeType mergeType;
        private final HTextFlow hTextFlow;
        private final HLocale hLocale;
        private final HTextFlowTarget currentHTarget;
        private final int nPlurals;
    }
}
