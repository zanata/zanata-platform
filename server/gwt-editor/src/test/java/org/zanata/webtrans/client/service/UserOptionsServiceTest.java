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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.zanata.webtrans.shared.model.UserOptions.*;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
import org.zanata.webtrans.client.resources.WebTransMessages;
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
public class UserOptionsServiceTest {
    private UserOptionsService service;

    @Mock
    private EventBus eventBus;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock
    private WebTransMessages messages;
    @Captor
    private ArgumentCaptor<AsyncCallback<SaveOptionsResult>> callbackCaptor;

    private UserConfigHolder configHolder = new UserConfigHolder();

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        service = new UserOptionsService(messages, eventBus, dispatcher, configHolder);
    }

    @Test
    public void persistOptionChange() {
        Map<UserOptions, String> map = new HashMap<>();
        map.put(DocumentListPageSize, String.valueOf(1000));
        map.put(EditorPageSize, String.valueOf(2000));

        map.put(DisplayButtons, String.valueOf(false));
        map.put(ShowErrors, String.valueOf(true));

        service.persistOptionChange(map);

        ArgumentCaptor<SaveOptionsAction> actionCaptor =
                ArgumentCaptor.forClass(SaveOptionsAction.class);
        verify(dispatcher).execute(actionCaptor.capture(),
                callbackCaptor.capture());

        SaveOptionsAction action = actionCaptor.getValue();

        int docListSize =
                Integer.parseInt(action.getConfigurationMap().get(
                        DocumentListPageSize));
        int editorSize =
                Integer.parseInt(action.getConfigurationMap().get(
                        EditorPageSize));

        boolean showError =
                Boolean.parseBoolean(action.getConfigurationMap().get(
                        ShowErrors));
        boolean displayButton =
                Boolean.parseBoolean(action.getConfigurationMap().get(
                        DisplayButtons));

        assertThat(docListSize).isEqualTo(1000);
        assertThat(editorSize).isEqualTo(2000);
        assertThat(displayButton).isFalse();
        assertThat(showError).isTrue();

        AsyncCallback<SaveOptionsResult> callback = callbackCaptor.getValue();
        callback.onSuccess(new SaveOptionsResult());
        callback.onFailure(null);
        verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
    }

    @Test
    public void getCommonOptions() {
        Map<UserOptions, String> map = service.getCommonOptions();
        assertThat(map.size()).isEqualTo(2);
        assertThat(map.containsKey(ShowErrors)).isTrue();
        assertThat(map.containsKey(Themes)).isTrue();
    }

    @Test
    public void getDocumentListOptions() {
        Map<UserOptions, String> map = service.getDocumentListOptions();
        assertThat(map).hasSize(3);
        assertThat(map).containsKey(ShowErrors);
        assertThat(map).containsKey(Themes);
        assertThat(map).containsKey(DocumentListPageSize);
    }

    @Test
    public void getEditorOptions() {
        Map<UserOptions, String> map = service.getEditorOptions();

        assertThat(map.keySet()).contains(ShowErrors,
                Themes, DisplayButtons, EnterSavesApproved, EditorPageSize,
                TranslatedMessageFilter, UseCodeMirrorEditor,
                TransMemoryDisplayMode, FuzzyMessageFilter,
                UntranslatedMessageFilter, ApprovedMessageFilter,
                RejectedMessageFilter, Navigation, ShowSaveApprovedWarning,
                SelectedReferenceLang, DisplayTransMemory, DisplayGlossary);
    }

    @Test
    public void loadCommonOptions() {
        service.loadCommonOptions();
        assertThat(configHolder.getState().isShowError())
                .isEqualTo(UserConfigHolder.DEFAULT_SHOW_ERROR);
    }

    @Test
    public void loadDocumentListDefaultOptions() {
        service.getConfigHolder().setShowError(true);
        service.getConfigHolder().setDocumentListPageSize(2000);
        service.loadDocumentListDefaultOptions();

        assertThat(configHolder.getState().isShowError())
                .isEqualTo(UserConfigHolder.DEFAULT_SHOW_ERROR);
        assertThat(configHolder.getState().getDocumentListPageSize())
                .isEqualTo(UserConfigHolder.DEFAULT_DOC_LIST_PAGE_SIZE);

    }

    @Test
    public void loadEditorDefaultOptions() {
        service.getConfigHolder().setEditorPageSize(100);
        service.getConfigHolder().setShowError(true);
        service.getConfigHolder().setDisplayButtons(false);

        service.loadEditorDefaultOptions();

        assertThat(configHolder.getState().isShowError())
                .isEqualTo(UserConfigHolder.DEFAULT_SHOW_ERROR);
        assertThat(configHolder.getState().isDisplayButtons())
                .isEqualTo(UserConfigHolder.DEFAULT_DISPLAY_BUTTONS);
        assertThat(configHolder.getState().isEnterSavesApproved())
                .isEqualTo(UserConfigHolder.DEFAULT_ENTER_SAVES_APPROVED);
        assertThat(configHolder.getState().isFilterByFuzzy())
                .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().isFilterByTranslated())
                .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().isFilterByUntranslated())
                .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().isFilterByApproved())
                .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().isFilterByRejected())
                .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().isFilterByMT())
            .isEqualTo(UserConfigHolder.DEFAULT_FILTER);
        assertThat(configHolder.getState().getNavOption())
                .isEqualTo(NavOption.FUZZY_UNTRANSLATED);
        assertThat(configHolder.getState().getEditorPageSize())
                .isEqualTo(UserConfigHolder.DEFAULT_EDITOR_PAGE_SIZE);
        assertThat(configHolder.getState().isShowSaveApprovedWarning())
                .isEqualTo(UserConfigHolder.DEFAULT_SHOW_SAVE_APPROVED_WARNING);
        assertThat(configHolder.getState().isUseCodeMirrorEditor())
                .isEqualTo(UserConfigHolder.DEFAULT_USE_CODE_MIRROR);
        assertThat(configHolder.getState().getTransMemoryDisplayMode())
                .isEqualTo(UserConfigHolder.DEFAULT_TM_DISPLAY_MODE);
        assertThat(configHolder.getState().getSelectedReferenceForSourceLang())
                .isEqualTo(UserConfigHolder.DEFAULT_SELECTED_REFERENCE);
    }

    @Test
    public void getConfigHolder() {
        assertThat(service.getConfigHolder()).isEqualTo(configHolder);
    }
}
