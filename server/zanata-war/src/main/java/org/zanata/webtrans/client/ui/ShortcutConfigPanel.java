/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.UserConfigChangeEvent;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.UIObject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class ShortcutConfigPanel extends DecoratedPopupPanel
{
   private ListBox optionsList = new ListBox();

   private Map<String, Boolean> configMap = new HashMap<String, Boolean>();

   private EventBus eventBus;

   public ShortcutConfigPanel(boolean autoHide, EventBus eventBus)
   {
      super(autoHide);
      this.eventBus = eventBus;
      init();
      bindEvent();
      setDefaultValue();
   }

   private void init()
   {
      FlowPanel mainPanel = new FlowPanel();
      mainPanel.add(new Label(EditorConfigConstants.LABEL_NAV_OPTION));

      optionsList.clear();
      optionsList.addItem(EditorConfigConstants.OPTION_FUZZY_UNTRANSLATED);
      optionsList.addItem(EditorConfigConstants.OPTION_FUZZY);
      optionsList.addItem(EditorConfigConstants.OPTION_UNTRANSLATED);
      mainPanel.add(optionsList);

      this.add(mainPanel);
   }

   private void bindEvent()
   {
      optionsList.addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent event)
         {
            String selectedOption = optionsList.getItemText(optionsList.getSelectedIndex());
            if (selectedOption.equals(EditorConfigConstants.OPTION_FUZZY_UNTRANSLATED))
            {
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
            }
            else if (selectedOption.equals(EditorConfigConstants.OPTION_FUZZY))
            {
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, false);
            }
            else if (selectedOption.equals(EditorConfigConstants.OPTION_UNTRANSLATED))
            {
               configMap.put(EditorConfigConstants.BUTTON_FUZZY, false);
               configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
            }
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      });
   }

   private void setDefaultValue()
   {
      optionsList.setSelectedIndex(0);

      configMap.put(EditorConfigConstants.BUTTON_FUZZY, true);
      configMap.put(EditorConfigConstants.BUTTON_UNTRANSLATED, true);
   }

   public void toggleDisplay(final UIObject target)
   {
      if (!isShowing())
      {
         showRelativeTo(target);
      }
       else if (isShowing())
       {
         hide();
       }
   }
}


 