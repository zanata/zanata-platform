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
package net.openl10n.flies.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.common.NavigationType;
import net.openl10n.flies.webtrans.client.events.NavTransUnitEvent;
import net.openl10n.flies.webtrans.client.events.NavTransUnitHandler;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;

public class TransUnitNavigationPresenter extends WidgetPresenter<TransUnitNavigationPresenter.Display> implements HasNavTransUnitHandlers
{

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getPrevEntryButton();

      HasClickHandlers getNextEntryButton();

      HasClickHandlers getPrevFuzzyOrUntranslatedButton();

      HasClickHandlers getNextFuzzyOrUntranslatedButton();
   }

   @Inject
   public TransUnitNavigationPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   @Override
   protected void onBind()
   {
      display.getPrevEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(null, -1));
         }
      });

      display.getNextEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(null, +1));
         }
      });

      display.getPrevFuzzyOrUntranslatedButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.FuzzyOrUntranslated, -1));
         }
      });

      display.getNextFuzzyOrUntranslatedButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.FuzzyOrUntranslated, +1));
         }
      });

   }

   @Override
   public HandlerRegistration addNavTransUnitHandler(NavTransUnitHandler handler)
   {
      // TODO Auto-generated method stub
      return eventBus.addHandler(NavTransUnitEvent.getType(), handler);
   }

   @Override
   public void fireEvent(GwtEvent<?> event)
   {
      eventBus.fireEvent(event);
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }

}
