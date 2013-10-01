package org.zanata.service;

import java.util.Set;

import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;

public interface TranslationMergeService {

    boolean merge(TextFlowTarget targetDto, HTextFlowTarget hTarget,
            Set<String> extensions);

}
