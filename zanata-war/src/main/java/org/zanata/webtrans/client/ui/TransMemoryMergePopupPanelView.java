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

import org.zanata.webtrans.client.resources.UiMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransMemoryMergePopupPanelView implements TransMemoryMergePopupPanelDisplay
{

   interface TMIMergeUiBinder extends UiBinder<DialogBox, TransMemoryMergePopupPanelView>
   {
   }

   private final TMMergeForm TMMergeForm;
   private final Label processingLabel;

   private DialogBox dialogBox;

   private static TMIMergeUiBinder uiBinder = GWT.create(TMIMergeUiBinder.class);



   @Inject
   public TransMemoryMergePopupPanelView(TMMergeForm TMMergeForm, UiMessages messages)
   {
      //auto hide false, modal true

      dialogBox = uiBinder.createAndBindUi(this);
      dialogBox.setText(messages.mergeTMCaption());
      dialogBox.setGlassEnabled(true);
      dialogBox.ensureDebugId("TMMerge");
      dialogBox.setAutoHideEnabled(false);
      dialogBox.setModal(true);

      VerticalPanel main = new VerticalPanel();
      main.add(TMMergeForm);
      processingLabel = new Label(messages.processing());
      main.add(processingLabel);
      dialogBox.add(main);
      this.TMMergeForm = TMMergeForm;
      processingLabel.setVisible(false);
      hide();
   }

   @Override
   public void setListener(Listener listener)
   {
      TMMergeForm.setListener(listener);
   }

   @Override
   public void showProcessing()
   {
      TMMergeForm.setVisible(false);
      processingLabel.setVisible(true);
   }

   @Override
   public void showForm()
   {
      processingLabel.setVisible(false);
      TMMergeForm.setVisible(true);
      dialogBox.center();
   }

   @Override
   public Widget asWidget()
   {
      return dialogBox;
   }

   @Override
   public void hide()
   {
      dialogBox.hide();

   }
}
