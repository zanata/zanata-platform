/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a> *
 */
public class KeyShortcutEvent extends GwtEvent<KeyShortcutEventHandler>
{

   private final int modifiers;
   private final int keyCode;

   public int getModifiers()
   {
      return modifiers;
   }

   public int getKeyCode()
   {
      return keyCode;
   }

   public KeyShortcutEvent(int modifiers, int keyCode)
   {
      this.modifiers = modifiers;
      this.keyCode = keyCode;
   }

   @Override
   protected void dispatch(KeyShortcutEventHandler handler)
   {
      handler.onKeyShortcut(this);
   }


   /**
    * Handler type.
    */
   private static Type<KeyShortcutEventHandler> TYPE;

   /**
    * Gets the type associated with this event.
    * 
    * @return returns the handler type
    */
   public static Type<KeyShortcutEventHandler> getType()
   {
      return TYPE != null ? TYPE : (TYPE = new Type<KeyShortcutEventHandler>());
   }

   @Override
   public Type<KeyShortcutEventHandler> getAssociatedType()
   {
      return getType();
   }
}
