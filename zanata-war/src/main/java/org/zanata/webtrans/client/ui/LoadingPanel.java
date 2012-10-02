/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Singleton
public class LoadingPanel extends DecoratedPopupPanel
{
   private final Label loadingLabel;
   @Inject
   public LoadingPanel(final  WebTransMessages messages, final Resources resources)
   {
      super(false, true);
      HorizontalPanel hp = new HorizontalPanel();
      hp.setSpacing(5);
      hp.setSize("100%", "100%");
      loadingLabel = new Label(messages.loading());
      loadingLabel.setStyleName("loadingLabel");
      hp.add(loadingLabel);
      hp.add(new Image(resources.loader()));
      setStyleName("loadingPanel");
      add(hp);
      
   }
   
   public void center(String text)
   {
      if (!isShowing())
      {
         hide();
      }
      loadingLabel.setText(text);
      center();
   }
}


 