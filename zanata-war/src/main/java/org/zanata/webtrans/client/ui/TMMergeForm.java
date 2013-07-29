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

package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.EnumMessages;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.shared.rpc.MergeOption;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeForm extends Composite implements EnumRadioButtonGroup.SelectionChangeListener<MergeOption>
{
   private static TMMergeFormUiBinder uiBinder = GWT.create(TMMergeFormUiBinder.class);

   @UiField
   ListBox matchThreshold;
   @UiField
   Button confirmButton, cancelButton;

   @UiField
   HorizontalPanel differentProject, differentDocument, differentContext, importedMatchPanel;

   @UiField
   InlineLabel differentProjectStatus, differentDocIdStatus, differentContextStatus, importedMatchStatus;

   @UiField
   EnumMessages enumMessages;
   @UiField
   Styles style;
   @UiField
   InlineLabel differentContentStatus;
   @UiField
   Label differentContentLabel;
   @UiField
   UiMessages messages;

   private final EnumRadioButtonGroup<MergeOption> projectOptionGroup;
   private final EnumRadioButtonGroup<MergeOption> docIdOptionGroup;
   private final EnumRadioButtonGroup<MergeOption> contextOptionGroup;
   private final EnumRadioButtonGroup<MergeOption> importedMatchOptionGroup;
   private final MergeStatusRenderer mergeStatusRenderer;

   private TransMemoryMergePopupPanelDisplay.Listener listener;

   @Inject
   public TMMergeForm(MergeOptionRenderer mergeOptionRenderer, MergeStatusRenderer mergeStatusRenderer)
   {
      this.mergeStatusRenderer = mergeStatusRenderer;
      initWidget(uiBinder.createAndBindUi(this));

      matchThreshold.setItemText(0, messages.identical());

      projectOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.PROJECT_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      projectOptionGroup.setSelectionChangeListener(this);
      projectOptionGroup.addToContainer(differentProject).setDefaultSelected(MergeOption.FUZZY);

      docIdOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.DOC_ID_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      docIdOptionGroup.setSelectionChangeListener(this);
      docIdOptionGroup.addToContainer(differentDocument).setDefaultSelected(MergeOption.FUZZY);

      contextOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.CTX_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      contextOptionGroup.setSelectionChangeListener(this);
      contextOptionGroup.addToContainer(differentContext).setDefaultSelected(MergeOption.FUZZY);

      importedMatchOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.IMPORTED_MATCH.name(), MergeOption.class, mergeOptionRenderer);
      importedMatchOptionGroup.setSelectionChangeListener(this);
      importedMatchOptionGroup.addToContainer(importedMatchPanel).setDefaultSelected(MergeOption.FUZZY);
   }

   public void setListener(TransMemoryMergePopupPanelDisplay.Listener listener)
   {
      this.listener = listener;
   }

   @UiHandler("confirmButton")
   public void onConfirmButtonClick(ClickEvent event)
   {
      Preconditions.checkNotNull(listener, "Do you forget to call setListener on TMMergeForm?");
      listener.proceedToMergeTM(getSelectedMatchThreshold(), projectOptionGroup.getSelected(), docIdOptionGroup.getSelected(),
            contextOptionGroup.getSelected(), importedMatchOptionGroup.getSelected());
   }

   private int getSelectedMatchThreshold()
   {
      String percent = matchThreshold.getValue(matchThreshold.getSelectedIndex());
      return Integer.valueOf(percent);
   }

   @UiHandler("cancelButton")
   public void onCancelButtonClick(ClickEvent event)
   {
      listener.cancelMergeTM();
   }

   @UiHandler("matchThreshold")
   public void onThresholdPercentChange(ChangeEvent event)
   {
      if (getSelectedMatchThreshold() == 100)
      {
         differentContentStatus.setStyleName(style.reject_action());
         differentContentStatus.setText(enumMessages.rejectMerge());
         differentContentLabel.setText(enumMessages.rejectMerge());
      }
      else
      {
         differentContentStatus.setStyleName(style.downgrade_action());
         differentContentStatus.setText(enumMessages.downgradeToFuzzy());
         differentContentLabel.setText(enumMessages.downgradeToFuzzy());
      }
   }

   @Override
   public void onSelectionChange(String groupName, MergeOption option)
   {
      OptionType optionType = OptionType.valueOf(groupName);
      switch (optionType)
      {
         case PROJECT_MISMATCH:
            setTextAndStyle(differentProjectStatus, option);
            break;
         case DOC_ID_MISMATCH:
            setTextAndStyle(differentDocIdStatus, option);
            break;
         case CTX_MISMATCH:
            setTextAndStyle(differentContextStatus, option);
            break;
         case IMPORTED_MATCH:
            setTextAndStyle(importedMatchStatus, option);
            break;
      }
   }

   private void setTextAndStyle(InlineLabel label, MergeOption option)
   {
      label.setText(mergeStatusRenderer.render(option));
      label.setStyleName(resolveStyle(option));
   }

   private String resolveStyle(MergeOption option)
   {
      switch (option)
      {
         case FUZZY:
            return style.downgrade_action();
         case REJECT:
            return style.reject_action();
         case IGNORE_CHECK:
            return style.ignore_action();
      }
      return style.approved_action();
   }

   interface TMMergeFormUiBinder extends UiBinder<Grid, TMMergeForm>
   {
   }

   interface Styles extends CssResource
   {
      String reject_action();

      String approved_action();

      String ignore_action();

      String downgrade_action();

      String header();

      String tmMergeTable();
   }

   enum OptionType
   {
      PROJECT_MISMATCH, DOC_ID_MISMATCH, CTX_MISMATCH, IMPORTED_MATCH
   }
}