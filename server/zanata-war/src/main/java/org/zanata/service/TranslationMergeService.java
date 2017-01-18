package org.zanata.service;

import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;

import java.util.Set;

public interface TranslationMergeService {

    /**
     * Update hTarget to match targetDto, according to the implemented strategy.
     * @param targetDto
     * @param hTarget
     * @param extensions
     * @return true if the HTextFlowTarget's state was changed
     */
    boolean merge(TextFlowTarget targetDto, HTextFlowTarget hTarget,
            Set<String> extensions);

}
