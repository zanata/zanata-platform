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
package org.zanata.webtrans.client.editor;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.KeyCodes;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 *         See org.zanata.webtrans.client.editor.table.CheckKey
 * 
 **/
public final class CheckKeyImpl implements CheckKey
{
   private Context context;
   private int keyCode;
   private boolean shiftKey, altKey, ctrlKey;

   public CheckKeyImpl(Context context)
   {
      this.context = context;
   }

   @Override
   public void init(NativeEvent event)
   {
      keyCode = event.getKeyCode();
      shiftKey = event.getShiftKey();
      altKey = event.getAltKey();
      ctrlKey = event.getCtrlKey();
   }

   @Override
   public boolean isSaveAsFuzzyKey()
   {
      return ctrlKey && keyCode == KEY_S;
   }

   @Override
   public boolean isPreviousStateEntryKey()
   {
      return altKey && keyCode == KeyCodes.KEY_PAGEUP;
   }

   @Override
   public boolean isNextStateEntryKey()
   {
      return altKey && keyCode == KeyCodes.KEY_PAGEDOWN;
   }

   @Override
   public boolean isPreviousEntryKey()
   {
      if (context == Context.Edit)
      {
         return altKey && (keyCode == KeyCodes.KEY_UP || keyCode == KEY_J);
      }
      else
      {
         return (altKey && keyCode == KeyCodes.KEY_UP) || keyCode == KEY_J;
      }
   }

   @Override
   public boolean isNextEntryKey()
   {
      if (context == Context.Edit)
      {
         return altKey && (keyCode == KeyCodes.KEY_DOWN || keyCode == KEY_K);
      }
      else
      {
         return (altKey && keyCode == KeyCodes.KEY_DOWN) || keyCode == KEY_K;
      }
   }

   @Override
   public boolean isCopyFromSourceKey()
   {
      return altKey && keyCode == KEY_G;
   }

   @Override
   public boolean isUserTyping()
   {
      return !altKey && !ctrlKey && keyCode != KeyCodes.KEY_ESCAPE;
   }

   @Override
   public boolean isCloseEditorKey(boolean isEscKeyCloseEditor)
   {
      return isEscKeyCloseEditor && keyCode == KeyCodes.KEY_ESCAPE;
   }

   @Override
   public boolean isEnterKey()
   {
      return keyCode == KeyCodes.KEY_ENTER;
   }

   @Override
   public boolean isSaveAsApprovedKey(boolean isEnterKeySavesEnabled)
   {
      if (isEnterKey())
      {
         if (ctrlKey)
         {
            return true;
         }
         else
         {
            return isEnterKeySavesEnabled && !shiftKey;
         }
      }
      return false;
   }
}
