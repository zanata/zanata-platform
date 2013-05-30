package org.zanata.service.impl;

import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;
import org.zanata.transformer.TargetTransformer;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
enum  TranslationMergeImport implements TranslationMergeService
{
   INSTANCE;

   @Override
   public boolean merge(TextFlowTarget incomingTarget, HTextFlowTarget hTarget, Set<String> extensions)
   {
      TargetTransformer targetTransformer = new TargetTransformer(extensions);
      boolean targetChanged = targetTransformer.transform(incomingTarget, hTarget);
      if (incomingTarget.getState().isTranslated())
      {
         hTarget.setState(ContentState.Translated);
      }
      return targetChanged;
   }
}
