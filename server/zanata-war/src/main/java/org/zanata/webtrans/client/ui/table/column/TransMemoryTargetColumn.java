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
package org.zanata.webtrans.client.ui.table.column;

import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;

import com.google.gwt.user.client.ui.VerticalPanel;

public class TransMemoryTargetColumn extends StaticWidgetColumn<TransMemoryResultItem, VerticalPanel>
{
   @Override
   public VerticalPanel getValue(TransMemoryResultItem object)
   {
      VerticalPanel panel = new VerticalPanel();
      panel.setSize("100%", "100%");
      // display multiple source/target strings
      for (String targetContent : object.getTargetContents())
      {
         HighlightingLabel label = new HighlightingLabel();
         label.setText(targetContent);
         panel.add(label);
      }
      return panel;
   }
}
