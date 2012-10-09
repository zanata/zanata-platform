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
package org.zanata.webtrans.client.service;

import java.util.HashMap;
import java.util.Map;

import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Singleton
public class UserSessionService implements TransUnitEditEventHandler, ExitWorkspaceEventHandler, EnterWorkspaceEventHandler
{
   private final HashMap<EditorClientId, UserPanelSessionItem> userSessionMap = Maps.newHashMap();
   private final EventBus eventBus;
   private final DistinctColor distinctColor;
   private final WorkspaceUsersPresenter workspaceUsersPresenter;
   private final TranslatorInteractionService interactionService;

   @Inject
   public UserSessionService(EventBus eventBus, DistinctColor distinctColor, WorkspaceUsersPresenter workspaceUsersPresenter, TranslatorInteractionService interactionService)
   {
      this.eventBus = eventBus;
      this.distinctColor = distinctColor;
      this.workspaceUsersPresenter = workspaceUsersPresenter;
      this.interactionService = interactionService;

      eventBus.addHandler(TransUnitEditEvent.getType(), this);
      eventBus.addHandler(ExitWorkspaceEvent.getType(), this);
      eventBus.addHandler(EnterWorkspaceEvent.getType(), this);
   }

   @Override
   public void onTransUnitEdit(TransUnitEditEvent event)
   {
      updateTranslatorStatus(event.getEditorClientId(), event.getSelectedTransUnit());
   }

   private void updateTranslatorStatus(EditorClientId editorClientId, TransUnit selectedTransUnit)
   {
      if (userSessionMap.containsKey(editorClientId) && selectedTransUnit != null)
      {
         userSessionMap.get(editorClientId).setSelectedTransUnit(selectedTransUnit);
      }
   }

   private UserPanelSessionItem getUserPanel(EditorClientId editorClientId)
   {
      return userSessionMap.get(editorClientId);
   }

   private void addUser(EditorClientId editorClientId, UserPanelSessionItem item)
   {
      userSessionMap.put(editorClientId, item);
   }

   private void removeUser(EditorClientId editorClientId)
   {
      userSessionMap.remove(editorClientId);
      distinctColor.releaseColor(editorClientId);
   }

   public Map<EditorClientId, UserPanelSessionItem> getUserSessionMap()
   {
      return userSessionMap;
   }

   public String getColor(EditorClientId editorClientId)
   {
      return distinctColor.getOrCreateColor(editorClientId);
   }

   @Override
   public void onEnterWorkspace(EnterWorkspaceEvent event)
   {
      HasManageUserPanel panel = workspaceUsersPresenter.addNewUser(event.getPerson());
      addTranslator(event.getEditorClientId(), event.getPerson(), null, panel);
   }

   @Override
   public void onExitWorkspace(ExitWorkspaceEvent event)
   {
      EditorClientId editorClientId = event.getEditorClientId();
      UserPanelSessionItem item = getUserPanel(editorClientId);
      removeUser(editorClientId);

      workspaceUsersPresenter.removeUser(item.getPanel(), event.getPerson().getId().toString());
      interactionService.personExit(event.getPerson(), item.getSelectedTransUnit());

      if (Objects.equal(editorClientId, interactionService.getCurrentEditorClientId()))
      {
         // TODO if this works then localize the message
         eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, "Session has timed out. Please refresh your browser."));
      }
   }

   public void initUserList(Map<EditorClientId, PersonSessionDetails> translatorList)
   {
      for (Map.Entry<EditorClientId, PersonSessionDetails> entry : translatorList.entrySet())
      {
         EditorClientId editorClientId = entry.getKey();
         PersonSessionDetails personSessionDetails = entry.getValue();
         HasManageUserPanel panel = workspaceUsersPresenter.addNewUser(personSessionDetails.getPerson());
         addTranslator(editorClientId, personSessionDetails.getPerson(), personSessionDetails.getSelectedTransUnit(), panel);
      }
   }

   private void addTranslator(EditorClientId editorClientId, Person person, TransUnit selectedTransUnit, HasManageUserPanel panel)
   {
      String color = getColor(editorClientId);

      UserPanelSessionItem item = getUserPanel(editorClientId);
      if (item == null)
      {
         item = new UserPanelSessionItem(panel, person);
         addUser(editorClientId, item);
      }

      item.setSelectedTransUnit(selectedTransUnit);

      item.getPanel().setColor(color);

      updateTranslatorStatus(editorClientId, selectedTransUnit);
   }
}
