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

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Singleton
public class UserSessionService implements TransUnitEditEventHandler
{
   private final HashMap<EditorClientId, UserPanelSessionItem> userSessionMap;
   private final DistinctColor distinctColor;

   @Inject
   public UserSessionService(final EventBus eventBus, DistinctColor distinctColor)
   {
      this.distinctColor = distinctColor;
      userSessionMap = Maps.newHashMap();

      eventBus.addHandler(TransUnitEditEvent.getType(), this);
   }

   @Override
   public void onTransUnitEdit(TransUnitEditEvent event)
   {
      updateTranslatorStatus(event.getEditorClientId(), event.getSelectedTransUnit());
   }

   public void updateTranslatorStatus(EditorClientId editorClientId, TransUnit selectedTransUnit)
   {
      if (userSessionMap.containsKey(editorClientId) && selectedTransUnit != null)
      {
         userSessionMap.get(editorClientId).setSelectedTransUnit(selectedTransUnit);
      }
   }

   public UserPanelSessionItem getUserPanel(EditorClientId editorClientId)
   {
      return userSessionMap.get(editorClientId);
   }

   public void addUser(EditorClientId editorClientId, UserPanelSessionItem item)
   {
      userSessionMap.put(editorClientId, item);
   }

   public void removeUser(EditorClientId editorClientId)
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
}
