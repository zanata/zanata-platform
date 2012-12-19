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
package org.zanata.webtrans.client.view;


import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OptionsView extends Composite implements OptionsDisplay
{
   private static OptionsUiBinder uiBinder = GWT.create(OptionsUiBinder.class);

   interface OptionsUiBinder extends UiBinder<VerticalPanel, OptionsView>
   {
   }

   @UiField
   HTMLPanel optionsContainer;

   @UiField
   Label advancedUserConfig;

   @UiField
   CheckBox showErrorChk;

   @UiField
   ListBox displayThemeList;

   @UiField
   PushButton saveButton;

   @UiField
   PushButton loadButton;

   @UiField
   PushButton restoreToDefaultsButton;

   private OptionsDisplay.Listener listener;

   @Inject
   public OptionsView(WebTransMessages messages)
   {
      initWidget(uiBinder.createAndBindUi(this));
      
      displayThemeList.addItem("Compact", UserConfigHolder.COMPACT_DISPLAY_THEME);
      displayThemeList.addItem("Default", UserConfigHolder.DEFAULT_DISPLAY_THEME);
      displayThemeList.addItem("Loose", UserConfigHolder.LOOSE_DISPLAY_THEME);

      advancedUserConfig.setText(messages.otherConfiguration());

      showErrorChk.setTitle(messages.showErrorsTooltip());

      saveButton.setText(messages.save());
      loadButton.setText(messages.load());
      restoreToDefaultsButton.setText(messages.restoreDefaults());
   }

   @Override
   public void setOptions(Widget optionWidget)
   {
      optionsContainer.clear();
      if (optionWidget != null)
      {
         optionsContainer.add(optionWidget);
      }
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @UiHandler("showErrorChk")
   public void onShowErrorOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onShowErrorsOptionChanged(showErrorChk.getValue());
   }

   @UiHandler("displayThemeList")
   public void onDisplayThemeListChanged(ChangeEvent event)
   {
      listener.onDisplayThemeChanged(displayThemeList.getValue(displayThemeList.getSelectedIndex()));
   }

   @UiHandler("saveButton")
   public void onSaveButtonClick( ClickEvent event )
   {
      listener.persistOptionChange();
   }

   @UiHandler("loadButton")
   public void onLoadButtonClick( ClickEvent event )
   {
      listener.loadOptions();
   }

   @UiHandler("restoreToDefaultsButton")
   public void onRestoreToDefaultsButtonClick( ClickEvent event )
   {
      listener.loadDefaultOptions();
   }

   @Override
   public void setShowErrorChk(boolean showError)
   {
      showErrorChk.setValue(showError, true);
   }

   @Override
   public void setDisplayTheme(String displayTheme)
   {
      if (displayTheme.equals(UserConfigHolder.COMPACT_DISPLAY_THEME))
      {
         displayThemeList.setSelectedIndex(0);
      }
      else if (displayTheme.equals(UserConfigHolder.LOOSE_DISPLAY_THEME))
      {
         displayThemeList.setSelectedIndex(2);
      }
      else
      {
         displayThemeList.setSelectedIndex(1);
      }

   }
}
