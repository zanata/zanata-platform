package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.EditorOptionsDisplay;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.zanata.webtrans.client.view.ChangeReferenceLangDisplay;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

public class EditorOptionsPresenterTest {
    private EditorOptionsPresenter presenter;
    @Mock
    private EditorOptionsDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private UserWorkspaceContext userWorkspaceContext;
    @Mock
    private WorkspaceContext workspaceContext;
    @Mock
    private ValidationOptionsPresenter validationDetailsPresenter;
    @Mock
    private ChangeReferenceLangPresenter changeReferenceLangPresenter;
    @Mock
    private ChangeReferenceLangDisplay changeReferenceLangDisplay;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock
    private UserOptionsService userOptionsService;
    @Mock
    private WebTransMessages messages;
    @Captor
    ArgumentCaptor<AsyncCallback<LoadOptionsResult>> asyncCallbackArgumentCaptor;

    private UserConfigHolder configHolder = new UserConfigHolder();

    private WorkspaceId workspaceId;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        presenter =
                new EditorOptionsPresenter(messages, display, eventBus,
                        userWorkspaceContext, validationDetailsPresenter,
                        changeReferenceLangPresenter,
                        dispatcher, userOptionsService);

        workspaceId =
                new WorkspaceId(new ProjectIterationId("projectSlug",
                        "iterationSlug", ProjectType.Podir), LocaleId.EN_US);

        when(userWorkspaceContext.getWorkspaceContext()).thenReturn(
                workspaceContext);
        when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);

        verify(display).setListener(presenter);
    }

    @Test
    public void onBindWillRegisterHandlers() {
        // Given: user workspace context is not readonly
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

        when(changeReferenceLangPresenter.getDisplay())
                .thenReturn(changeReferenceLangDisplay);

        // When:
        presenter.onBind();

        // Then:
        verify(validationDetailsPresenter).bind();

        verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                presenter);
        verify(display).setOptionsState(
                userOptionsService.getConfigHolder().getState());
    }

    @Test
    public void canSetReadOnlyOnWorkspaceUpdate() {
        // Given: project become inactive
        WorkspaceContextUpdateEvent workspaceContextUpdateEvent =
                new WorkspaceContextUpdateEvent(workplaceContextData(false,
                        ProjectType.Podir));
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false, true);
        when(userOptionsService.getConfigHolder()).thenReturn(configHolder);

        // When:
        presenter.onWorkspaceContextUpdated(workspaceContextUpdateEvent);

        // Then:
        verify(userWorkspaceContext).setProjectActive(false);
        assertThat(configHolder.getState().isDisplayButtons()).isFalse();
        verify(display).setOptionsState(configHolder.getState());
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    private static HasWorkspaceContextUpdateData workplaceContextData(
            final boolean projectActive, final ProjectType projectType) {
        return new HasWorkspaceContextUpdateData() {
            @Override
            public boolean isProjectActive() {
                return projectActive;
            }

            @Override
            public ProjectType getProjectType() {
                return projectType;
            }

            @Override
            public Map<ValidationId, State> getValidationStates() {
                return null;
            }

            @Override
            public String getOldProjectSlug() {
                return null;
            }

            @Override
            public String getNewProjectSlug() {
                return null;
            }

            @Override
            public String getOldIterationSlug() {
                return null;
            }

            @Override
            public String getNewIterationSlug() {
                return null;
            }
        };
    }

    @Test
    public void onPageSizeClick() {
        presenter.onPageSizeClick(99);

        assertThat(configHolder.getState().getEditorPageSize())
                .isEqualTo(99);
        ArgumentCaptor<EditorPageSizeChangeEvent> eventCaptor =
                ArgumentCaptor.forClass(EditorPageSizeChangeEvent.class);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getPageSize()).isEqualTo(99);
    }

    @Test
    public void onEnterSaveApprovedOptionChange() {
        configHolder.setEnterSavesApproved(false);

        presenter.onEnterSaveOptionChanged(true);

        assertThat(configHolder.getState().isEnterSavesApproved()).isTrue();
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onEditorButtonsOptionChange() {
        configHolder.setDisplayButtons(false);

        presenter.onEditorButtonsOptionChanged(true);

        assertThat(configHolder.getState().isDisplayButtons()).isTrue();
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onNavOptionChange() {
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);

        presenter.onSelectionChange("", NavOption.FUZZY);

        assertThat(configHolder.getState().getNavOption())
                .isEqualTo(NavOption.FUZZY);
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onShowSaveApprovedWarningChanged() {
        configHolder.setShowSaveApprovedWarning(true);

        presenter.onShowSaveApprovedWarningChanged(false);

        assertThat(configHolder.getState().isShowSaveApprovedWarning()).isFalse();
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onTMDisplayModeChanged() {
        configHolder.setTMDisplayMode(DiffMode.HIGHLIGHT);

        presenter.onTransMemoryDisplayModeChanged(DiffMode.NORMAL);

        assertThat(configHolder.getState().getTransMemoryDisplayMode())
                .isEqualTo(DiffMode.NORMAL);
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
    }

    @Test
    public void onCodeMirrorOptionChanged() {
        configHolder.setUseCodeMirrorEditor(false);

        presenter.onUseCodeMirrorOptionChanged(true, false);

        assertThat(configHolder.getState().isUseCodeMirrorEditor()).isTrue();
        verify(eventBus).fireEvent(RefreshPageEvent.REDRAW_PAGE_EVENT);
    }

    @Test
    public void onLoadDefaultOptions() {
        presenter.loadDefaultOptions();

        verify(userOptionsService).loadEditorDefaultOptions();

        verify(display).setOptionsState(
                isA(UserConfigHolder.ConfigurationState.class));
        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
        verify(eventBus).fireEvent(isA(NotificationEvent.class));
    }

    @Test
    public void onPersistOption() {
        configHolder.setFilterByFuzzy(true);
        configHolder.setFilterByTranslated(false);
        configHolder.setFilterByUntranslated(true);

        presenter.persistOptionChange();

        verify(userOptionsService).persistOptionChange(
                userOptionsService.getEditorOptions());
    }

    @Test
    public void onLoadSavedOption() {
        UserConfigHolder configHolder = new UserConfigHolder();
        configHolder.setEnterSavesApproved(true);
        configHolder.setFilterByTranslated(true);
        configHolder.setNavOption(NavOption.FUZZY);
        configHolder.setEditorPageSize(10);

        LoadOptionsResult result =
                new LoadOptionsResult(configHolder.getState());

        presenter.loadOptions();

        ArgumentCaptor<LoadOptionsAction> actionCaptor =
                ArgumentCaptor.forClass(LoadOptionsAction.class);
        verify(dispatcher).execute(actionCaptor.capture(),
                asyncCallbackArgumentCaptor.capture());

        AsyncCallback<LoadOptionsResult> callback = asyncCallbackArgumentCaptor.getValue();

        // when(needReviewChk.getValue()).thenReturn(false);
        // when(translatedChk.getValue()).thenReturn(true);
        // when(untranslatedChk.getValue()).thenReturn(false);
        callback.onSuccess(result);
        assertThat(configHolder.getState().getEditorPageSize()).isEqualTo(10);
        assertThat(configHolder.getState().getNavOption())
                .isEqualTo(NavOption.FUZZY);

        verify(eventBus).fireEvent(
                UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

        callback.onFailure(null);
        verify(eventBus, times(2)).fireEvent(isA(NotificationEvent.class));
    }
}
