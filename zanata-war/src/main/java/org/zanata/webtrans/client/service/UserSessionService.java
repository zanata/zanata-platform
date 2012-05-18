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
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;

import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.user.client.Random;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Singleton
public class UserSessionService implements TransUnitEditEventHandler
{
   private final HashMap<Person, UserPanelSessionItem> userSessionMap;

   public final HashMap<String, String> colorListMap;

   private static final int DARK_BOUND = 100;
   private static final int BRIGHT_BOUND = 400;

   @Inject
   public UserSessionService(final EventBus eventBus)
   {
      userSessionMap = new HashMap<Person, UserPanelSessionItem>();
      
      colorListMap = new HashMap<String, String>();

      eventBus.addHandler(TransUnitEditEvent.getType(), this);
   }

   @Override
   public void onTransUnitEdit(TransUnitEditEvent event)
   {
      updateTranslatorStatus(event.getPerson(), event.getSelectedTransUnit());
   }

   public void updateTranslatorStatus(Person person, TransUnit selectedTransUnit)
   {
      if (userSessionMap.containsKey(person) && selectedTransUnit != null)
      {
         userSessionMap.get(person).setSelectedTransUnit(selectedTransUnit);
      }
   }

   public int getTranslatorsSize()
   {
      return userSessionMap.size();
   }

   public UserPanelSessionItem getUserPanel(Person person)
   {
      return userSessionMap.get(person);
   }

   public void addUser(Person person, UserPanelSessionItem item)
   {
      userSessionMap.put(person, item);
   }

   public void removeUser(Person person)
   {
      userSessionMap.remove(person);
   }

   public Map<Person, UserPanelSessionItem> getUserSessionMap()
   {
      return userSessionMap;
   }

   public String getColor(String sessionId)
   {

      if (colorListMap.containsKey(sessionId))
      {
         return colorListMap.get(sessionId);
      }

      String color = null;

      while (colorListMap.containsValue(color) || color == null || color.equals("rgb(0,0,0)") || color.equals("rgb(255,255,255)"))
      {
         color = generateNewColor();
      }

      colorListMap.put(sessionId, color);

      return color;
   }

   private String generateNewColor()
   {
      int total = 0;

      int rndRedColor = 0;
      int rndGreenColor = 0;
      int rndBlueColor = 0;

      while (total < DARK_BOUND || total > BRIGHT_BOUND)
      {
         rndRedColor = Random.nextInt(255) / 2;
         rndGreenColor = Random.nextInt(255);
         rndBlueColor = Random.nextInt(255);
         total = rndRedColor + rndGreenColor + rndBlueColor;
      }

      CssColor randomColor = CssColor.make(rndRedColor, rndGreenColor, rndBlueColor);
      return randomColor.value();
   }
}
