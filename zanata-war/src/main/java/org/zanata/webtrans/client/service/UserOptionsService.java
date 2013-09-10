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

package org.zanata.webtrans.client.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;
import org.zanata.webtrans.shared.rpc.ThemesOption;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;


/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Singleton
public class UserOptionsService
{
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final UserConfigHolder configHolder;

   @Inject
   public UserOptionsService(EventBus eventBus, CachingDispatchAsync dispatcher, UserConfigHolder configHolder)
   {
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.configHolder = configHolder;
   }

   public void persistOptionChange(Map<UserOptions, String> optsMap)
   {
      SaveOptionsAction action = new SaveOptionsAction(optsMap);

      dispatcher.execute(action, new AsyncCallback<SaveOptionsResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Warning, "Could not save user options"));
         }

         @Override
         public void onSuccess(SaveOptionsResult result)
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, "Saved user options"));
         }
      });
   }

   public HashMap<UserOptions, String> getCommonOptions()
   {
      HashMap<UserOptions, String> configMap = new HashMap<UserOptions, String>();
      configMap.put(UserOptions.ShowErrors, Boolean.toString(configHolder.getState().isShowError()));
      configMap.put(UserOptions.Themes, configHolder.getState().getDisplayTheme().name());

      return configMap;
   }

   public HashMap<UserOptions, String> getDocumentListOptions()
   {
      HashMap<UserOptions, String> configMap;
      configMap = getCommonOptions();
      configMap.put(UserOptions.DocumentListPageSize, Integer.toString(configHolder.getState().getDocumentListPageSize()));

      return configMap;
   }

   public HashMap<UserOptions, String> getEditorOptions()
   {
      HashMap<UserOptions, String> configMap;
      configMap = getCommonOptions();
      configMap.put(UserOptions.DisplayButtons, Boolean.toString(configHolder.getState().isDisplayButtons()));
      configMap.put(UserOptions.EnterSavesApproved, Boolean.toString(configHolder.getState().isEnterSavesApproved()));
      configMap.put(UserOptions.EditorPageSize, Integer.toString(configHolder.getState().getEditorPageSize()));
      configMap.put(UserOptions.UseCodeMirrorEditor, Boolean.toString(configHolder.getState().isUseCodeMirrorEditor()));
      configMap.put(UserOptions.EnableSpellCheck, Boolean.toString(configHolder.getState().isSpellCheckEnabled()));
      configMap.put(UserOptions.TransMemoryDisplayMode, configHolder.getState().getTransMemoryDisplayMode().name());
      configMap.put(UserOptions.DisplayTransMemory, Boolean.toString(configHolder.getState().isShowTMPanel()));
      configMap.put(UserOptions.DisplayGlossary, Boolean.toString(configHolder.getState().isShowGlossaryPanel()));
      configMap.put(UserOptions.TranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByTranslated()));
      configMap.put(UserOptions.FuzzyMessageFilter, Boolean.toString(configHolder.getState().isFilterByFuzzy()));
      configMap.put(UserOptions.UntranslatedMessageFilter, Boolean.toString(configHolder.getState().isFilterByUntranslated()));
      configMap.put(UserOptions.ApprovedMessageFilter, Boolean.toString(configHolder.getState().isFilterByApproved()));
      configMap.put(UserOptions.RejectedMessageFilter, Boolean.toString(configHolder.getState().isFilterByRejected()));
      configMap.put(UserOptions.Navigation, configHolder.getState().getNavOption().toString());
      configMap.put(UserOptions.ShowSaveApprovedWarning, Boolean.toString(configHolder.getState().isShowSaveApprovedWarning()));
      configMap.put(UserOptions.EnableReferenceLang, Boolean.toString(configHolder.getState().isEnabledReferenceForSourceLang()));

      return configMap;
   }


   public void loadCommonOptions()
   {
      configHolder.setShowError(UserConfigHolder.DEFAULT_SHOW_ERROR);
      configHolder.setDisplayTheme(ThemesOption.THEMES_DEFAULT);
   }

   public void loadDocumentListDefaultOptions()
   {
      // default options
      loadCommonOptions();

      configHolder.setDocumentListPageSize(UserConfigHolder.DEFAULT_DOC_LIST_PAGE_SIZE);
   }

   public void loadEditorDefaultOptions()
   {
      // default options
      loadCommonOptions();

      // default options
      configHolder.setDisplayButtons(UserConfigHolder.DEFAULT_DISPLAY_BUTTONS);
      configHolder.setEnterSavesApproved(UserConfigHolder.DEFAULT_ENTER_SAVES_APPROVED);
      configHolder.setFilterByTranslated(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setFilterByFuzzy(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setFilterByUntranslated(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setFilterByApproved(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setFilterByRejected(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setFilterByHasError(UserConfigHolder.DEFAULT_FILTER);
      configHolder.setEnabledValidationIds(new ArrayList<ValidationId>());
      configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
      configHolder.setEditorPageSize(UserConfigHolder.DEFAULT_EDITOR_PAGE_SIZE);
      configHolder.setShowSaveApprovedWarning(UserConfigHolder.DEFAULT_SHOW_SAVE_APPROVED_WARNING);
      configHolder.setUseCodeMirrorEditor(UserConfigHolder.DEFAULT_USE_CODE_MIRROR);
      configHolder.setTMDisplayMode(UserConfigHolder.DEFAULT_TM_DISPLAY_MODE);
      configHolder.setShowTMPanel(UserConfigHolder.DEFAULT_SHOW_PANEL);
      configHolder.setShowGlossaryPanel(UserConfigHolder.DEFAULT_SHOW_PANEL);
      configHolder.setEnableReferenceForSourceLang(UserConfigHolder.DEFAULT_SHOW_PANEL);
   }

   public UserConfigHolder getConfigHolder()
   {
      return configHolder;
   }


}
