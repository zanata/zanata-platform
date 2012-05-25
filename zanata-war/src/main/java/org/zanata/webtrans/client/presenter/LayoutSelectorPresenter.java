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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class LayoutSelectorPresenter extends WidgetPresenter<LayoutSelectorPresenter.Display>
{
   @Inject
   public LayoutSelectorPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   public interface Display extends WidgetDisplay
   {
      void show();

      void hide();
      
      void center();

      HasClickHandlers getDefaultLayoutContainer();

      HasClickHandlers getMaximiseLayoutContainer();

      HasClickHandlers getNoOptionLayoutContainer();

      HasClickHandlers getNoSouthLayoutContainer();
   }

   private HasLayoutOrganiser layoutOrganiser;

   @Override
   protected void onBind()
   {
      display.hide();
      
      registerHandler(display.getDefaultLayoutContainer().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            layoutOrganiser.setSidePanelVisible(true);
            layoutOrganiser.setSouthPanelVisible(true);
            display.hide();
         }
      }));
      
      registerHandler(display.getMaximiseLayoutContainer().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            layoutOrganiser.setSidePanelVisible(false);
            layoutOrganiser.setSouthPanelVisible(false);
            display.hide();
         }
      }));
      
      registerHandler(display.getNoOptionLayoutContainer().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            layoutOrganiser.setSidePanelVisible(false);
            layoutOrganiser.setSouthPanelVisible(true);
            display.hide();
         }
      }));
      
      registerHandler(display.getNoSouthLayoutContainer().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            layoutOrganiser.setSidePanelVisible(true);
            layoutOrganiser.setSouthPanelVisible(false);
            display.hide();
         }
      }));
   }

   @Override
   protected void onUnbind()
   {
      // TODO Auto-generated method stub
   }

   @Override
   protected void onRevealDisplay()
   {
      // TODO Auto-generated method stub
   }

   public void show()
   {
      display.center();
   }

   public void hide()
   {
      display.hide();
   }

   public void setLayoutListener(HasLayoutOrganiser layoutOrganiser)
   {
      this.layoutOrganiser = layoutOrganiser;
   }
}
