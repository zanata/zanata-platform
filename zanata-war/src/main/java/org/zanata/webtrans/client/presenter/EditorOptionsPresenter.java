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

import java.util.ArrayList;
import java.util.HashMap;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.EditorConfigChangeEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EditorOptionsPresenter extends WidgetPresenter<EditorOptionsDisplay> implements EditorOptionsDisplay.Listener, OptionsDisplay.Listener, WorkspaceContextUpdateEventHandler, FilterViewEventHandler
{
   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final UserConfigHolder configHolder;
   private final UserWorkspaceContext userWorkspaceContext;
   private final CachingDispatchAsync dispatcher;

   private final ValueChangeHandler<Boolean> filterChangeHandler = new ValueChangeHandler<Boolean>()
   {
      @Override
      public void onValueChange(ValueChangeEvent<Boolean> event)
      {
         eventBus.fireEvent(new FilterViewEvent(display.getTranslatedChk().getValue(), display.getNeedReviewChk().getValue(), display.getUntranslatedChk().getValue(), false));
         configHolder.setFilterByUntranslated( display.getUntranslatedChk().getValue() );
         configHolder.setFilterByNeedReview( display.getNeedReviewChk().getValue() );
         configHolder.setFilterByTranslated( display.getTranslatedChk().getValue() );
      }
   };

   @Inject
   public EditorOptionsPresenter(EditorOptionsDisplay display, EventBus eventBus, UserWorkspaceContext userWorkspaceContext,
                                 ValidationOptionsPresenter validationDetailsPresenter, UserConfigHolder configHolder,
                                 CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.configHolder = configHolder;
      this.userWorkspaceContext = userWorkspaceContext;
      this.dispatcher = dispatcher;
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
      eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
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
         eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onPageSizeClick(int pageSize)
   {
      if (configHolder.getEditorPageSize() != pageSize)
      {
         configHolder.setEditorPageSize(pageSize);
         eventBus.fireEvent(new EditorPageSizeChangeEvent(pageSize));
      }
   }

   @Override
   public void onEnterSaveOptionChanged(Boolean enterSaveApproved)
   {
      if (configHolder.isEnterSavesApproved() != enterSaveApproved)
      {
         configHolder.setEnterSavesApproved(enterSaveApproved);
         eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
      }
   }

   @Override
   public void onEditorButtonsOptionChanged(Boolean editorButtons)
   {
      if (configHolder.isDisplayButtons() != editorButtons)
      {
         configHolder.setDisplayButtons(editorButtons);
         eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
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
         eventBus.fireEvent(RefreshPageEvent.REDRAW_PAGE_EVENT);
      }
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   private HashMap<UserOptions, String> generateConfigMap()
   {
      HashMap<UserOptions, String> configMap = new HashMap<UserOptions, String>();
      configMap.put(UserOptions.DisplayButtons, Boolean.toString(configHolder.getState().isDisplayButtons()));
      configMap.put(UserOptions.EnterSavesApproved, Boolean.toString(configHolder.getState().isEnterSavesApproved()));
      configMap.put(UserOptions.EditorPageSize, Integer.toString(configHolder.getState().getEditorPageSize()));

      configMap.put(UserOptions.TranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByTranslated()));
      configMap.put(UserOptions.NeedReviewMessageFilter, Boolean.toString(configHolder.getState().isFilterByNeedReview()));
      configMap.put(UserOptions.UntranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByUntranslated()));
      configMap.put(UserOptions.Navigation, configHolder.getState().getNavOption().toString());

      configMap.put(UserOptions.ShowErrors, Boolean.toString(configHolder.getState().isShowError()));
      return configMap;
   }

   @Override
   public void persistOptionChange()
   {
      SaveOptionsAction action = new SaveOptionsAction(generateConfigMap());

      dispatcher.execute(action, new AsyncCallback<SaveOptionsResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Could not Save editor Options"));
         }

         @Override
         public void onSuccess(SaveOptionsResult result)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, "Saved editor Options"));
         }
      });
   }

   @Override
   public void loadOptions()
   {
      ArrayList<String> prefixes = new ArrayList<String>();
      prefixes.add(UserOptions.editor());
      prefixes.add(UserOptions.common());

      dispatcher.execute(new LoadOptionsAction(prefixes), new AsyncCallback<LoadOptionsResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Unable to Load editor Options"));
         }

         @Override
         public void onSuccess(LoadOptionsResult result)
         {
            configHolder.setState( result.getConfiguration() );

            display.setOptionsState(configHolder.getState());
            eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
            filterChangeHandler.onValueChange(null); //NB: Null event is valid because it's not being used
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Loaded editor options"));
         }
      });
   }

   @Override
   public void loadDefaultOptions()
   {
      // default options
      configHolder.setDisplayButtons(true);
      configHolder.setEnterSavesApproved(false);
      configHolder.setFilterByTranslated(false);
      configHolder.setFilterByNeedReview(false);
      configHolder.setFilterByUntranslated(false);
      configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
      configHolder.setEditorPageSize(25);
      configHolder.setShowError(false);

      eventBus.fireEvent(EditorConfigChangeEvent.EVENT);
      filterChangeHandler.onValueChange(null); //NB: Null event is valid because it's not being used
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Loaded default editor options."));
      display.setOptionsState(configHolder.getState());
   }
}
