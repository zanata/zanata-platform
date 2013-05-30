package org.zanata.service.impl;

import java.util.Set;

import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class TranslationMergeImport implements TranslationMergeService
{
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   public TranslationMergeImport(TextFlowTargetHistoryDAO textFlowTargetHistoryDAO)
   {
      this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
   }

   @Override
   public boolean merge(TextFlowTarget targetDto, HTextFlowTarget hTarget, Set<String> extensions)
   {
      return false;
   }
}
