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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.DisplaySouthPanelEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.ReloadUserConfigUIEvent;
import org.zanata.webtrans.client.events.ReloadUserConfigUIHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class EditorOptionsPresenter extends WidgetPresenter<EditorOptionsDisplay> implements EditorOptionsDisplay.Listener, OptionsDisplay.CommonOptionsListener, WorkspaceContextUpdateEventHandler, ReloadUserConfigUIHandler
{
   private final ValidationOptionsPresenter validationOptionsPresenter;
   private final UserWorkspaceContext userWorkspaceContext;
   private final CachingDispatchAsync dispatcher;
   private final UserOptionsService userOptionsService;

   @Inject
   public EditorOptionsPresenter(EditorOptionsDisplay display, EventBus eventBus, UserWorkspaceContext userWorkspaceContext,
                                 ValidationOptionsPresenter validationDetailsPresenter,
                                 CachingDispatchAsync dispatcher, UserOptionsService userOptionsService)
   {
      super(display, eventBus);
      this.validationOptionsPresenter = validationDetailsPresenter;
      this.userWorkspaceContext = userWorkspaceContext;
      this.dispatcher = dispatcher;
      this.userOptionsService = userOptionsService;
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

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this));
      registerHandler(eventBus.addHandler(ReloadUserConfigUIEvent.TYPE, this));

      //set options default values
      display.setOptionsState(userOptionsService.getConfigHolder().getState());
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getProjectIterationId().setProjectType(event.getProjectType());
      setReadOnly(userWorkspaceContext.hasReadOnlyAccess());
   }

   private void setReadOnly(boolean readOnly)
   {
      boolean displayButtons = !readOnly && userOptionsService.getConfigHolder().getState().isDisplayButtons();
      userOptionsService.getConfigHolder().setDisplayButtons(displayButtons);
      userOptionsService.getConfigHolder().setShowTMPanel(false);
      userOptionsService.getConfigHolder().setShowGlossaryPanel(false);
      display.setOptionsState(userOptionsService.getConfigHolder().getState());
      eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
   }

   @Override
   public void onSelectionChange(String groupName, NavOption navOption)
   {
      if (userOptionsService.getConfigHolder().getState().getNavOption() != navOption)
      {
         userOptionsService.getConfigHolder().setNavOption(navOption);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onPageSizeClick(int pageSize)
   {
      if (userOptionsService.getConfigHolder().getState().getEditorPageSize() != pageSize)
      {
         userOptionsService.getConfigHolder().setEditorPageSize(pageSize);
         eventBus.fireEvent(new EditorPageSizeChangeEvent(pageSize));
      }
   }

   @Override
   public void onEnterSaveOptionChanged(Boolean enterSaveApproved)
   {
      if (userOptionsService.getConfigHolder().getState().isEnterSavesApproved() != enterSaveApproved)
      {
         userOptionsService.getConfigHolder().setEnterSavesApproved(enterSaveApproved);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onEditorButtonsOptionChanged(Boolean editorButtons)
   {
      if (userOptionsService.getConfigHolder().getState().isDisplayButtons() != editorButtons)
      {
         userOptionsService.getConfigHolder().setDisplayButtons(editorButtons);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onShowSaveApprovedWarningChanged(Boolean showSaveApprovedWarning)
   {
      if (userOptionsService.getConfigHolder().getState().isShowSaveApprovedWarning() != showSaveApprovedWarning)
      {
         userOptionsService.getConfigHolder().setShowSaveApprovedWarning(showSaveApprovedWarning);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onSpellCheckOptionChanged(Boolean spellCheckChkValue)
   {
      if (userOptionsService.getConfigHolder().getState().isSpellCheckEnabled() != spellCheckChkValue)
      {
         userOptionsService.getConfigHolder().setSpellCheckEnabled(spellCheckChkValue);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onTransMemoryDisplayModeChanged(DiffMode displayMode)
   {
      if (userOptionsService.getConfigHolder().getState().getTransMemoryDisplayMode() != displayMode)
      {
         userOptionsService.getConfigHolder().setTMDisplayMode(displayMode);
         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   @Override
   public void onTMOrGlossaryDisplayOptionsChanged(Boolean showTMChkValue, Boolean showGlossaryChkValue)
   {
      UserConfigHolder.ConfigurationState state = userOptionsService.getConfigHolder().getState();
      if (state.isShowTMPanel() != showTMChkValue)
      {
         userOptionsService.getConfigHolder().setShowTMPanel(showTMChkValue);
      }
      if (state.isShowGlossaryPanel() != showGlossaryChkValue)
      {
         userOptionsService.getConfigHolder().setShowGlossaryPanel(showGlossaryChkValue);
      }
      boolean displaySouthPanel = showTMChkValue || showGlossaryChkValue;
      eventBus.fireEvent(new DisplaySouthPanelEvent(displaySouthPanel));
   }
   
   @Override
   public void onEnableReferenceForSourceLangOptionChanged(Boolean displayReference)
   {
      if (userOptionsService.getConfigHolder().getState().isEnabledReferenceForSourceLang() != displayReference)
      {
         userOptionsService.getConfigHolder().setEnableReferenceForSourceLang(displayReference);
      }
      eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
   }

   @Override
   public void onDisplayTransUnitDetailsOptionChanged(Boolean showTransUnitDetailsChkValue)
   {
      if (userOptionsService.getConfigHolder().getState().isShowOptionalTransUnitDetails() != showTransUnitDetailsChkValue)
      {
         userOptionsService.getConfigHolder().setShowOptionalTransUnitDetails(showTransUnitDetailsChkValue);
      }
      eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
   }

   @Override
   public void onUseCodeMirrorOptionChanged(Boolean useCodeMirrorChkValue)
   {
      if (userOptionsService.getConfigHolder().getState().isUseCodeMirrorEditor() != useCodeMirrorChkValue)
      {
         userOptionsService.getConfigHolder().setUseCodeMirrorEditor(useCodeMirrorChkValue);
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

   @Override
   public void persistOptionChange()
   {
      userOptionsService.persistOptionChange(userOptionsService.getEditorOptions());
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
            userOptionsService.getConfigHolder().setState(result.getConfiguration());
            refreshOptions();
         }
      });
   }

   @Override
   public void loadDefaultOptions()
   {
      userOptionsService.loadEditorDefaultOptions();
      refreshOptions();
   }

   @Override
   public void onReloadUserConfigUI(ReloadUserConfigUIEvent event)
   {
      if (event.getView() == MainView.Editor)
      {
         display.setOptionsState(userOptionsService.getConfigHolder().getState());
      }
   }

   private void refreshOptions()
   {
      display.setOptionsState(userOptionsService.getConfigHolder().getState());
      eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Loaded default editor options."));
   }
}
