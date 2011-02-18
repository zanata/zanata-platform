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
package net.openl10n.flies.webtrans.client.action;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.google.gwt.event.shared.GwtEvent;

public class UndoManager
{
   private static final int DEFAULT_LIMIT = 20;
   private int limit = DEFAULT_LIMIT;
   private Vector<UndoableAction<?>> undoList = new Vector<UndoableAction<?>>(limit);
   private List<GwtEvent<?>> history = new ArrayList<GwtEvent<?>>();

   public void addEdit(UndoableAction<?> edit)
   {
      undoList.add(edit);
   }

   public boolean canUndo()
   {
      return !undoList.isEmpty();
   }

   public boolean canRedo()
   {
      return false;
   }

   public void undo()
   {
      UndoableAction<?> var = undoList.lastElement();
      var.undo();
      undoList.remove(var);
   }

   public void setLimit(int lim)
   {
      this.limit = lim;
   }

   public void addEvent(GwtEvent<?> event)
   {
      history.add(event);
   }


}
