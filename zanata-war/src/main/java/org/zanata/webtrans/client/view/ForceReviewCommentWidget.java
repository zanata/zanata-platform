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

package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DialogBoxCloseButton;
import org.zanata.webtrans.client.ui.ReviewCommentInputWidget;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ForceReviewCommentWidget extends DialogBox implements ForceReviewCommentDisplay
{
   private final ReviewCommentInputWidget inputWidget;

   @Inject
   public ForceReviewCommentWidget(WebTransMessages messages)
   {
      super(false, true);
      setGlassEnabled(true);

      setText(messages.rejectCommentTitle());

      inputWidget = new ReviewCommentInputWidget();
      inputWidget.setButtonText(messages.confirmRejection());
      FlowPanel panel = new FlowPanel();
      panel.setStyleName("new-zanata");
      panel.setWidth("800px");
      panel.add(inputWidget);
      DialogBoxCloseButton button = new DialogBoxCloseButton(this);
      button.setText(messages.cancel());
      panel.add(button);
      setWidget(panel);
   }

   @Override
   public void setListener(Listener listener)
   {
      inputWidget.setListener(listener);
   }

   @Override
   public void clearInput()
   {
      inputWidget.clearInput();
   }
}
