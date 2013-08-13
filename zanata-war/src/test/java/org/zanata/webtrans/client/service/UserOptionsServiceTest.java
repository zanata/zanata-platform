/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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
package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Test(groups = "unit-tests")
public class UserOptionsServiceTest
{
   private UserOptionsService service;

   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;

   private UserConfigHolder configHolder = new UserConfigHolder();

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      service = new UserOptionsService(eventBus, dispatcher, configHolder);
   }

   @Test
   public void persistOptionChange()
   {
      Map<UserOptions, String> map = new HashMap<UserOptions, String>();
      map.put(UserOptions.DocumentListPageSize, String.valueOf(1000));
      map.put(UserOptions.EditorPageSize, String.valueOf(2000));
      
      map.put(UserOptions.DisplayButtons, String.valueOf(false));
      map.put(UserOptions.ShowErrors, String.valueOf(true));

      service.persistOptionChange(map);

      ArgumentCaptor<SaveOptionsAction> actionCaptor = ArgumentCaptor.forClass(SaveOptionsAction.class);
      ArgumentCaptor<AsyncCallback> callbackCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      verify(dispatcher).execute(actionCaptor.capture(), callbackCaptor.capture());

      SaveOptionsAction action = actionCaptor.getValue();

      int docListSize = Integer.parseInt(action.getConfigurationMap().get(UserOptions.DocumentListPageSize));
      int editorSize = Integer.parseInt(action.getConfigurationMap().get(UserOptions.EditorPageSize));

      boolean showError = Boolean.parseBoolean(action.getConfigurationMap().get(UserOptions.ShowErrors));
      boolean displayButton = Boolean.parseBoolean(action.getConfigurationMap().get(UserOptions.DisplayButtons));

      assertThat(docListSize, Matchers.equalTo(1000));
      assertThat(editorSize, Matchers.equalTo(2000));
      assertThat(displayButton, Matchers.equalTo(false));
      assertThat(showError, Matchers.equalTo(true));

      
      AsyncCallback<SaveOptionsResult> callback = callbackCaptor.getValue();
      callback.onSuccess(new SaveOptionsResult());
      callback.onFailure(null);
      verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
   }

   @Test
   public void getCommonOptions()
   {
      Map<UserOptions, String> map = service.getCommonOptions();
      assertThat(map.size(), Matchers.equalTo(2));
      assertThat(map.containsKey(UserOptions.ShowErrors), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.Themes), Matchers.equalTo(true));
   }

   @Test
   public void getDocumentListOptions()
   {
      Map<UserOptions, String> map = service.getDocumentListOptions();
      assertThat(map.size(), Matchers.equalTo(3));
      assertThat(map.containsKey(UserOptions.ShowErrors), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.Themes), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.DocumentListPageSize), Matchers.equalTo(true));
   }

   @Test
   public void getEditorOptions()
   {
      Map<UserOptions, String> map = service.getEditorOptions();

      assertThat(map.size(), Matchers.equalTo(18));
      
      assertThat(map.containsKey(UserOptions.ShowErrors), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.Themes), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.DisplayButtons), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.EnterSavesApproved), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.EditorPageSize), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.TranslatedMessageFilter), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.UseCodeMirrorEditor), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.TransMemoryDisplayMode), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.EnableSpellCheck), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.FuzzyMessageFilter), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.UntranslatedMessageFilter), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.ApprovedMessageFilter), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.RejectedMessageFilter), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.Navigation), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.ShowSaveApprovedWarning), Matchers.equalTo(true));
      assertThat(map.containsKey(UserOptions.EnableReferenceLang), Matchers.equalTo(true));
   }

   @Test
   public void loadCommonOptions()
   {
      service.loadCommonOptions();

      assertThat(configHolder.getState().isShowError(), Matchers.equalTo(UserConfigHolder.DEFAULT_SHOW_ERROR));
   }

   @Test
   public void loadDocumentListDefaultOptions()
   {
      service.getConfigHolder().setShowError(true);
      service.getConfigHolder().setDocumentListPageSize(2000);

      service.loadDocumentListDefaultOptions();

      assertThat(configHolder.getState().isShowError(), Matchers.equalTo(UserConfigHolder.DEFAULT_SHOW_ERROR));
      assertThat(configHolder.getState().getDocumentListPageSize(), Matchers.equalTo(UserConfigHolder.DEFAULT_DOC_LIST_PAGE_SIZE));

   }

   @Test
   public void loadEditorDefaultOptions()
   {
      service.getConfigHolder().setEditorPageSize(100);
      service.getConfigHolder().setShowError(true);
      service.getConfigHolder().setDisplayButtons(false);

      service.loadEditorDefaultOptions();

      assertThat(configHolder.getState().isShowError(), Matchers.equalTo(UserConfigHolder.DEFAULT_SHOW_ERROR));
      assertThat(configHolder.getState().isDisplayButtons(), Matchers.equalTo(UserConfigHolder.DEFAULT_DISPLAY_BUTTONS));
      assertThat(configHolder.getState().isEnterSavesApproved(), Matchers.equalTo(UserConfigHolder.DEFAULT_ENTER_SAVES_APPROVED));
      assertThat(configHolder.getState().isFilterByFuzzy(), Matchers.equalTo(UserConfigHolder.DEFAULT_FILTER));
      assertThat(configHolder.getState().isFilterByTranslated(), Matchers.equalTo(UserConfigHolder.DEFAULT_FILTER));
      assertThat(configHolder.getState().isFilterByUntranslated(), Matchers.equalTo(UserConfigHolder.DEFAULT_FILTER));
      assertThat(configHolder.getState().isFilterByApproved(), Matchers.equalTo(UserConfigHolder.DEFAULT_FILTER));
      assertThat(configHolder.getState().isFilterByRejected(), Matchers.equalTo(UserConfigHolder.DEFAULT_FILTER));
      assertThat(configHolder.getState().getNavOption(), Matchers.equalTo(NavOption.FUZZY_UNTRANSLATED));
      assertThat(configHolder.getState().getEditorPageSize(), Matchers.equalTo(UserConfigHolder.DEFAULT_EDITOR_PAGE_SIZE));
      assertThat(configHolder.getState().isShowSaveApprovedWarning(), Matchers.equalTo(UserConfigHolder.DEFAULT_SHOW_SAVE_APPROVED_WARNING));
      assertThat(configHolder.getState().isUseCodeMirrorEditor(), Matchers.equalTo(UserConfigHolder.DEFAULT_USE_CODE_MIRROR));
      assertThat(configHolder.getState().getTransMemoryDisplayMode(), Matchers.equalTo(UserConfigHolder.DEFAULT_TM_DISPLAY_MODE));
   }

   @Test
   public void getConfigHolder()
   {
      assertThat(service.getConfigHolder(), Matchers.equalTo(configHolder));
   }
}
