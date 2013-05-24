package org.zanata.service.impl;

import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class TranslationMergeImportNonReviewable implements TranslationMergeService
{
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   public TranslationMergeImportNonReviewable(TextFlowTargetHistoryDAO textFlowTargetHistoryDAO)
   {
      this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
   }

   @Override
   public boolean merge(TextFlowTarget targetDto, HTextFlowTarget hTarget)
   {
      //TODO implement
      throw new UnsupportedOperationException("Implement me!");
      //return false;
   }
}
