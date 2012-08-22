/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.EnableModalNavigationEvent;
import org.zanata.webtrans.client.events.EnableModalNavigationEventHandler;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class OptionsPanelPresenter extends WidgetPresenter<OptionsPanelPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {

      HasValue<Boolean> getTranslatedChk();

      HasValue<Boolean> getNeedReviewChk();

      HasValue<Boolean> getUntranslatedChk();

      HasValue<Boolean> getEditorButtonsChk();

      HasValue<Boolean> getEnterChk();

      HasValue<Boolean> getEscChk();

      void setNavOptionVisible(boolean visible);

      HasChangeHandlers getModalNavigationOptionsSelect();

      // possible filter values
      static final String KEY_FUZZY_UNTRANSLATED = "FU";
      static final String KEY_FUZZY = "F";
      static final String KEY_UNTRANSLATED = "U";

      String getSelectedFilter();

      void center();

      void hide();

      HasClickHandlers getCloseButton();
   }

   private final ValidationOptionsPresenter validationOptionsPresenter;

   private UserConfigHolder configHolder;
   private final UserWorkspaceContext userWorkspaceContext;

   @Inject
   public OptionsPanelPresenter(final Display display, final EventBus eventBus, UserWorkspaceContext userWorkspaceContext, final ValidationOptionsPresenter validationDetailsPresenter, UserConfigHolder configHolder)
   {
      super(display, eventBus);
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.configHolder = configHolder;
      this.userWorkspaceContext = userWorkspaceContext;
   }

   private final ValueChangeHandler<Boolean> filterChangeHandler = new ValueChangeHandler<Boolean>()
   {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         eventBus.fireEvent(new FilterViewEvent(display.getTranslatedChk().getValue(), display.getNeedReviewChk().getValue(), display.getUntranslatedChk().getValue(), false));
      }
   };

   @Override
   protected void onBind()
   {
      validationOptionsPresenter.bind();
      if(userWorkspaceContext.hasReadOnlyAccess())
      {
         setReadOnly(true);
      }

      registerHandler(display.getTranslatedChk().addValueChangeHandler(filterChangeHandler));
      registerHandler(display.getNeedReviewChk().addValueChangeHandler(filterChangeHandler));
      registerHandler(display.getUntranslatedChk().addValueChangeHandler(filterChangeHandler));

      registerHandler(eventBus.addHandler(FilterViewEvent.getType(), new FilterViewEventHandler()
      {
         @Override
         public void onFilterView(FilterViewEvent event)
         {
            // filter cancel will revert a checkbox value, so the checkboxes are
            // updated to reflect this reversion
            if (event.isCancelFilter())
            {
               display.getTranslatedChk().setValue(event.isFilterTranslated(), false);
               display.getNeedReviewChk().setValue(event.isFilterNeedReview(), false);
               display.getUntranslatedChk().setValue(event.isFilterUntranslated(), false);
            }
         }
      }));

      registerHandler(display.getEditorButtonsChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Show editor buttons: " + event.getValue());
            configHolder.setDisplayButtons(event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent());
         }
      }));

      registerHandler(display.getEnterChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Enter' Key to save and move to next string: " + event.getValue());
            configHolder.setEnterSavesApproved(event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent());
         }
      }));

      registerHandler(display.getEscChk().addValueChangeHandler(new ValueChangeHandler<Boolean>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Boolean> event)
         {
            Log.info("Enable 'Esc' Key to close editor: " + event.getValue());
            configHolder.setEscClosesEditor(event.getValue());
            eventBus.fireEvent(new UserConfigChangeEvent());
         }
      }));

      // editor buttons always shown by default
      display.getEditorButtonsChk().setValue(true, false);
      display.getEnterChk().setValue(configHolder.isEnterSavesApproved(), false);
      display.getEscChk().setValue(configHolder.isEscClosesEditor(), false);

      registerHandler(display.getModalNavigationOptionsSelect().addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent event)
         {
            String selectedOption = display.getSelectedFilter();
            if (selectedOption.equals(Display.KEY_FUZZY_UNTRANSLATED))
            {
               configHolder.setButtonUntranslated(true);
               configHolder.setButtonFuzzy(true);
            }
            else if (selectedOption.equals(Display.KEY_FUZZY))
            {
               configHolder.setButtonFuzzy(true);
               configHolder.setButtonUntranslated(false);
            }
            else if (selectedOption.equals(Display.KEY_UNTRANSLATED))
            {
               configHolder.setButtonFuzzy(false);
               configHolder.setButtonUntranslated(true);
            }
            eventBus.fireEvent(new UserConfigChangeEvent());
         }
      }));
      
      registerHandler(display.getCloseButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.hide();
         }
      }));

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            userWorkspaceContext.setProjectActive(event.isProjectActive());
            setReadOnly(userWorkspaceContext.hasReadOnlyAccess());
         }
      }));

      registerHandler(eventBus.addHandler(EnableModalNavigationEvent.getType(), new EnableModalNavigationEventHandler()
      {
         @Override
         public void onEnable(EnableModalNavigationEvent event)
         {
            display.setNavOptionVisible(event.isEnable());
         }
      }));
   }

   private void setReadOnly(boolean readOnly)
   {
      boolean displayButtons = readOnly ? false : display.getEditorButtonsChk().getValue();
      configHolder.setDisplayButtons(displayButtons);
      eventBus.fireEvent(new UserConfigChangeEvent());
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void setVisible(boolean visible)
   {
      if (visible)
      {
         display.center();
      }
      else
      {
         display.hide();
      }
   }

}
