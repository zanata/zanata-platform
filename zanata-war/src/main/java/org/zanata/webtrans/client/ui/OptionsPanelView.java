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
package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.presenter.OptionsPanelPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class OptionsPanelView extends Composite implements OptionsPanelPresenter.Display
{
   private static OptionsPanelUiBinder uiBinder = GWT.create(OptionsPanelUiBinder.class);

   interface OptionsPanelUiBinder extends UiBinder<SplitLayoutPanel, OptionsPanelView>
   {
   }

   @UiField
   SplitLayoutPanel mainPanel;

   @UiField
   LayoutPanel editorOptionsContainer, validationOptionsContainer;

   @UiField
   CheckBox translatedChk, needReviewChk, untranslatedChk, enterChk, escChk, editorButtonsChk;

   @UiField
   Label navOptionHeader;

   @UiField
   ListBox optionsList;

   private double validationPanelHeight = 200;

   @Inject
   public OptionsPanelView(ValidationOptionsPresenter.Display validationOptionsView)
   {
      initWidget(uiBinder.createAndBindUi(this));
      validationOptionsContainer.add(validationOptionsView.asWidget());

      populateOptionsList();
   }

   private void populateOptionsList()
   {
      // TODO localise strings
      optionsList.addItem("Next Fuzzy/Untranslated", KEY_FUZZY_UNTRANSLATED);
      optionsList.addItem("Next Fuzzy", KEY_FUZZY);
      optionsList.addItem("Next Untranslated", KEY_UNTRANSLATED);

      optionsList.setSelectedIndex(0);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public HasValue<Boolean> getTranslatedChk()
   {
      return translatedChk;
   }

   @Override
   public HasValue<Boolean> getNeedReviewChk()
   {
      return needReviewChk;
   }

   @Override
   public HasValue<Boolean> getUntranslatedChk()
   {
      return untranslatedChk;
   }

   @Override
   public HasValue<Boolean> getEditorButtonsChk()
   {
      return editorButtonsChk;
   }

   @Override
   public HasValue<Boolean> getEnterChk()
   {
      return enterChk;
   }

   @Override
   public HasValue<Boolean> getEscChk()
   {
      return escChk;
   }

   @Override
   public void setEditorOptionsVisible(boolean visible)
   {
      editorButtonsChk.setVisible(visible);
      enterChk.setVisible(visible);
      escChk.setVisible(visible);
   }

   @Override
   public void setNavOptionVisible(boolean visible)
   {
      navOptionHeader.setVisible(visible);
      optionsList.setVisible(visible);
   }

   @Override
   public void setValidationOptionsVisible(boolean visible)
   {
      mainPanel.forceLayout();

      Widget splitter = SplitLayoutPanelHelper.getAssociatedSplitter(mainPanel, validationOptionsContainer);
      if (visible)
      {
         SplitLayoutPanelHelper.setSplitPosition(mainPanel, validationOptionsContainer, validationPanelHeight);
      }
      else
      {
         validationPanelHeight = mainPanel.getWidgetContainerElement(validationOptionsContainer).getOffsetHeight();
         SplitLayoutPanelHelper.setSplitPosition(mainPanel, validationOptionsContainer, 0);
      }
      splitter.setVisible(visible);

      mainPanel.animate(200);
   }

   @Override
   public HasChangeHandlers getModalNavigationOptionsSelect()
   {
      return optionsList;
   }

   @Override
   public String getSelectedFilter()
   {
      return optionsList.getValue(optionsList.getSelectedIndex());
   }


}
