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

import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;

/**
 * Represents a key shortcut for registration with {@link KeyShortcutPresenter}.
 * 
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 * 
 */
public class KeyShortcut
{
   public static final int ALT_KEY = 0x1;
   public static final int SHIFT_KEY = 0x2;
   public static final int CTRL_KEY = 0x4;
   public static final int META_KEY = 0x8;
   public static final int SHIFT_ALT_KEYS = ALT_KEY | SHIFT_KEY;

   private final int modifiers;
   private final int keyCode;
   private final ShortcutContext context;
   private String description;
   private final KeyShortcutEventHandler handler;

   /**
    * Construct a KeyShortcut.
    * 
    * @param modifiers keys such as Shift and Alt that must be depressed for the
    *           shortcut to fire.
    *           <p>
    *           Use {@link #ALT_KEY}, {@link #SHIFT_KEY},
    *           {@link #SHIFT_ALT_KEYS}, {@link #META_KEY} and {@link #CTRL_KEY}
    *           to generate this. ( e.g. {@code} CTRL_KEY | ALT_KEY )
    *           </p>
    * @param keyCode the integer code for the key.
    *           <p>
    *           This may be an uppercase character, but results may vary so test
    *           thoroughly in the targeted browsers.
    *           </p>
    *           <p>
    *           Note that for keypress events, the key code depends on Shift and
    *           CapsLock and will give the lowercase or uppercase ASCII code as
    *           expected. keydown and keyup events appear always to give the
    *           uppercase key code (keydown is currently used for all shortcuts.
    *           </p>
    * @param context see
    *           {@link KeyShortcutPresenter#setContextActive(ShortcutContext, boolean)}
    * @param description shown to the user in the key shortcut summary pane
    */
   public KeyShortcut(int modifiers, int keyCode, ShortcutContext context, String description, KeyShortcutEventHandler handler)
   {
      this.modifiers = modifiers;
      this.keyCode = keyCode;
      this.context = context;
      this.description = description;
      this.handler = handler;
   }

   public int getModifiers()
   {
      return modifiers;
   }

   public int getKeyCode()
   {
      return keyCode;
   }

   public ShortcutContext getContext()
   {
      return context;
   }

   public String getDescription()
   {
      return description;
   }

   public KeyShortcutEventHandler getHandler()
   {
      return handler;
   }

   /**
    * Return a hash for just the user input part of the shortcut, without
    * context.
    * 
    * @return a hash that is unique for a set of modifiers + key code
    */
   public int keysHash()
   {
      return keyCode * 8 + modifiers;
   }

   @Override
   public int hashCode()
   {
      int hash = context.ordinal();
      hash = hash * 256 + keyCode;
      hash = hash * 8 + modifiers;
      return hash;
   }

   /**
    * Two {@link KeyShortcut} objects are equal if they have the same modifier
    * keys, key code and context.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof KeyShortcut))
         return false;
      KeyShortcut other = (KeyShortcut) obj;
      return modifiers == other.modifiers && keyCode == other.keyCode && context == other.context;
   }
}