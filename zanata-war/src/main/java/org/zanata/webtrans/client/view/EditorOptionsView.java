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
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.EnumRadioButtonGroup;
import org.zanata.webtrans.client.ui.NavOptionRenderer;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EditorOptionsView extends Composite implements EditorOptionsDisplay
{
   private static EditorOptionsUiBinder uiBinder = GWT.create(EditorOptionsUiBinder.class);
   private final EnumRadioButtonGroup<NavOption> navOptionGroup;
   
   @UiField
   CheckBox enterChk, editorButtonsChk;
   
   @UiField
   Label navOptionHeader, editorOptionHeader;
   
   @UiField
   VerticalPanel optionsContainer;
   @UiField
   Label pageSizeHeader;
   @UiField
   InlineLabel five;
   @UiField
   InlineLabel ten;
   @UiField
   InlineLabel fifty;
   @UiField
   InlineLabel twentyFive;
   @UiField
   Styles style;
   @UiField
   CheckBox useCodeMirrorChk;
   @UiField
   CheckBox showSaveApprovedWarningChk;
   @UiField
   CheckBox spellCheckChk;
   @UiField
   Label transMemoryHeader;
   @UiField
   RadioButton diffModeDiff;
   @UiField
   RadioButton diffModeHighlight;
   @UiField
   Label displayHeader;
   @UiField
   CheckBox showTMChk;
   @UiField
   CheckBox showGlossaryChk;
   @UiField
   CheckBox showOptionalTransUnitDetailsChk;
   
   @UiField
   CheckBox enableReferenceForSourceLang;
   
   private Listener listener;

   @Inject
   public EditorOptionsView(WebTransMessages messages, NavOptionRenderer navOptionRenderer, UiMessages uiMessages)
   {
      initWidget(uiBinder.createAndBindUi(this));
      navOptionGroup = new EnumRadioButtonGroup<NavOption>("navOption", NavOption.class, navOptionRenderer);
      navOptionGroup.addToContainer(optionsContainer);

      editorOptionHeader.setText(messages.editorOptions());
      navOptionHeader.setText(messages.navOption());
      pageSizeHeader.setText(messages.pageSize());
      transMemoryHeader.setText(messages.transMemoryOption());

      useCodeMirrorChk.setTitle(messages.useCodeMirrorEditorTooltip());
      showSaveApprovedWarningChk.setTitle(messages.showSaveApprovedWarningTooltip());
      // TODO at the moment browser spell check only works in Firefox. If later Chrome supports it then change the tooltip.
      spellCheckChk.setTitle(messages.spellCheckTooltip());

      diffModeDiff.setText(uiMessages.diffModeAsDiff());
      diffModeHighlight.setText(uiMessages.diffModeAsHighlight());
      diffModeDiff.setValue(true);

      displayHeader.setText(messages.displayConfiguration());
      displayHeader.setTitle(messages.displayConfigurationTooltip());
      showTMChk.setText(messages.showTranslationMemoryPanel());
      showGlossaryChk.setText(messages.showGlossaryPanel());
      showOptionalTransUnitDetailsChk.setText(messages.showTransUnitDetails());
      showOptionalTransUnitDetailsChk.setTitle(messages.showTransUnitDetailsTooltip());
      enableReferenceForSourceLang.setText(messages.enableReferenceForSourceLang());
      enableReferenceForSourceLang.setTitle(messages.enableReferenceForSourceLangTooltip());
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @UiHandler("five")
   public void onPageSizeFiveClicked(ClickEvent event)
   {
      selectFive();
      listener.onPageSizeClick(5);
   }

   private void selectFive()
   {
      five.setStyleName(style.selectedPageSize());
      ten.removeStyleName(style.selectedPageSize());
      twentyFive.removeStyleName(style.selectedPageSize());
      fifty.removeStyleName(style.selectedPageSize());
   }

   @UiHandler("ten")
   public void onPageSizeTenClicked(ClickEvent event)
   {
      selectTen();
      listener.onPageSizeClick(10);
   }

   private void selectTen()
   {
      five.removeStyleName(style.selectedPageSize());
      ten.setStyleName(style.selectedPageSize());
      twentyFive.removeStyleName(style.selectedPageSize());
      fifty.removeStyleName(style.selectedPageSize());
   }

   @UiHandler("twentyFive")
   public void onPageSizeTwentyFiveClicked(ClickEvent event)
   {
      selectTwentyFive();
      listener.onPageSizeClick(25);
   }

   private void selectTwentyFive()
   {
      five.removeStyleName(style.selectedPageSize());
      ten.removeStyleName(style.selectedPageSize());
      twentyFive.setStyleName(style.selectedPageSize());
      fifty.removeStyleName(style.selectedPageSize());
   }

   @UiHandler("fifty")
   public void onPageSizeFiftyClicked(ClickEvent event)
   {
      selectFifty();
      listener.onPageSizeClick(50);
   }

   private void selectFifty()
   {
      five.removeStyleName(style.selectedPageSize());
      ten.removeStyleName(style.selectedPageSize());
      twentyFive.removeStyleName(style.selectedPageSize());
      fifty.setStyleName(style.selectedPageSize());
   }

   @UiHandler("editorButtonsChk")
   public void onEditorButtonsOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onEditorButtonsOptionChanged(editorButtonsChk.getValue());
   }

   @UiHandler("enterChk")
   public void onEnterSaveOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onEnterSaveOptionChanged(enterChk.getValue());
   }

   @UiHandler("useCodeMirrorChk")
   public void onCodeMirrorOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onUseCodeMirrorOptionChanged(useCodeMirrorChk.getValue());
   }

   @UiHandler("showSaveApprovedWarningChk")
   public void onShowSaveApprovedWarningChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onShowSaveApprovedWarningChanged(showSaveApprovedWarningChk.getValue());
   }

   @UiHandler("spellCheckChk")
   public void onSpellCheckChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onSpellCheckOptionChanged(spellCheckChk.getValue());
   }

   @UiHandler({"diffModeDiff", "diffModeHighlight"})
   public void onDiffModeOptionChange(ValueChangeEvent<Boolean> event)
   {
      if (diffModeDiff.getValue())
      {
         listener.onTransMemoryDisplayModeChanged(DiffMode.NORMAL);
      }
      else
      {
         listener.onTransMemoryDisplayModeChanged(DiffMode.HIGHLIGHT);
      }
   }

   @UiHandler({"showTMChk", "showGlossaryChk"})
   public void onTMOrGlossaryDisplayOptionsChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onTMOrGlossaryDisplayOptionsChanged(showTMChk.getValue(), showGlossaryChk.getValue());
   }

   @UiHandler("showOptionalTransUnitDetailsChk")
   public void onDisplayTransUnitDetailsOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onDisplayTransUnitDetailsOptionChanged(showOptionalTransUnitDetailsChk.getValue());
   }

   @UiHandler("enableReferenceForSourceLang")
   public void onEnableReferenceForSourceLangOptionChanged(ValueChangeEvent<Boolean> event)
   {
      listener.onEnableReferenceForSourceLangOptionChanged(enableReferenceForSourceLang.getValue());
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
      navOptionGroup.setSelectionChangeListener(listener);
   }

   @Override
   public void setOptionsState(UserConfigHolder.ConfigurationState state)
   {
      enterChk.setValue(state.isEnterSavesApproved());
      editorButtonsChk.setValue(state.isDisplayButtons());

      navOptionGroup.setDefaultSelected(state.getNavOption());
      selectPageSize(state.getEditorPageSize());
      useCodeMirrorChk.setValue(state.isUseCodeMirrorEditor());
      showSaveApprovedWarningChk.setValue(state.isShowSaveApprovedWarning());
      spellCheckChk.setValue(state.isSpellCheckEnabled());

      if (state.getTransMemoryDisplayMode() == DiffMode.NORMAL)
      {
         diffModeDiff.setValue(true);
      }
      else
      {
         diffModeHighlight.setValue(true);
      }

      showTMChk.setValue(state.isShowTMPanel());
      showGlossaryChk.setValue(state.isShowGlossaryPanel());
      showOptionalTransUnitDetailsChk.setValue(state.isShowOptionalTransUnitDetails());
      enableReferenceForSourceLang.setValue(state.isEnabledReferenceForSourceLang());
   }

   private void selectPageSize(int pageSize)
   {
      if (pageSize == 5)
      {
         selectFive();
      }
      else if (pageSize == 10)
      {
         selectTen();
      }
      else if (pageSize == 25)
      {
         selectTwentyFive();
      }
      else if (pageSize ==50)
      {
         selectFifty();
      }
   }

   interface EditorOptionsUiBinder extends UiBinder<VerticalPanel, EditorOptionsView>
   {
   }

   interface Styles extends CssResource
   {
      String selectedPageSize();

      String mainPanel();

      String pageSizeContainer();
   }
}
