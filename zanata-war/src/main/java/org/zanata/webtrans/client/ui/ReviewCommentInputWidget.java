/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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

import org.zanata.webtrans.client.view.ForceReviewCommentDisplay;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextArea;

public class ReviewCommentInputWidget extends Composite
{
   private static ReviewCommentInputWidgetUiBinder ourUiBinder = GWT.create(ReviewCommentInputWidgetUiBinder.class);
   @UiField
   TextArea commentTextArea;
   @UiField
   Button addCommentButton;
   private ForceReviewCommentDisplay.Listener listener;

   public ReviewCommentInputWidget()
   {
      initWidget(ourUiBinder.createAndBindUi(this));
      commentTextArea.getElement().setAttribute("placeholder", "Add a comment...");
   }

   @UiHandler("addCommentButton")
   public void onAddCommentButtonClick(ClickEvent event)
   {
      if (!commentTextArea.getValue().trim().isEmpty())
      {
         listener.addComment(commentTextArea.getText());
      }
   }

   public void setListener(ForceReviewCommentDisplay.Listener listener)
   {
      this.listener = listener;
   }

   public void setEnabled(boolean enabled)
   {
      commentTextArea.setEnabled(enabled);
      addCommentButton.setEnabled(enabled);
   }

   public void clearInput()
   {
      commentTextArea.setValue("");
   }

   interface ReviewCommentInputWidgetUiBinder extends UiBinder<HTMLPanel, ReviewCommentInputWidget>
   {
   }
}