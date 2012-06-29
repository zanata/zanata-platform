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
import org.zanata.webtrans.shared.rpc.MergeOption;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
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
   HorizontalPanel differentProject, differentDocument, differentResId;

   @UiField
   InlineLabel differentProjectStatus, differentDocIdStatus, differentResIdStatus;

   @UiField
   EnumMessages enumMessages;
   @UiField
   Styles style;

   private final EnumRadioButtonGroup<MergeOption> projectOptionGroup;
   private final EnumRadioButtonGroup<MergeOption> docIdOptionGroup;
   private final EnumRadioButtonGroup<MergeOption> resIdOptionGroup;
   private final MergeStatusRenderer mergeStatusRenderer;

   private TransMemoryMergePopupPanelDisplay.Listener listener;

   @Inject
   public TMMergeForm(MergeOptionRenderer mergeOptionRenderer, MergeStatusRenderer mergeStatusRenderer)
   {
      this.mergeStatusRenderer = mergeStatusRenderer;
      initWidget(uiBinder.createAndBindUi(this));

      projectOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.PROJECT_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      projectOptionGroup.setSelectionChangeListener(this);
      projectOptionGroup.addToContainer(differentProject).setDefaultSelected(MergeOption.FUZZY);

      docIdOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.DOC_ID_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      docIdOptionGroup.setSelectionChangeListener(this);
      docIdOptionGroup.addToContainer(differentDocument).setDefaultSelected(MergeOption.FUZZY);

      resIdOptionGroup = new EnumRadioButtonGroup<MergeOption>(OptionType.RES_ID_MISMATCH.name(), MergeOption.class, mergeOptionRenderer);
      resIdOptionGroup.setSelectionChangeListener(this);
      resIdOptionGroup.addToContainer(differentResId).setDefaultSelected(MergeOption.FUZZY);
   }

   public void setListener(TransMemoryMergePopupPanelDisplay.Listener listener)
   {
      this.listener = listener;
   }

   @UiHandler("confirmButton")
   public void onConfirmButtonClick(ClickEvent event)
   {
      Preconditions.checkNotNull(listener, "Do you forget to call setListener on TMMergeForm?");
      listener.proceedToMergeTM(getSelectedMatchThreshold(), projectOptionGroup.getSelected(), docIdOptionGroup.getSelected(), resIdOptionGroup.getSelected());
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

   @Override
   public void onSelectionChange(String groupName, MergeOption option)
   {
      OptionType optionType = OptionType.valueOf(groupName);
      switch (optionType)
      {
         case PROJECT_MISMATCH:
            differentProjectStatus.setText(mergeStatusRenderer.render(option));
            differentProjectStatus.setStyleName(resolveStyle(option));
            break;
         case DOC_ID_MISMATCH:
            differentDocIdStatus.setText(mergeStatusRenderer.render(option));
            differentDocIdStatus.setStyleName(resolveStyle(option));
            break;
         case RES_ID_MISMATCH:
            differentResIdStatus.setText(mergeStatusRenderer.render(option));
            differentResIdStatus.setStyleName(resolveStyle(option));
            break;
      }
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
      PROJECT_MISMATCH, DOC_ID_MISMATCH, RES_ID_MISMATCH
   }
}