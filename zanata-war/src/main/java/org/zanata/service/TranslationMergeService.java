package org.zanata.service;

import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;

public interface TranslationMergeService
{

   boolean merge(TextFlowTarget targetDto, HTextFlowTarget hTarget);

}
