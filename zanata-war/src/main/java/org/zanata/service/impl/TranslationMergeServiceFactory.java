package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.MergeType;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.rest.service.ResourceUtils;
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

   public TranslationMergeService getMergeService(MergeType mergeType)
   {
      if (mergeType == MergeType.AUTO)
      {
         return new TranslationMergeAuto(textFlowTargetHistoryDAO);
      }
      else if (mergeType == MergeType.IMPORT)
      {
         return new TranslationMergeImport(textFlowTargetHistoryDAO);
      }
      throw new UnsupportedOperationException("merge type unsupported");
   }
}
