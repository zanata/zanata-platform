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
public class KeyShortcut implements Comparable<KeyShortcut>
{
   public enum KeyEvent {
      KEY_UP ("keyup"),
      KEY_DOWN ("keydown"),
      KEY_PRESS ("keypress");

      public final String nativeEventType;

      KeyEvent(String nativeType)
      {
         this.nativeEventType = nativeType;
      }
   }

   public static final String DO_NOT_DISPLAY_DESCRIPTION = "";

   // TODO replace with List<Keys>
   private final Keys keys;


   private final ShortcutContext context;
   private String description;
   private final KeyShortcutEventHandler handler;
   private final KeyEvent keyEvent;

   private final boolean stopPropagation;
   private final boolean preventDefault;


   /**
    * Construct a KeyShortcut.
    * 
    * @param modifiers keys such as Shift and Alt that must be depressed for the
    *           shortcut to fire.
    *           <p>
    *           Use {@link Keys#ALT_KEY}, {@link Keys#SHIFT_KEY},
    *           {@link Keys#SHIFT_ALT_KEYS}, {@link Keys#META_KEY} and {@link Keys#CTRL_KEY}
    *           to generate this. ( e.g. {@code CTRL_KEY | ALT_KEY} )
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
    *           uppercase key code.
    *           </p>
    * @param context see
    *           {@link KeyShortcutPresenter#setContextActive(ShortcutContext, boolean)}
    * @param description shown to the user in the key shortcut summary pane.
    *        Use {@link #DO_NOT_DISPLAY_DESCRIPTION} to prevent shortcut being
    *        displayed in the summary pane.
    * @param keyEvent determines which type of key event will trigger this shortcut.
    * @param stopPropagation {@see NativeEvent#stopPropagation()}
    * @param preventDefault {@see NativeEvent#preventDefault()}
    * @param handler activated for a registered {@link KeyShortcut} when context is active
    *        and a user inputs the correct key combination
    */
   public KeyShortcut(int modifiers, int keyCode, ShortcutContext context, String description,
         KeyEvent keyEvent, boolean stopPropagation, boolean preventDefault, KeyShortcutEventHandler handler)
   {
      this.keys = new Keys(modifiers, keyCode);
      this.context = context;
      this.description = description;
      this.handler = handler;
      this.keyEvent = keyEvent;
      this.stopPropagation = stopPropagation;
      this.preventDefault = preventDefault;
   }

   /**
    * Create a key-down key shortcut that does not stop propagation or prevent default actions.
    * 
    * @see #KeyShortcut(int, int, ShortcutContext, String, KeyShortcutEventHandler, String, boolean, boolean, boolean)
    */
   public KeyShortcut(int modifiers, int keyCode, ShortcutContext context, String description, KeyShortcutEventHandler handler)
   {
      this(modifiers, keyCode, context, description, KeyEvent.KEY_DOWN, false, false, handler);
   }

   public Keys getKeys()
   {
      return keys;
   }

   // TODO remove (use Keys.getModifiers)
   public int getModifiers()
   {
      return keys.getModifiers();
   }

   // TODO remove (use Keys.getKeyCode)
   public int getKeyCode()
   {
      return keys.getKeyCode();
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

   public KeyEvent getKeyEvent()
   {
      return keyEvent;
   }

   public boolean isDisplayInView()
   {
      return !DO_NOT_DISPLAY_DESCRIPTION.equals(description);
   }

   public boolean isStopPropagation()
   {
      return stopPropagation;
   }

   public boolean isPreventDefault()
   {
      return preventDefault;
   }

   //TODO remove this and just inline body for any usages
   /**
    * Return a hash for just the user input part of the shortcut, without
    * context.
    * 
    * @return a hash that is unique for a set of modifiers + key code
    */
   public int keysHash()
   {
      return keys.hashCode();
   }

   // TODO update to deal with list of key combinations
   @Override
   public int hashCode()
   {
      return context.ordinal() * 2048 + keys.hashCode();
   }

   /**
    * Two {@link KeyShortcut} objects are equal if they have the same key combination and context.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof KeyShortcut))
         return false;
      KeyShortcut other = (KeyShortcut) obj;
      return keys.equals(other.keys) && context == other.context;
   }

   // TODO update to deal with list of key combinations
   @Override
   public int compareTo(KeyShortcut o)
   {
      return keys.compareTo(o.keys);
   }
}