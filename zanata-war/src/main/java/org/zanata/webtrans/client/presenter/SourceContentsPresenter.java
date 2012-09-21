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

import java.util.Collections;
import java.util.List;

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.util.FindByTransUnitIdPredicate;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Inject;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public class SourceContentsPresenter implements ClickHandler
{
   private HasSelectableSource selectedSource;

   private final EventBus eventBus;
   private Provider<SourceContentsDisplay> displayProvider;
   private List<SourceContentsDisplay> displayList = Collections.emptyList();
   private TransUnitId currentTransUnitId;

   @Inject
   public SourceContentsPresenter(final EventBus eventBus, Provider<SourceContentsDisplay> displayProvider)
   {
      this.eventBus = eventBus;
      this.displayProvider = displayProvider;
   }

   /**
    * Select first source in the list when row is selected or reselect previous selected one
    *
    */
   public void setSelectedSource(TransUnitId id)
   {
      currentTransUnitId = id;
      Log.debug("source content selected id:" + id);

      SourceContentsDisplay sourceContentsView = Iterables.find(displayList, new FindByTransUnitIdPredicate(id));
      // after save as fuzzy re-render(will call
      // SourceContentsView.setValue(TransUnit) which cause re-creation of
      // SourcePanel list), we want to re-select the radio button
      List<HasSelectableSource> sourcePanelList = sourceContentsView.getSourcePanelList();
      for (HasSelectableSource sourcePanel : sourcePanelList)
      {
         if (selectedSource != null && selectedSource.getSource().equals(sourcePanel.getSource()))
         {
            fireClickEventToSelectSource(sourcePanel);
            return;
         }
      }
      //else by default it will select the first one
      fireClickEventToSelectSource(sourceContentsView.getSourcePanelList().get(0));
   }

   private static void fireClickEventToSelectSource(HasSelectableSource sourcePanel)
   {
      ClickEvent.fireNativeEvent(Document.get().createClickEvent(0, 0, 0, 0, 0, false, false, false, false), sourcePanel);
   }

   public String getSelectedSource()
   {
      return selectedSource == null ? null : selectedSource.getSource();
   }

   public void showData(List<TransUnit> transUnits)
   {
      ImmutableList.Builder<SourceContentsDisplay> builder = ImmutableList.builder();
      for (TransUnit transUnit : transUnits)
      {
         SourceContentsDisplay display = displayProvider.get();
         display.setValue(transUnit);
         display.setSourceSelectionHandler(this);
         builder.add(display);
      }
      displayList = builder.build();
   }

   public List<SourceContentsDisplay> getDisplays()
   {
      return displayList;
   }

   public void highlightSearch(String message)
   {
      for (SourceContentsDisplay sourceContentsDisplay : displayList)
      {
         sourceContentsDisplay.highlightSearch(message);
      }
   }

   @Override
   public void onClick(ClickEvent event)
   {
      if (event.getSource() instanceof HasSelectableSource)
      {
         HasSelectableSource previousSource = selectedSource;

         selectedSource = (HasSelectableSource) event.getSource();

         if (previousSource != null)
         {
            previousSource.setSelected(false);
         }

         selectedSource.setSelected(true);

         Log.debug("Selected source: " + selectedSource.getSource());
         //TODO this is firing every time we click.
         eventBus.fireEvent(new RequestValidationEvent());
      }
   }

   public TransUnitId getCurrentTransUnitIdOrNull()
   {
      return currentTransUnitId;
   }
}
