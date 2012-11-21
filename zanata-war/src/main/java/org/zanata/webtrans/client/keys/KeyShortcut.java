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

import java.util.Set;

import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;

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

   private final Set<Keys> keys;


   private final ShortcutContext context;
   private String description;
   private final KeyShortcutEventHandler handler;
   private final KeyEvent keyEvent;

   private final boolean stopPropagation;
   private final boolean preventDefault;

   /**
    * Construct a KeyShortcut.
    *
    * @param builder key shortcut builder
    */
   public KeyShortcut(Builder builder)
   {
      this.keys = builder.keys;
      this.context = builder.context;
      this.description = builder.description;
      this.handler = builder.handler;
      this.keyEvent = builder.keyEvent;
      this.stopPropagation = builder.stopPropagation;
      this.preventDefault = builder.preventDefault;
   }

   // TODO migrate all code to use Builder
   public KeyShortcut(Keys shortcutKeys, ShortcutContext context, String description, KeyShortcutEventHandler handler)
   {
      this.keys = Keys.setOf(shortcutKeys);
      this.context = context;
      this.description = description;
      this.handler = handler;
      this.keyEvent = KeyEvent.KEY_DOWN;
      this.stopPropagation = false;
      this.preventDefault = false;
   }

   public Set<Keys> getAllKeys()
   {
      return keys;
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

   @Override
   public int hashCode()
   {
      int hash = context.ordinal();
      for (Keys singleKey : keys)
      {
         hash *= 2048;
         hash += singleKey.hashCode();
      }
      return hash;
   }

   /**
    * Two {@link KeyShortcut} objects are equal if they have the same key combination and context.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof KeyShortcut))
      {
         return false;
      }
      KeyShortcut other = (KeyShortcut) obj;
      return keys.equals(other.keys) && context == other.context;
   }

   /**
    * Used for sorting shortcuts in summary in UI
    */
   @Override
   public int compareTo(KeyShortcut o)
   {
      if (context.ordinal() != o.context.ordinal())
      {
         return context.ordinal() - o.context.ordinal();
      }
      return keys.iterator().next().compareTo(o.keys.iterator().next());
   }

   public void setDescription(String description)
   {
      this.description = description;
   }

   public static class Builder
   {
      private Set<Keys> keys = Sets.newHashSet();
      private ShortcutContext context;
      private String description;
      private KeyShortcutEventHandler handler;
      private KeyEvent keyEvent = KeyEvent.KEY_DOWN;
      private boolean stopPropagation = false;
      private boolean preventDefault = false;

      private Builder(KeyShortcut shortcut)
      {
         this.keys = shortcut.getAllKeys();
         this.context = shortcut.getContext();
         this.description = shortcut.getDescription();
         this.handler = shortcut.getHandler();
         this.keyEvent = shortcut.getKeyEvent();
         this.stopPropagation = shortcut.isStopPropagation();
         this.preventDefault = shortcut.isPreventDefault();
      }

      private Builder()
      {
      }

      public static Builder builder()
      {
         return new Builder();
      }

      public KeyShortcut build()
      {
         Preconditions.checkNotNull(keys);
         Preconditions.checkNotNull(context);
         Preconditions.checkNotNull(handler);
         Preconditions.checkNotNull(keyEvent);
         return new KeyShortcut(this);
      }

      public Builder addKey(Keys key)
      {
         keys.add(key);
         return this;
      }

      /**
       * @param context see {@link KeyShortcutPresenter#setContextActive(ShortcutContext, boolean)}
       * @return builder itself
       */
      public Builder setContext(ShortcutContext context)
      {
         this.context = context;
         return this;
      }

      /**
       * @param description shown to the user in the key shortcut summary pane.
       *        Use {@link #DO_NOT_DISPLAY_DESCRIPTION} to prevent shortcut being
       * @return builder itself
       */
      public Builder setDescription(String description)
      {
         this.description = description;
         return this;
      }

      /**
       * @param handler activated for a registered {@link KeyShortcut} when context is active and a user inputs the correct key combination
       * @return builder itself
       */
      public Builder setHandler(KeyShortcutEventHandler handler)
      {
         this.handler = handler;
         return this;
      }

      /**
       * @param keyEvent determines which type of key event will trigger this shortcut.
       * @return builder itself
       */
      public Builder setKeyEvent(KeyEvent keyEvent)
      {
         this.keyEvent = keyEvent;
         return this;
      }

      /**
       * @param stopPropagation {@see NativeEvent#stopPropagation()}
       * @return builder itself
       */
      public Builder setStopPropagation(boolean stopPropagation)
      {
         this.stopPropagation = stopPropagation;
         return this;
      }

      /**
       * @param preventDefault {@see NativeEvent#preventDefault()}
       * @return builder itself
       */
      public Builder setPreventDefault(boolean preventDefault)
      {
         this.preventDefault = preventDefault;
         return this;
      }
   }

}