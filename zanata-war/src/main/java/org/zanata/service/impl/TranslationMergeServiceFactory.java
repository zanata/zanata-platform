package org.zanata.service.impl;

import lombok.extern.slf4j.Slf4j;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.common.MergeType;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.service.TranslationMergeService;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("translationMergeServiceFactory")
@AutoCreate
@Scope(ScopeType.STATELESS)
@Slf4j
public class TranslationMergeServiceFactory
{
   @In
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;
   private TranslationMergeAuto translationMergeAuto;

   @Create
   public void createMergeAuto()
   {
      translationMergeAuto = new TranslationMergeAuto(textFlowTargetHistoryDAO);
   }

   public TranslationMergeService getMergeService(MergeType mergeType)
   {
      if (mergeType == MergeType.AUTO)
      {
         return translationMergeAuto;
      }
      else if (mergeType == MergeType.IMPORT)
      {
         return TranslationMergeImport.INSTANCE;
      }
      throw new UnsupportedOperationException("merge type unsupported");
   }
}
