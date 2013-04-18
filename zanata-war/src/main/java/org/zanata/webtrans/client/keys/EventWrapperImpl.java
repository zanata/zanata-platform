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
package org.zanata.webtrans.client.keys;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

public class EventWrapperImpl implements EventWrapper
{

   @Override
   public HandlerRegistration addNativePreviewHandler(NativePreviewHandler handler)
   {
      return com.google.gwt.user.client.Event.addNativePreviewHandler(handler);
   }

   public int keyDownEvent()
   {
      return com.google.gwt.user.client.Event.ONKEYDOWN;
   }

   @Override
   public int keyUpEvent()
   {
      return com.google.gwt.user.client.Event.ONKEYUP;
   }

   @Override
   public int getTypeInt(NativePreviewEvent evt)
   {
      return evt.getTypeInt();
   }

   @Override
   public Keys createKeys(NativeEvent evt)
   {

      int modifiers = (evt.getAltKey() ? Keys.ALT_KEY : 0) | (evt.getShiftKey() ? Keys.SHIFT_KEY : 0)
                    | (evt.getCtrlKey() ? Keys.CTRL_KEY : 0) | (evt.getMetaKey() ? Keys.META_KEY : 0);

      return new Keys(modifiers, evt.getKeyCode());
   }

   @Override
   public String getType(NativeEvent evt)
   {
      return evt.getType();
   }

}
