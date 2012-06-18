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

import org.zanata.webtrans.shared.rpc.MergeOption;
import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TMMergeForm extends Composite
{
   private static TMMergeFormUiBinder uiBinder = GWT.create(TMMergeFormUiBinder.class);

   private TransMemoryMergePopupPanelDisplay.Listener listener;

   @UiField
   ListBox matchThreshold;
   @UiField
   Button confirmButton;
   @UiField
   Button cancelButton;
   @UiField(provided = true)
   EnumListBox<MergeOption> differentProject;
   @UiField(provided = true)
   EnumListBox<MergeOption> differentDocument;
   @UiField(provided = true)
   EnumListBox<MergeOption> differentResId;

   @Inject
   public TMMergeForm(MergeOptionRenderer mergeOptionRenderer)
   {
      differentProject = new EnumListBox<MergeOption>(MergeOption.class, mergeOptionRenderer);
      differentDocument = new EnumListBox<MergeOption>(MergeOption.class, mergeOptionRenderer);
      differentResId = new EnumListBox<MergeOption>(MergeOption.class, mergeOptionRenderer);
      initWidget(uiBinder.createAndBindUi(this));
   }

   public void setListener(TransMemoryMergePopupPanelDisplay.Listener listener)
   {
      this.listener = listener;
   }

   @UiHandler("confirmButton")
   public void onConfirmButtonClick(ClickEvent event)
   {
      Preconditions.checkNotNull(listener, "Do you forget to call setListener on TMMergeForm?");
      listener.proceedToMergeTM(getSelectedMatchThreshold(), differentProject.getValue(), differentDocument.getValue(), differentResId.getValue());
   }

   private String getSelectedMatchThreshold()
   {
      return matchThreshold.getValue(matchThreshold.getSelectedIndex());
   }

   @UiHandler("cancelButton")
   public void onCancelButtonClick(ClickEvent event)
   {
      listener.cancelMergeTM();
   }

   interface TMMergeFormUiBinder extends UiBinder<VerticalPanel, TMMergeForm>
   {
   }
}