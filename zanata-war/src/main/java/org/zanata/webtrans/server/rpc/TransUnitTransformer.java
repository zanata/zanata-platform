/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import java.util.ArrayList;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.webtrans.shared.model.TransUnit;

@Name("transUnitTransformer")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TransUnitTransformer
{
   private static final int NULL_TARGET_VERSION_NUM = 0;

   @In
   private ResourceUtils resourceUtils;

   public TransUnit transform(HTextFlow hTextFlow, HLocale hLocale)
   {
      HTextFlowTarget target = hTextFlow.getTargets().get(hLocale.getId());

      return transform(hTextFlow, target, hLocale);
   }

   public TransUnit transform(HTextFlow hTextFlow, HTextFlowTarget target)
   {
      return transform(hTextFlow, target, target.getLocale());
   }

   private TransUnit transform(HTextFlow hTextFlow, HTextFlowTarget target, HLocale hLocale)
   {
      String msgContext = null;
      if (hTextFlow.getPotEntryData() != null)
      {
         msgContext = hTextFlow.getPotEntryData().getContext();
      }

      int nPlurals = resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale);
      ArrayList<String> sourceContents = GwtRpcUtil.getSourceContents(hTextFlow);
      ArrayList<String> targetContents = GwtRpcUtil.getTargetContentsWithPadding(hTextFlow, target, nPlurals);

      // @formatter:off
      TransUnit.Builder builder = TransUnit.Builder.newTransUnitBuilder()
            .setId(hTextFlow.getId())
            .setResId(hTextFlow.getResId())
            .setLocaleId(hLocale.getLocaleId())
            .setPlural(hTextFlow.isPlural())
            .setSources(sourceContents)
            .setSourceComment(commentToString(hTextFlow.getComment()))
            .setTargets(targetContents)
            .setTargetComment(target == null ? null : commentToString(target.getComment()))
            .setMsgContext(msgContext)
            .setRowIndex(hTextFlow.getPos())
            .setVerNum(target == null ? NULL_TARGET_VERSION_NUM : target.getVersionNum())
            // TODO pahuang we may consider to use a query to gather has comment information
            .setHasUserComment(target != null && target.getReviewComments().size() > 0)
            ;
      // @formatter:on

      if (target != null)
      {
         builder.setStatus(target.getState());
         if (target.getLastModifiedBy() != null)
         {
            builder.setLastModifiedBy(target.getLastModifiedBy().getName());
         }
         builder.setLastModifiedTime(target.getLastChanged());
      }
      return builder.build();
   }



   private static String commentToString(HSimpleComment comment)
   {
      return comment == null ? null : comment.getComment();
   }

}
