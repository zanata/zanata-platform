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
package org.zanata.webtrans.client.editor.filter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.FindMessageEvent;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      HasValue<String> getFilterText();
   }

   @Inject
   public TransFilterPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
   }

   @Override
   protected void onBind()
   {

      display.getFilterText().addValueChangeHandler(new ValueChangeHandler<String>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<String> event)
         {
            eventBus.fireEvent(new FindMessageEvent(event.getValue()));
         }
      });

   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void onRevealDisplay()
   {
      // TODO Auto-generated method stub

   }

}
