package org.zanata.service.impl;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.MergeType;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationMergeService;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Named("translationMergeServiceFactory")
@RequestScoped
public class TranslationMergeServiceFactory {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(TranslationMergeServiceFactory.class);

    @Inject
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
        throw new UnsupportedOperationException(
                "merge unsupported: " + mergeContext);
    }

    private TranslationMergeService ensureMergeAuto() {
        if (translationMergeAuto == null) {
            translationMergeAuto =
                    new TranslationMergeAuto(textFlowTargetHistoryDAO);
        }
        return translationMergeAuto;
    }

    public static class MergeContext {
        private final MergeType mergeType;
        private final HTextFlow hTextFlow;
        private final HLocale hLocale;
        private final HTextFlowTarget currentHTarget;
        private final int nPlurals;

        public MergeType getMergeType() {
            return this.mergeType;
        }

        public HTextFlow getHTextFlow() {
            return this.hTextFlow;
        }

        public HLocale getHLocale() {
            return this.hLocale;
        }

        public HTextFlowTarget getCurrentHTarget() {
            return this.currentHTarget;
        }

        public int getNPlurals() {
            return this.nPlurals;
        }

        @java.beans.ConstructorProperties({ "mergeType", "hTextFlow", "hLocale",
                "currentHTarget", "nPlurals" })
        public MergeContext(final MergeType mergeType,
                final HTextFlow hTextFlow, final HLocale hLocale,
                final HTextFlowTarget currentHTarget, final int nPlurals) {
            this.mergeType = mergeType;
            this.hTextFlow = hTextFlow;
            this.hLocale = hLocale;
            this.currentHTarget = currentHTarget;
            this.nPlurals = nPlurals;
        }

        @Override
        public String toString() {
            return "TranslationMergeServiceFactory.MergeContext(mergeType="
                    + this.getMergeType() + ", hTextFlow=" + this.getHTextFlow()
                    + ", hLocale=" + this.getHLocale() + ", currentHTarget="
                    + this.getCurrentHTarget() + ", nPlurals="
                    + this.getNPlurals() + ")";
        }
    }
}
