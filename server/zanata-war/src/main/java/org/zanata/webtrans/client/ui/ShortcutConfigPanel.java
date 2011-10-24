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

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
   private final CheckBox enterChk = new CheckBox(UserConfigConstants.LABEL_ENTER_BUTTON_SAVE);
   private final CheckBox escChk = new CheckBox(UserConfigConstants.LABEL_ESC_KEY_CLOSE);
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
      mainPanel.add(new Label(UserConfigConstants.LABEL_NAV_OPTION));

      optionsList.clear();
      optionsList.addItem(UserConfigConstants.OPTION_FUZZY_UNTRANSLATED);
      optionsList.addItem(UserConfigConstants.OPTION_FUZZY);
      optionsList.addItem(UserConfigConstants.OPTION_UNTRANSLATED);
      mainPanel.add(optionsList);

      HorizontalPanel enteroptHP = new HorizontalPanel();
      enteroptHP.setSpacing(5);
      enteroptHP.add(enterChk);
      mainPanel.add(enteroptHP);

      HorizontalPanel escoptHP = new HorizontalPanel();
      escoptHP.setSpacing(5);
      escoptHP.add(escChk);
      mainPanel.add(escoptHP);

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
            if (selectedOption.equals(UserConfigConstants.OPTION_FUZZY_UNTRANSLATED))
            {
               configMap.put(UserConfigConstants.BUTTON_UNTRANSLATED, true);
               configMap.put(UserConfigConstants.BUTTON_FUZZY, true);
            }
            else if (selectedOption.equals(UserConfigConstants.OPTION_FUZZY))
            {
               configMap.put(UserConfigConstants.BUTTON_FUZZY, true);
               configMap.put(UserConfigConstants.BUTTON_UNTRANSLATED, false);
            }
            else if (selectedOption.equals(UserConfigConstants.OPTION_UNTRANSLATED))
            {
               configMap.put(UserConfigConstants.BUTTON_FUZZY, false);
               configMap.put(UserConfigConstants.BUTTON_UNTRANSLATED, true);
            }
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      });

      enterChk.addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Enter' Key to save and move to next string: " + event.getValue());
            configMap.put(UserConfigConstants.BUTTON_ENTER, event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      });

      escChk.addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Esc' Key to close editor: " + event.getValue());
            configMap.put(UserConfigConstants.BUTTON_ESC, event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent(configMap));
         }
      });
   }

   private void setDefaultValue()
   {
      optionsList.setSelectedIndex(0);
      enterChk.setValue(false);
      escChk.setValue(false);

      configMap.put(UserConfigConstants.BUTTON_FUZZY, true);
      configMap.put(UserConfigConstants.BUTTON_UNTRANSLATED, true);
      configMap.put(UserConfigConstants.BUTTON_ENTER, false);
      configMap.put(UserConfigConstants.BUTTON_ESC, false);
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


 