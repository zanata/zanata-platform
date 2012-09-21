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
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.PageSizeChangeEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;

public class EditorOptionsPresenter extends WidgetPresenter<EditorOptionsDisplay> implements EditorOptionsDisplay.Listener, WorkspaceContextUpdateEventHandler, FilterViewEventHandler
{
   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final UserConfigHolder configHolder;
   private final UserWorkspaceContext userWorkspaceContext;

   private final ValueChangeHandler<Boolean> filterChangeHandler = new ValueChangeHandler<Boolean>()
   {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         eventBus.fireEvent(new FilterViewEvent(display.getTranslatedChk().getValue(), display.getNeedReviewChk().getValue(), display.getUntranslatedChk().getValue(), false));
      }
   };

   @Inject
   public EditorOptionsPresenter(EditorOptionsDisplay display, EventBus eventBus, UserWorkspaceContext userWorkspaceContext, ValidationOptionsPresenter validationDetailsPresenter, UserConfigHolder configHolder)
   {
      super(display, eventBus);
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.configHolder = configHolder;
      this.userWorkspaceContext = userWorkspaceContext;
      display.setListener(this);
   }

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
      registerHandler(eventBus.addHandler(FilterViewEvent.getType(), this));
      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this));

      //set options default values
      display.setOptionsState(configHolder.getState());
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      setReadOnly(userWorkspaceContext.hasReadOnlyAccess());
   }

   private void setReadOnly(boolean readOnly)
   {
      boolean displayButtons = !readOnly && configHolder.isDisplayButtons();
      configHolder.setDisplayButtons(displayButtons);
      display.setOptionsState(configHolder.getState());
      eventBus.fireEvent(UserConfigChangeEvent.EVENT);
   }

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

   @Override
   public void onSelectionChange(String groupName, NavOption navOption)
   {
      if (configHolder.getNavOption() != navOption)
      {
         configHolder.setNavOption(navOption);
         eventBus.fireEvent(UserConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onPageSizeClick(int pageSize)
   {
      if (configHolder.getPageSize() != pageSize)
      {
         configHolder.setPageSize(pageSize);
         eventBus.fireEvent(new PageSizeChangeEvent(pageSize));
      }
   }

   @Override
   public void onEnterSaveOptionChanged(Boolean enterSaveApproved)
   {
      if (configHolder.isEnterSavesApproved() != enterSaveApproved)
      {
         configHolder.setEnterSavesApproved(enterSaveApproved);
         eventBus.fireEvent(UserConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onEscCancelEditOptionChanged(Boolean escCancelEdit)
   {
      if (configHolder.isEscClosesEditor() != escCancelEdit)
      {
         configHolder.setEscClosesEditor(escCancelEdit);
         eventBus.fireEvent(UserConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onEditorButtonsOptionChanged(Boolean editorButtons)
   {
      if (configHolder.isDisplayButtons() != editorButtons)
      {
         configHolder.setDisplayButtons(editorButtons);
         eventBus.fireEvent(UserConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onShowErrorsOptionChanged(Boolean showErrorChkValue)
   {
      // this config value is only used in org.zanata.webtrans.client.Application.registerUncaughtExceptionHandler
      // therefore we don't need to broadcast the change event
      configHolder.setShowError(showErrorChkValue);
   }

   @Override
   public void onUseCodeMirrorOptionChanged(Boolean useCodeMirrorChkValue)
   {
      if (configHolder.isUseCodeMirrorEditor() != useCodeMirrorChkValue)
      {
         configHolder.setUseCodeMirrorEditor(useCodeMirrorChkValue);
         eventBus.fireEvent(RefreshPageEvent.EVENT);
      }
   }

   @Override
   public void refreshCurrentPage()
   {
      eventBus.fireEvent(RefreshPageEvent.EVENT);
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }
}
