/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class FilterViewConfirmationPanel extends DecoratedPopupPanel implements FilterViewConfirmationDisplay
{
   private Button saveChanges = new Button("Save Changes");
   private Button saveFuzzy = new Button("Save as Fuzzy");
   private Button discardChanges = new Button("Discard Changes");
   private Button cancelFilter = new Button("Cancel filter");

   private boolean filterTranslated, filterNeedReview, filterUntranslated;
   private Listener listener;

   public FilterViewConfirmationPanel()
   {
      super(false, true);
      VerticalPanel vp = new VerticalPanel();
      vp.setSpacing(10);
      vp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

      Label message = new Label("Save changes before filtering view?");
      HorizontalPanel buttonPanel = new HorizontalPanel();
      buttonPanel.setSpacing(5);
      buttonPanel.setSize("100%", "100%");
      buttonPanel.add(saveChanges);
      buttonPanel.add(saveFuzzy);
      buttonPanel.add(discardChanges);
      buttonPanel.add(cancelFilter);
      setStyleName("confirmationDialogPanel");

      vp.add(message);
      vp.add(buttonPanel);
      add(vp);

      hide();
   }

   //TODO to be removed below methods
   public HasClickHandlers getSaveChangesAndFilterButton()
   {
      return saveChanges;
   }

   public HasClickHandlers getSaveFuzzyAndFilterButton()
   {
      return saveFuzzy;
   }

   public HasClickHandlers getDiscardChangesAndFilterButton()
   {
      return discardChanges;
   }

   public HasClickHandlers getCancelFilterButton()
   {
      return cancelFilter;
   }

   public void updateFilter(boolean filterTranslated, boolean filterNeedReview, boolean filterUntranslated)
   {
      this.filterTranslated = filterTranslated;
      this.filterNeedReview = filterNeedReview;
      this.filterUntranslated = filterUntranslated;
   }

   public boolean isFilterTranslated()
   {
      return filterTranslated;
   }

   public boolean isFilterNeedReview()
   {
      return filterNeedReview;
   }

   public boolean isFilterUntranslated()
   {
      return filterUntranslated;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
      addListenerToButtons();
   }

   private void addListenerToButtons()
   {
      saveChanges.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.saveChangesAndFilter();
         }
      });
      saveFuzzy.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.saveAsFuzzyAndFilter();
         }
      });
      discardChanges.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.discardChangesAndFilter();
         }
      });
      cancelFilter.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            listener.cancelFilter();
         }
      });
   }
}


 