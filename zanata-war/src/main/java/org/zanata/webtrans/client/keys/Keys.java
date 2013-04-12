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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a combination of modifier keys and a single key code for use with
 * {@link KeyShortcut}.
 * 
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 * 
 */
public class Keys implements Comparable<Keys>
{

   public static final int NO_MODIFIER = 0x0;
   public static final int NO_ALIAS = 0x0;
   public static final int ALT_KEY = 0x1;
   public static final int SHIFT_KEY = 0x2;
   public static final int CTRL_KEY = 0x4;
   public static final int META_KEY = 0x8;
   public static final int SHIFT_ALT_KEYS = ALT_KEY | SHIFT_KEY;
   public static final int CTRL_ALT_KEYS = CTRL_KEY | ALT_KEY;

   public static final int KEY_1 = 49;
   public static final int KEY_2 = 50;
   public static final int KEY_3 = 51;
   public static final int KEY_4 = 52;

   public static final int KEY_NUM_1 = 97;
   public static final int KEY_NUM_2 = 98;
   public static final int KEY_NUM_3 = 99;
   public static final int KEY_NUM_4 = 100;

   public static final int ALIAS_KEY = ALT_KEY | 'X';

   private int alias;
   private final int modifiers;
   private final int keyCode;

   public Keys(int modifiers, int keyCode)
   {
      this(NO_ALIAS, modifiers, keyCode);
   }
   
   public Keys(int alias, int modifiers, int keyCode)
   {
      this.modifiers = modifiers;
      this.keyCode = keyCode;
      this.alias = alias;
   }

   public int getModifiers()
   {
      return modifiers;
   }

   public int getKeyCode()
   {
      return keyCode;
   }

   @Override
   public int hashCode()
   {
      // could pre-calculate hash as these are both final.
      return keyCode * 8 + modifiers + alias;
   }

   /**
    * Two {@link KeyShortcut} objects are equal if they have the same key
    * combination and context.
    */
   @Override
   public boolean equals(Object obj)
   {
      if (obj == null)
         return false;
      if (!(obj instanceof Keys))
         return false;
      Keys other = (Keys) obj;
      boolean equal = keyCode == other.keyCode && modifiers == other.modifiers && alias == other.alias;
      return equal;
   }

   @Override
   public int compareTo(Keys o)
   {
      Integer compareFrom;
      Integer compareTo;

      if (this.alias == o.alias && this.modifiers == o.modifiers)
      {
         compareFrom = this.alias + this.modifiers + this.keyCode;
         compareTo = o.alias + o.modifiers + o.keyCode;
      }
      else
      {
         compareFrom = this.alias + this.modifiers;
         compareTo = o.alias + o.modifiers;
      }

      return compareFrom.compareTo(compareTo);
   }

   @Override
   public String toString()
   {
      return "alias: " + alias + " mod: " + modifiers + " key: " + keyCode + " hash: " + hashCode();
   }

   public static Set<Keys> setOf(Keys... keys)
   {
      return new HashSet<Keys>(Arrays.asList(keys));
   }

   public void setAlias(int alias)
   {
      this.alias = alias;
   }
}
