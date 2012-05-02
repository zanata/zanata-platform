/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.server.rpc;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.webtrans.shared.model.TransUnit;

@Name("transUnitTransformer")
@Scope(ScopeType.STATELESS)
@AutoCreate
public class TransUnitTransformer
{
   @In
   private ResourceUtils resourceUtils;

   public TransUnit transform(HTextFlow hTextFlow, HLocale hLocale)
   {
      String msgContext = null;
      if (hTextFlow.getPotEntryData() != null)
      {
         msgContext = hTextFlow.getPotEntryData().getContext();
      }
      HTextFlowTarget target = hTextFlow.getTargets().get(hLocale);

      int nPlurals = resourceUtils.getNumPlurals(hTextFlow.getDocument(), hLocale);
      ArrayList<String> sourceContents = GwtRpcUtil.getSourceContents(hTextFlow);
      ArrayList<String> targetContents = GwtRpcUtil.getTargetContentsWithPadding(hTextFlow, target, nPlurals);

      TransUnit.Builder builder = TransUnit.Builder.newTransUnitBuilder()
            .setId(hTextFlow.getId())
            .setResId(hTextFlow.getResId())
            .setLocaleId(hLocale.getLocaleId())
            .setPlural(hTextFlow.isPlural())
            .setSources(sourceContents)
            .setSourceComment(CommentsUtil.toString(hTextFlow.getComment()))
            .setTargets(targetContents)
            .setMsgContext(msgContext)
            .setRowIndex(hTextFlow.getPos())
            .setVerNum(target == null ? 1 : target.getVersionNum());

      if (target != null)
      {
         builder.setStatus(target.getState());
         if (target.getLastModifiedBy() != null)
         {
            builder.setLastModifiedBy(target.getLastModifiedBy().getName());
         }
         builder.setLastModifiedTime(new SimpleDateFormat().format(target.getLastChanged()));
      }
      return builder.build();
   }
}
