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

import org.zanata.webtrans.client.ui.SourcePanel;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class SourceContentsView extends Composite
{

   private final FlowPanel container;
   private final VerticalPanel sourcePanelContainer;
   private TransUnit value;
   private List<HasClickHandlers> sourcePanelList;

   public SourceContentsView()
   {
      sourcePanelList = new ArrayList<HasClickHandlers>();
      container = new FlowPanel();
      container.setSize("100%", "100%");

      initWidget(container);

      sourcePanelContainer = new VerticalPanel();
      sourcePanelContainer.addStyleName("sourceTable");

      container.add(sourcePanelContainer);
   }

   public List<HasClickHandlers> getSourcePanelList()
   {
      return sourcePanelList;
   }

   public void setValue(TransUnit value)
   {
      setValue(value, true);
   }

   public void setValue(TransUnit value, boolean fireEvents)
   {
      if (this.value != value)
      {
         this.value = value;

         sourcePanelContainer.clear();
         sourcePanelList.clear();

         for (String source : value.getSources())
         {
            SourcePanel sourcePanel = new SourcePanel();
            sourcePanel.setValue(source, value.getSourceComment(), (value.getSources().size() > 1));
            sourcePanelContainer.add(sourcePanel);
            sourcePanelList.add(sourcePanel);
         }
      }
   }

   public void highlightSearch(String search)
   {
      for (Widget sourceLabel : sourcePanelContainer)
      {
         ((SourcePanel) sourceLabel).highlightSearch(search);
      }
   }
}
