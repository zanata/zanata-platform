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
package org.zanata.webtrans.client.presenter;

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TableConstants;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent.NavigationType;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.ui.ShortcutConfigPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitNavigationPresenter extends WidgetPresenter<TransUnitNavigationPresenter.Display> implements HasNavTransUnitHandlers
{

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getPrevEntryButton();

      HasClickHandlers getNextEntryButton();

      HasClickHandlers getFirstEntryButton();

      HasClickHandlers getLastEntryButton();

      HasClickHandlers getPrevStateButton();

      HasClickHandlers getNextStateButton();

      HasClickHandlers getConfigureButton();

      Widget getConfigureButtonObject();

      void setNavModeTooltip(Map<ContentState, Boolean> configMap);
   }

   @Inject
   public TransUnitNavigationPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   final ShortcutConfigPanel shortcutConfigPanel = new ShortcutConfigPanel(true, eventBus);

   @Override
   protected void onBind()
   {
      display.getPrevEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.PrevEntry));
         }
      });

      display.getNextEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.NextEntry));
         }
      });

      display.getFirstEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.FirstEntry));
         }
      });

      display.getLastEntryButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.LastEntry));
         }
      });

      display.getPrevStateButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.PrevState));
         }
      });

      display.getNextStateButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireEvent(new NavTransUnitEvent(NavigationType.NextState));
         }
      });

      display.getConfigureButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            shortcutConfigPanel.toggleDisplay(display.getConfigureButtonObject());
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
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

}
