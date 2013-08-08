/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.server.rpc;

import org.zanata.common.ContentState;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.MergeRule;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import com.google.common.base.Objects;

/**
 * This is a stateful class and needs to create new instance each time
 * to determine whether a TM result should be used to auto translate a text flow.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergeStatusResolver
{
   private TransMemoryMergeStatusResolver()
   {
   }

   public static TransMemoryMergeStatusResolver newInstance()
   {
      return new TransMemoryMergeStatusResolver();
   }

   private boolean needReview = false;
   private boolean needSkip = false;

   /**
    * 
    * @param action TM merge action
    * @param tfToBeFilled text flow to be filled
    * @param tmDetail TM detail
    * @param tmResult TM result
    * @param oldTarget text flow target that may be null or has NEW or FUZZY
    *           status
    * 
    * @return content state to be set on auto translated target. If null means
    *         we want to reject the auto translation via TM merge
    */
   public ContentState decideStatus(TransMemoryMerge action, HTextFlow tfToBeFilled, TransMemoryDetails tmDetail, TransMemoryResultItem tmResult, HTextFlowTarget oldTarget)
   {

      if ((int) tmResult.getSimilarityPercent() != 100)
      {
         needReview = true;
      }
      compareTextFlowResId(action, tfToBeFilled, tmDetail);
      compareTextFlowMsgContext(action, tfToBeFilled, tmDetail);
      compareDocId(action, tfToBeFilled, tmDetail);
      compareProjectName(action, tfToBeFilled, tmDetail);

      if (needSkip)
      {
         return null;
      }
      else if (needReview)
      {
         // if there is an old translation and we only find TM needs review, we don't overwrite previous translation
         if (oldTarget != null && oldTarget.getState() != ContentState.New)
         {
            return null;
         }
         return ContentState.NeedReview;
      }
      return ContentState.Translated;
   }

   /**
    *
    * @param action TM merge action
    * @param tmResult TM result
    * @param oldTarget text flow target that may be null or has NEW or FUZZY
    *           status
    *
    * @return content state to be set on auto translated target. If null means
    *         we want to reject the auto translation via TM merge
    */
   public ContentState decideStatus(TransMemoryMerge action, TransMemoryResultItem tmResult,
                                     HTextFlowTarget oldTarget)
   {

      if ((int) tmResult.getSimilarityPercent() != 100)
      {
         needReview = true;
      }

      setFlagsBasedOnOption(action.getImportedMatchRule());

      if (needSkip)
      {
         return null;
      }
      else if (needReview)
      {
         // if there is an old translation and we only find TM needs review, we don't overwrite previous translation
         if (oldTarget != null && oldTarget.getState() != ContentState.New)
         {
            return null;
         }
         return ContentState.NeedReview;
      }
      return ContentState.Translated;
   }

   private void compareTextFlowResId(TransMemoryMerge action, HTextFlow tfToBeFilled, TransMemoryDetails tmDetail)
   {
      if (action.getDifferentContextRule() != MergeRule.IGNORE_CHECK
            && notEqual(tfToBeFilled.getResId(), tmDetail.getResId()))
      {
         setFlagsBasedOnOption(action.getDifferentContextRule());
      }
   }

   private void compareTextFlowMsgContext(TransMemoryMerge action, HTextFlow tfToBeFilled, TransMemoryDetails tmDetail)
   {
      if (action.getDifferentContextRule() != MergeRule.IGNORE_CHECK)
      {
         String msgCtx = null;
         if (tfToBeFilled.getPotEntryData() != null)
         {
            msgCtx = tfToBeFilled.getPotEntryData().getContext();
         }
         if (notEqual(msgCtx, tmDetail.getMsgContext()))
         {
            setFlagsBasedOnOption(action.getDifferentContextRule());
         }
      }
   }

   private void compareDocId(TransMemoryMerge action, HTextFlow tfToBeFilled, TransMemoryDetails tmDetail)
   {
      if (action.getDifferentDocumentRule() != MergeRule.IGNORE_CHECK
            && notEqual(tfToBeFilled.getDocument().getDocId(), tmDetail.getDocId()))
      {
         setFlagsBasedOnOption(action.getDifferentDocumentRule());
      }
   }

   private void compareProjectName(TransMemoryMerge action, HTextFlow tfToBeFilled, TransMemoryDetails tmDetail)
   {
      if (action.getDifferentProjectRule() != MergeRule.IGNORE_CHECK
            && notEqual(tfToBeFilled.getDocument().getProjectIteration().getProject().getName(), tmDetail.getProjectName()))
      {
         setFlagsBasedOnOption(action.getDifferentProjectRule());
      }
   }

   private void setFlagsBasedOnOption(MergeRule mergeOption)
   {
      if (mergeOption == MergeRule.REJECT)
      {
         needSkip = true;
      }
      else if (mergeOption == MergeRule.FUZZY)
      {
         needReview = true;
      }
   }

   private static boolean notEqual(String one, String another)
   {
      return !Objects.equal(one, another);
   }
}
