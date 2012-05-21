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
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class PrefillPopupPanelView extends DialogBox implements PrefillPopupPanelDisplay
{

   private final PrefillForm prefillForm;
   private final Label processingLabel = new Label("processing...");

   @Inject
   public PrefillPopupPanelView(PrefillForm prefillForm, UiMessages messages)
   {
      //auto hide false, modal true
      super(false, true);
      getCaption().setText(messages.prefillCaption());
      setGlassEnabled(true);
      VerticalPanel main = new VerticalPanel();
      main.add(prefillForm);
      main.add(processingLabel);
      add(main);
      this.prefillForm = prefillForm;
      processingLabel.setVisible(false);
      hide();
   }

   @Override
   public void setListener(Listener listener)
   {
      prefillForm.setListener(listener);
   }

   @Override
   public void showProcessing()
   {
      prefillForm.setVisible(false);
      processingLabel.setVisible(true);
   }

   @Override
   public void showForm()
   {
      processingLabel.setVisible(false);
      prefillForm.setVisible(true);
      center();
   }
}
