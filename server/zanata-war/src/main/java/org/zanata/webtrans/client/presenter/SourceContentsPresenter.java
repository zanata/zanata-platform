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
package org.zanata.webtrans.client.presenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.editor.table.SourceContentsView;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.shared.model.TransUnit;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class SourceContentsPresenter
{
   private final Map<Integer, SourceContentsView> sourcePanelMap;
   private HasSelectableSource selectedSource;
   private HasSelectableSource previousSource;
   
   private final EventBus eventBus;

   @Inject
   public SourceContentsPresenter(final EventBus eventBus)
   {
      this.eventBus = eventBus;
      sourcePanelMap = new HashMap<Integer, SourceContentsView>();
   }
   
   private final ClickHandler selectSourceHandler = new ClickHandler()
   {
      @Override
      public void onClick(ClickEvent event)
      {
         previousSource = selectedSource;
         selectedSource = (HasSelectableSource) event.getSource();
         selectedSource.setSelected(true);

         if (previousSource != null)
         {
            previousSource.setSelected(false);
         }

         Log.debug("Selected source: " + selectedSource.getSource());
         eventBus.fireEvent(new RequestValidationEvent());
      }
   };

   /**
    * Select first source in the list when row is selected
    * 
    * @param row
    */
   public void setSelectedSource(int row)
   {
      SourceContentsView sourceContentsView = sourcePanelMap.get(row);
      if (sourceContentsView != null)
      {
         ClickEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), sourceContentsView.getSourcePanelList().get(0));
      }
   }

   public String getSelectedSource()
   {
      return selectedSource.getSource();
   }

   public SourceContentsView getSourceContent(int row, TransUnit value)
   {
      SourceContentsView sourceContentsView;

      if (sourcePanelMap.containsKey(row))
      {
         sourceContentsView = sourcePanelMap.get(row);
      }
      else
      {
         sourceContentsView = new SourceContentsView();
         sourcePanelMap.put(row, sourceContentsView);
      }

      sourceContentsView.setValue(value);

      List<HasClickHandlers> sourcePanelList = sourceContentsView.getSourcePanelList();

      for (HasClickHandlers sourcePanel : sourcePanelList)
      {
         sourcePanel.addClickHandler(selectSourceHandler);
      }
      return sourceContentsView;
   }
}
