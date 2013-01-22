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
package org.zanata.webtrans.client.ui.table.cell;

import org.zanata.webtrans.client.ui.TransUnitCountGraph;

import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 **/
public class TransUnitCountGraphCell extends StaticWidgetCell<TransUnitCountGraph>
{
   public TransUnitCountGraphCell()
   {
      super(BrowserEvents.MOUSEOVER, BrowserEvents.MOUSEOUT);
   }

   @Override
   public void onBrowserEvent(Context context, Element parent, TransUnitCountGraph value, NativeEvent event, ValueUpdater<TransUnitCountGraph> valueUpdater)
   {
      if (event.getType().equalsIgnoreCase(BrowserEvents.MOUSEOVER))
      {
         value.onMouseOver(parent.getFirstChildElement());
      }
      else if (event.getType().equalsIgnoreCase(BrowserEvents.MOUSEOUT))
      {
         value.onMouseOut();
      }
   }

}
