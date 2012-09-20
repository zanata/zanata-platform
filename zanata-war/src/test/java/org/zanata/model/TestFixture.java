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

package org.zanata.model;

import java.util.Date;

import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;

import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;

public class TestFixture
{
   public static TransUnit makeTransUnit(long id)
   {
      return makeTransUnit(id, ContentState.New);
   }

   public static TransUnit makeTransUnit(long id, ContentState contentState, String targetContent)
   {
      return TransUnit.Builder.newTransUnitBuilder().setId(id).setResId("resId" + id).setVerNum(0)
            .setLocaleId("en").addSource("source").addTargets(targetContent).setStatus(contentState).setRowIndex((int) id).build();
   }

   public static TransUnit makeTransUnit(long id, ContentState contentState)
   {
      return TransUnit.Builder.newTransUnitBuilder().setId(id).setResId("resId" + id).setVerNum(0)
            .setLocaleId("en").addSource("source").addTargets("target").setStatus(contentState).setRowIndex((int) id).build();
   }

   public static HTextFlow makeHTextFlow(long id, HLocale hLocale, ContentState contentState)
   {
      return makeHTextFlow(id, hLocale, contentState, "pot/message.pot");
   }

   public static HTextFlow makeApprovedHTextFlow(long id, HLocale hLocale)
   {
      return makeHTextFlow(id, hLocale, ContentState.Approved, "pot/message.pot");
   }

   public static HTextFlow makeHTextFlow(long id, HLocale hLocale, ContentState contentState, String docId)
   {
      HDocument hDocument = new HDocument(docId, "message.po", "/src/main/resources", ContentType.PO, hLocale);
      HTextFlow hTextFlow = new HTextFlow(hDocument, "resId" + id, "hello world " + id);
      hTextFlow.setId(id);
      hTextFlow.setPos((int) id);

      HTextFlowTarget target = new HTextFlowTarget();
      target.setTextFlow(hTextFlow);
      target.setVersionNum(0);
      target.setState(contentState);
      target.setLastChanged(new Date());

      hTextFlow.getTargets().put(hLocale.getId(), target);
      return hTextFlow;
   }

   public static UserWorkspaceContext userWorkspaceContext(boolean projectActive, boolean hasWriteAccess, String projectSlug, String iterationSlug)
   {
      LocaleId localeId = new LocaleId("en-US");
      ProjectIterationId projectIterationId = new ProjectIterationId(projectSlug, iterationSlug);
      return new UserWorkspaceContext(new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId()), projectActive, hasWriteAccess, true);
   }
}
