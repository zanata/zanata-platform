/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.TransHistoryItem;

/**
 * This class holds two distinct translation history items at most. They are then used for comparison.
 * The object created is immutable. See test for more detail.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
class ComparingPair
{
   private final TransHistoryItem one;
   private final TransHistoryItem two;

   private ComparingPair(TransHistoryItem one, TransHistoryItem two)
   {
      this.one = one;
      this.two = two;
   }

   public static ComparingPair empty()
   {
      return new ComparingPair(null, null);
   }

   TransHistoryItem one()
   {
      return one;
   }

   TransHistoryItem two()
   {
      return two;
   }

   /**
    * Add item to this holder.
    * If item already exist in this holder, it will be removed.
    * If current holder is full (already contains two items), new item won't be added in. i.e. no op.
    * Otherwise it will be added to the holder.
    *
    * @param newItem to be added/removed item
    * @return a new object of this class
    */
   public ComparingPair addOrRemove(TransHistoryItem newItem)
   {
      if (isEmpty())
      {
         return new ComparingPair(newItem, null);
      }

      if (newItem == one)
      {
         return new ComparingPair(two, null);
      }

      if (newItem == two)
      {
         return new ComparingPair(one, null);
      }

      if (isFull())
      {
         return this;
      }

      return new ComparingPair(one, newItem);
   }

   private boolean isEmpty()
   {
      return one == null && two == null;
   }

   public boolean isFull()
   {
      return one != null && two != null;
   }

   public boolean contains(TransHistoryItem item)
   {
      return one == item || two == item;
   }
}
