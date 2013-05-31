package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.MergeType;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.service.TranslationMergeService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("translationMergeServiceFactory")
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationMergeServiceFactory
{
   @In
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;

   protected TranslationMergeServiceFactory()
   {
   }

   // for testing only
   protected TranslationMergeServiceFactory(TextFlowTargetHistoryDAO targetHistoryDAO)
   {
      this.textFlowTargetHistoryDAO = targetHistoryDAO;
   }

   public TranslationMergeService getMergeService(MergeType mergeType, boolean requireTranslationReview)
   {
      if (mergeType == MergeType.AUTO)
      {
         if (requireTranslationReview)
         {
            return new TranslationMergeAutoReviewable(textFlowTargetHistoryDAO);
         }
         else
         {
            return new TranslationMergeAutoNonReviewable(textFlowTargetHistoryDAO);
         }
      }
      else if (mergeType == MergeType.IMPORT)
      {
         if (requireTranslationReview)
         {
            return new TranslationMergeImportReviewable(textFlowTargetHistoryDAO);
         }
         else
         {
            return new TranslationMergeImportNonReviewable(textFlowTargetHistoryDAO);
         }
      }
      else
      {
         throw new UnsupportedOperationException("merge type unsupported");
      }
   }
}
