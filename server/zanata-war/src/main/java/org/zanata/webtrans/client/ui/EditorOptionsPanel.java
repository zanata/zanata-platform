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

import org.zanata.webtrans.client.resources.EditorConfigConstants;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class EditorOptionsPanel extends Composite
{
   private static EditorOptionsUiBinder uiBinder = GWT.create(EditorOptionsUiBinder.class);

   interface EditorOptionsUiBinder extends UiBinder<Widget, EditorOptionsPanel>
   {
   }

   @UiField
   VerticalPanel contentPanel;

   @UiField
   Label header, navOptionHeader, filterHeader;

   @UiField
   CheckBox enterChk, escChk, editorButtonsChk;
   
   @UiField
   CheckBox translatedChk, needReviewChk, untranslatedChk;

   @UiField
   ListBox optionsList;

   public EditorOptionsPanel()
   {
      initWidget(uiBinder.createAndBindUi(this));

      header.setText(EditorConfigConstants.LABEL_EDITOR_OPTIONS);
      enterChk.setText(EditorConfigConstants.LABEL_ENTER_BUTTON_SAVE);
      escChk.setText(EditorConfigConstants.LABEL_ESC_KEY_CLOSE);
      editorButtonsChk.setText(EditorConfigConstants.LABEL_EDITOR_BUTTONS);
      navOptionHeader.setText(EditorConfigConstants.LABEL_NAV_OPTION);

      enterChk.setValue(false);
      escChk.setValue(false);
      editorButtonsChk.setValue(true);

      translatedChk.setText(EditorConfigConstants.LABEL_TRANSLATED);
      needReviewChk.setText(EditorConfigConstants.LABEL_NEED_REVIEW);
      untranslatedChk.setText(EditorConfigConstants.LABEL_UNTRANSLATED);
      filterHeader.setText(EditorConfigConstants.LABEL_FILTERS);

      optionsList.addItem(EditorConfigConstants.OPTION_FUZZY_UNTRANSLATED);
      optionsList.addItem(EditorConfigConstants.OPTION_FUZZY);
      optionsList.addItem(EditorConfigConstants.OPTION_UNTRANSLATED);

      optionsList.setSelectedIndex(0);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   public CheckBox getTranslatedChk()
   {
      return translatedChk;
   }

   public CheckBox getNeedReviewChk()
   {
      return needReviewChk;
   }

   public CheckBox getUntranslatedChk()
   {
      return untranslatedChk;
   }

   public CheckBox getEditorButtonsChk()
   {
      return editorButtonsChk;
   }

   public CheckBox getEnterChk()
   {
      return enterChk;
   }

   public CheckBox getEscChk()
   {
      return escChk;
   }

   public ListBox getOptionsList()
   {
      return optionsList;
   }

   public void setNavOptionVisible(boolean visible)
   {
      navOptionHeader.setVisible(visible);
      optionsList.setVisible(visible);
   }
}


 