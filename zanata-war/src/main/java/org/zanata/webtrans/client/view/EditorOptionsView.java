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


import org.zanata.webtrans.client.presenter.EditorOptionsPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.EnumRadioButtonGroup;
import org.zanata.webtrans.client.ui.NavOptionRenderer;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditorOptionsView extends Composite implements EditorOptionsPresenter.Display
{
   private static EditorOptionsUiBinder uiBinder = GWT.create(EditorOptionsUiBinder.class);
   private final EnumRadioButtonGroup<NavOption> navOptionGroup;

   @UiField
   LayoutPanel container;

   @UiField
   CheckBox translatedChk, needReviewChk, untranslatedChk, enterChk, escChk, editorButtonsChk;

   @UiField
   Label navOptionHeader, editorOptionHeader, filterHeader;

   @UiField
   VerticalPanel optionsContainer;

   @Inject
   public EditorOptionsView(WebTransMessages messages, NavOptionRenderer navOptionRenderer)
   {
      initWidget(uiBinder.createAndBindUi(this));
      navOptionGroup = new EnumRadioButtonGroup<NavOption>("navOption", NavOption.class, navOptionRenderer);
      navOptionGroup.addToContainer(optionsContainer);

      editorOptionHeader.setText(messages.editorOptions());
      filterHeader.setText(messages.messageFilters());
      navOptionHeader.setText(messages.navOption());
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
   public void setNavOptionVisible(boolean visible)
   {
      navOptionHeader.setVisible(visible);
      optionsContainer.setVisible(visible);
   }

   @Override
   public void setNavOptionHandler(EnumRadioButtonGroup.SelectionChangeListener<NavOption> listener)
   {
      navOptionGroup.setSelectionChangeListener(listener);
      navOptionGroup.setDefaultSelected(NavOption.FUZZY_UNTRANSLATED);
   }

   interface EditorOptionsUiBinder extends UiBinder<Widget, EditorOptionsView>
   {
   }
}
