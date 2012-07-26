/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.ui.SourcePanel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Widget;

public class SourceContentsView extends Composite implements SourceContentsDisplay
{

   public static final int COLUMNS = 1;
   public static final int DEFAULT_ROWS = 1;
   private final Grid sourcePanelContainer;
   private List<HasSelectableSource> sourcePanelList;

   public SourceContentsView()
   {
      sourcePanelList = new ArrayList<HasSelectableSource>();
      FlowPanel container = new FlowPanel();
      container.setSize("100%", "100%");

      initWidget(container);

      sourcePanelContainer = new Grid(DEFAULT_ROWS, COLUMNS);
      sourcePanelContainer.addStyleName("sourceTable");

      container.add(sourcePanelContainer);
   }

   @Override
   public List<HasSelectableSource> getSourcePanelList()
   {
      return sourcePanelList;
   }

   @Override
   public void setValue(TransUnit value)
   {
      setValue(value, true);
   }

   @Override
   public void setValue(TransUnit value, boolean fireEvents)
   {
      sourcePanelContainer.resizeRows(value.getSources().size());
      sourcePanelList.clear();

      int rowIndex = 0;
      for (String source : value.getSources())
      {
         SourcePanel sourcePanel = new SourcePanel();
         sourcePanel.setValue(source, value.getSourceComment(), value.isPlural());
         sourcePanelContainer.setWidget(rowIndex, 0, sourcePanel);
         sourcePanelList.add(sourcePanel);
         rowIndex++;
      }
   }

   @Override
   public void highlightSearch(String search)
   {
      for (Widget sourceLabel : sourcePanelContainer)
      {
         ((SourcePanel) sourceLabel).highlightSearch(search);
      }
   }

   @Override
   public void setSourceSelectionHandler(ClickHandler clickHandler)
   {
      Preconditions.checkState(!sourcePanelList.isEmpty(), "empty source panel list. Did you forget to call setValue() before this?");
      for (HasSelectableSource hasSelectableSource : sourcePanelList)
      {
         hasSelectableSource.addClickHandler(clickHandler);
      }
   }
}
