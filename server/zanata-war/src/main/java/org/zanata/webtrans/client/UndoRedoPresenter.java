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
package org.zanata.webtrans.client;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.action.UndoManager;
import org.zanata.webtrans.client.action.UndoableAction;
import org.zanata.webtrans.client.events.EditTransUnitEvent;
import org.zanata.webtrans.client.events.EditTransUnitEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.RedoFailureEvent;
import org.zanata.webtrans.client.events.RedoFailureEventHandler;
import org.zanata.webtrans.client.events.UndoAddEvent;
import org.zanata.webtrans.client.events.UndoAddEventHandler;
import org.zanata.webtrans.client.events.UndoFailureEvent;
import org.zanata.webtrans.client.events.UndoFailureEventHandler;
import org.zanata.webtrans.client.events.UndoFinishEventHandler;
import org.zanata.webtrans.client.events.UndoRedoFinishEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

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
            undoManager.undo();
            display.disableUndo();
            display.disableRedo();
         }
      });

      display.getRedoButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            undoManager.redo();
            display.disableUndo();
            display.disableRedo();
         }
      });

      registerHandler(eventBus.addHandler(UndoAddEvent.getType(), new UndoAddEventHandler()
      {
         @Override
         public void onUndoableAction(UndoAddEvent event)
         {
            undoManager.addUndo(event.getUndoableAction());
            undoManager.clearRedo();
            checkUndoRedo();
         }
      }));

      registerHandler(eventBus.addHandler(UndoFailureEvent.getType(), new UndoFailureEventHandler()
      {
         @Override
         public void onFailure(UndoFailureEvent event)
         {
            undoManager.clearCurrent();
            undoManager.clearUndo();
            checkUndoRedo();
         }
      }));

      registerHandler(eventBus.addHandler(RedoFailureEvent.getType(), new RedoFailureEventHandler()
      {
         @Override
         public void onFailure(RedoFailureEvent event)
         {
            undoManager.clearCurrent();
            undoManager.clearRedo();
            checkUndoRedo();
         }
      }));

      registerHandler(eventBus.addHandler(UndoRedoFinishEvent.getType(), new UndoFinishEventHandler()
      {
         @Override
         public void onFinish(UndoRedoFinishEvent event)
         {
            UndoableAction<?, ?> action = event.getAction();
            if (action.equals(undoManager.getCurrent()))
            {
               undoManager.clearCurrent();
            }
            if (action.isUndo())
            {
               undoManager.addRedo(action);
            }
            if (action.isRedo())
            {
               undoManager.addUndo(action);
            }
            checkUndoRedo();
         }
      }));

      registerHandler(eventBus.addHandler(FindMessageEvent.getType(), new FindMessageHandler()
      {

         @Override
         public void onFindMessage(FindMessageEvent event)
         {
            undoManager.clearUndo();
            undoManager.clearUndo();
            checkUndoRedo();
         }

      }));

      registerHandler(eventBus.addHandler(EditTransUnitEvent.getType(), new EditTransUnitEventHandler()
      {

         @Override
         public void onEdit(EditTransUnitEvent event)
         {
            undoManager.clearRedo();
            checkUndoRedo();
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

   public void checkUndoRedo()
   {
      if (undoManager.canUndo())
      {
         display.enableUndo();
      }
      else
      {
         display.disableUndo();
      }
      if (undoManager.canRedo())
      {
         display.enableRedo();
      }
      else
      {
         display.disableRedo();
      }
   }

}
