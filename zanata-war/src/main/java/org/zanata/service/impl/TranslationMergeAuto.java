package org.zanata.service.impl;

import java.util.List;
import java.util.Set;

import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowTargetHistoryDAO;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.TextFlowTargetExtension;
import org.zanata.rest.dto.resource.ExtensionSet;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.service.TranslationMergeService;
import org.zanata.transformer.TargetCommentTransformer;
import org.zanata.transformer.TargetTransformer;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class TranslationMergeAuto implements TranslationMergeService
{
   private TextFlowTargetHistoryDAO textFlowTargetHistoryDAO;
   private TargetTransformer targetTransformer;

   public TranslationMergeAuto(TextFlowTargetHistoryDAO textFlowTargetHistoryDAO)
   {
      this.textFlowTargetHistoryDAO = textFlowTargetHistoryDAO;
      targetTransformer = new TargetTransformer();
   }

   @Override
   public boolean merge(TextFlowTarget incomingTarget, HTextFlowTarget hTarget, Set<String> extensions)
   {
      if (incomingTarget.getState().isUntranslated())
      {
         return false;
      }

      if (hTarget.getState() == ContentState.New)
      {
         return serverIsUntranslated(incomingTarget, hTarget, extensions);
      }
      else if (incomingTarget.getState().isTranslated())
      {
         return clientIsTranslated(incomingTarget, hTarget, extensions);
      }
      else if (incomingTarget.getState().isRejectedOrFuzzy())
      {
         return clientIsFuzzyOrRejected(incomingTarget, hTarget, extensions);
      }

      throw new RuntimeException("unexpected content state" + incomingTarget.getState());
   }

   private boolean clientIsFuzzyOrRejected(TextFlowTarget incomingTarget, HTextFlowTarget hTarget, Set<String> extensions)
   {
      boolean targetChanged = false;
      if (incomingTarget.getState() == ContentState.NeedReview && hTarget.getState() == ContentState.NeedReview)
      {
         List<String> incomingContents = incomingTarget.getContents();
         boolean contentInHistory = incomingContents.equals(hTarget.getContents()) || textFlowTargetHistoryDAO.findContentInHistory(hTarget, incomingContents);
         if (!contentInHistory)
         {
            targetChanged |= targetTransformer.transform(incomingTarget, hTarget);
            targetChanged |= transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
         }
      }
      return targetChanged;
   }

   private boolean clientIsTranslated(TextFlowTarget incomingTarget, HTextFlowTarget hTarget, Set<String> extensions)
   {
      boolean targetChanged = false;
      List<String> incomingContents = incomingTarget.getContents();
      boolean contentInHistory = incomingContents.equals(hTarget.getContents()) || textFlowTargetHistoryDAO.findContentInHistory(hTarget, incomingContents);
      if (!contentInHistory)
      {
         // content has changed
         targetChanged |= targetTransformer.transform(incomingTarget, hTarget);
         targetChanged |= transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
         hTarget.setState(ContentState.Translated);
      }
      return targetChanged;
   }

   private boolean serverIsUntranslated(TextFlowTarget incomingTarget, HTextFlowTarget hTarget, Set<String> extensions)
   {
      boolean targetChanged = false;
      targetChanged |= targetTransformer.transform(incomingTarget, hTarget);
      if (incomingTarget.getState() == ContentState.Approved)
      {
         hTarget.setState(ContentState.Translated);
      }
      targetChanged |= transferFromTextFlowTargetExtensions(incomingTarget.getExtensions(true), hTarget, extensions);
      return targetChanged;
   }

   private boolean transferFromTextFlowTargetExtensions(ExtensionSet<TextFlowTargetExtension> extensions, HTextFlowTarget hTarget, Set<String> enabledExtensions)
   {
      boolean changed = false;
      if (enabledExtensions.contains(SimpleComment.ID))
      {
         SimpleComment comment = extensions.findByType(SimpleComment.class);
         if (comment != null)
         {
            changed |= new TargetCommentTransformer().transform(comment, hTarget);
         }
      }

      return changed;

   }
}
