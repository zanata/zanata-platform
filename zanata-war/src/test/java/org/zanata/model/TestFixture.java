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
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;

import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.gwt.event.shared.GwtEvent;

public class TestFixture
{
   public static TransUnit makeTransUnit(long id)
   {
      return makeTransUnit(id, ContentState.New);
   }

   public static TransUnit makeTransUnit(long id, ContentState contentState, String... targetContent)
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

   public static UserWorkspaceContext userWorkspaceContext()
   {
      return userWorkspaceContext(true, true, "project", "master");
   }

   public static UserWorkspaceContext userWorkspaceContext(boolean projectActive, boolean hasWriteAccess, String projectSlug, String iterationSlug)
   {
      return new UserWorkspaceContext(workspaceContext(new LocaleId("en-US")), projectActive, hasWriteAccess, true);
   }

   public static WorkspaceContext workspaceContext(LocaleId localeId)
   {
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master");
      return new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId());
   }

   public static  <E extends GwtEvent<?>> E extractFromEvents(List<? extends GwtEvent> events, final Class<E> eventType)
   {
      GwtEvent gwtEvent = Iterables.find(events, new Predicate<GwtEvent>()
      {
         @Override
         public boolean apply(GwtEvent input)
         {
            return eventType.isAssignableFrom(input.getClass());
         }
      });
      return (E) gwtEvent;
   }

   public static List<Integer> asIds(List<TransUnit> transUnits)
   {
      return Lists.newArrayList(Collections2.transform(transUnits, new Function<TransUnit, Integer>()
      {
         @Override
         public Integer apply(TransUnit from)
         {
            return (int) from.getId().getId();
         }
      }));
   }
}
