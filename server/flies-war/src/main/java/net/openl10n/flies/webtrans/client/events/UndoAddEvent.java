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
package net.openl10n.flies.webtrans.client.events;




import net.openl10n.flies.webtrans.client.action.UndoableAction;

import com.google.gwt.event.shared.GwtEvent;

public class UndoAddEvent extends GwtEvent<UndoAddEventHandler>
{
   private UndoableAction<?> undoableAction;
   /**
    * Handler type.
    */
   private static Type<UndoAddEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<UndoAddEventHandler> getType()
   {
      if (TYPE == null)
      {
         TYPE = new Type<UndoAddEventHandler>();
      }
      return TYPE;
   }

   @Override
   public com.google.gwt.event.shared.GwtEvent.Type<UndoAddEventHandler> getAssociatedType()
   {
      return getType();
   }


   @Override
   protected void dispatch(UndoAddEventHandler handler)
   {
      handler.onUndoableAction(this);
   }

   public UndoAddEvent(UndoableAction<?> undoableAction)
   {
      this.undoableAction = undoableAction;
   }

   public UndoableAction<?> getUndoableAction()
   {
      return this.undoableAction;
   }

}
