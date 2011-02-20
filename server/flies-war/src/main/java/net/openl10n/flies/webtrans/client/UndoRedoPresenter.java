/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package net.openl10n.flies.webtrans.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;
import com.allen_sauer.gwt.log.client.Log;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import net.openl10n.flies.webtrans.client.action.UndoManager;
import net.openl10n.flies.webtrans.client.action.UndoableTransUnitUpdateAction;
import net.openl10n.flies.webtrans.client.events.FindMessageEvent;
import net.openl10n.flies.webtrans.client.events.FindMessageHandler;
import net.openl10n.flies.webtrans.client.events.TransUnitUpdatedEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitUpdatedEventHandler;
import net.openl10n.flies.webtrans.client.events.UndoAddEvent;
import net.openl10n.flies.webtrans.client.events.UndoAddEventHandler;

public class UndoRedoPresenter extends WidgetPresenter<UndoRedoPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getUndoButton();

      HasClickHandlers getRedoButton();

      void disableUndo();

      void disableRedo();

      void enableUndo();

      void enableRedo();

   }

   private UndoManager undoManager = new UndoManager();

   @Inject
   public UndoRedoPresenter(Display display, EventBus eventBus)
   {
      super(display, eventBus);
   }

   @Override
   protected void onBind()
   {

      display.getUndoButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Log.info("undo");
            undoManager.undo();
            if (!undoManager.canUndo())
            {
               display.disableUndo();
            }
         }
      });

      display.getRedoButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            Log.info("redo");
         }
      });

      registerHandler(eventBus.addHandler(UndoAddEvent.getType(), new UndoAddEventHandler()
      {
         @Override
         public void onUndoableAction(UndoAddEvent event)
         {
            Log.info("add undoAddEvent for:" + ((UndoableTransUnitUpdateAction) event.getUndoableAction()).getRowNum());
            undoManager.addEdit(event.getUndoableAction());
            if (undoManager.canUndo())
            {
               display.enableUndo();
            }
         }
      }));

      registerHandler(eventBus.addHandler(TransUnitUpdatedEvent.getType(), new TransUnitUpdatedEventHandler()
      {
         @Override
         public void onTransUnitUpdated(TransUnitUpdatedEvent event)
         {
         }
      }));

      registerHandler(eventBus.addHandler(FindMessageEvent.getType(), new FindMessageHandler()
      {

         @Override
         public void onFindMessage(FindMessageEvent event)
         {
            display.disableUndo();
            undoManager.clear();
         }

      }));

   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

}
