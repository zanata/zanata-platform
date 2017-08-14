package org.zanata.service.impl;

import org.zanata.model.HCopyTransOptions;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class CopyTransOptionFactory {

    /**
     * Loose settings - wide range of search
     */
    public static HCopyTransOptions getExplicitOptions() {
        return getOptions(HCopyTransOptions.ConditionRuleAction.IGNORE,
                HCopyTransOptions.ConditionRuleAction.IGNORE,
                HCopyTransOptions.ConditionRuleAction.IGNORE);
    }

    /**
     * Conservative settings - strict search
     */
    public static HCopyTransOptions getImplicitOptions() {
        return getOptions(HCopyTransOptions.ConditionRuleAction.REJECT,
                HCopyTransOptions.ConditionRuleAction.REJECT,
                HCopyTransOptions.ConditionRuleAction.REJECT);
    }

    private static HCopyTransOptions getOptions(
            HCopyTransOptions.ConditionRuleAction contextAction,
            HCopyTransOptions.ConditionRuleAction docIdAction,
            HCopyTransOptions.ConditionRuleAction projectAction) {

        HCopyTransOptions instance = new HCopyTransOptions();
        instance.setContextMismatchAction(contextAction);
        instance.setDocIdMismatchAction(docIdAction);
        instance.setProjectMismatchAction(projectAction);

        return instance;
    }
}
