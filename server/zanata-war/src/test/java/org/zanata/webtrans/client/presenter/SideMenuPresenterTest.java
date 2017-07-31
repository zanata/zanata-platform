package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.view.SideMenuDisplay;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class SideMenuPresenterTest {
    private SideMenuPresenter presenter;
    @Mock
    private SideMenuDisplay display;
    @Mock
    private EventBus eventBus;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock
    private OptionsPresenter optionsPresenter;
    @Mock
    private ValidationOptionsPresenter validationOptionsPresenter;
    @Mock
    private WorkspaceUsersPresenter workspaceUsersPresenter;
    @Mock
    private NotificationPresenter notificationPresenter;
    @Mock
    private UserWorkspaceContext userWorkspaceContext;
    @Mock
    private WorkspaceContext workspaceContext;
    @Captor
    private ArgumentCaptor<ShowSideMenuEvent> eventCaptor;
    @Captor
    private ArgumentCaptor<AsyncCallback<GetTranslatorListResult>> callbackCaptor;
    @Mock
    private UserSessionService sessionService;
    private WorkspaceId workspaceId;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        presenter =
                new SideMenuPresenter(display, eventBus, dispatcher,
                        optionsPresenter, validationOptionsPresenter,
                        workspaceUsersPresenter, notificationPresenter,
                        sessionService, userWorkspaceContext);

        workspaceId =
                new WorkspaceId(new ProjectIterationId("projectSlug",
                        "iterationSlug", ProjectType.Podir), LocaleId.EN_US);

        when(userWorkspaceContext.getWorkspaceContext()).thenReturn(
                workspaceContext);
        when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);

        verify(display).setListener(presenter);

    }

    @Test
    public void onBind() {
        presenter.onBind();

        verify(optionsPresenter).bind();
        verify(validationOptionsPresenter).bind();
        verify(workspaceUsersPresenter).bind();
        verify(notificationPresenter).bind();
        verify(notificationPresenter).setNotificationListener(presenter);

        verify(eventBus).addHandler(PublishWorkspaceChatEvent.getType(),
                presenter);
        verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                presenter);
    }

    @Test
    public void onWorkspaceContextUpdated() {
        // Given: on workspace context update event: project active is true but
        // userWorkspaceContext has read only access
        WorkspaceContextUpdateEvent workspaceContextEvent =
                mock(WorkspaceContextUpdateEvent.class);
        when(workspaceContextEvent.isProjectActive()).thenReturn(true);
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(true);

        presenter.onWorkspaceContextUpdated(workspaceContextEvent);

        // Then:
        verify(userWorkspaceContext).setProjectActive(
                workspaceContextEvent.isProjectActive());
        verify(display).setChatTabVisible(false);
        verify(display, times(1)).setSelectedTab(
                SideMenuDisplay.NOTIFICATION_VIEW);
        verify(display).setValidationOptionsTabVisible(false);
    }

    @Test
    public void onBindWillLoadTranslatorList() {
        GetTranslatorListResult result = mock(GetTranslatorListResult.class);

        presenter.onBind();

        // on calling get translator list callback success
        verify(dispatcher).execute(Mockito.eq(GetTranslatorList.ACTION),
                callbackCaptor.capture());
        AsyncCallback<GetTranslatorListResult> callback =
                callbackCaptor.getValue();
        callback.onSuccess(result);
        verify(result).getTranslatorList();
        verify(sessionService).initUserList(result.getTranslatorList());
    }

    @Test
    public void onPublishChatEventAndCurrentTabIsNotWorkspaceUserView() {
        when(display.getCurrentTab()).thenReturn(SideMenuDisplay.OPTION_VIEW);

        presenter.onPublishWorkspaceChat(null);

        verify(display).setChatTabAlert(true);
    }

    @Test
    public void onPublishChatEventAndCurrentTabIsWorkspaceUserView() {
        when(display.getCurrentTab()).thenReturn(
                SideMenuDisplay.WORKSPACEUSER_VIEW);

        presenter.onPublishWorkspaceChat(null);

        verify(display).getCurrentTab();
        verifyNoMoreInteractions(display);
    }

    @Test
    public void onUnbind() {
        presenter.onUnbind();

        verify(optionsPresenter).unbind();
        verify(validationOptionsPresenter).unbind();
        verify(workspaceUsersPresenter).unbind();
        verify(notificationPresenter).unbind();
    }

    @Test
    public void testShowEditorMenu() throws Exception {
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
        presenter.expendSideMenu(true);

        presenter.setOptionMenu(MainView.Editor);

        verify(optionsPresenter).setOptionsView(MainView.Editor);
    }

    @Test
    public void testShowValidationOptions() throws Exception {
        boolean visible = true;
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
        presenter.expendSideMenu(true);

        presenter.showValidationOptions(visible);

        verify(display).setValidationOptionsTabVisible(visible);
    }

    @Test
    public void testOnEditorOptionsClickNotExpanded() throws Exception {
        // Given: initial side menu is NOT expanded
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

        // When:
        presenter.onOptionsClick();

        // Then:
        verify(display).setSelectedTab(SideMenuDisplay.OPTION_VIEW);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isTrue();
        verifyNoMoreInteractions(display);
    }

    @Test
    public void
            testOnEditorOptionsClickExpandedAndCurrentTabIsNotEditorOptionTab()
                    throws Exception {
        // Given: initial side menu is expanded and current tab is workspace
        // user view
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
        presenter.expendSideMenu(true);
        when(display.getCurrentTab()).thenReturn(
                SideMenuDisplay.WORKSPACEUSER_VIEW);

        // When:
        presenter.onOptionsClick();

        // Then:
        verify(display).getCurrentTab();
        verify(display).setSelectedTab(SideMenuDisplay.OPTION_VIEW);
        verifyNoMoreInteractions(display);
    }

    @Test
    public void
            testOnEditorOptionsClickExpandedAndCurrentTabIsEditorOptionTab()
                    throws Exception {
        // Given: initial side menu is expanded and current tab is editor option
        // view
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
        presenter.expendSideMenu(true);
        when(display.getCurrentTab()).thenReturn(SideMenuDisplay.OPTION_VIEW);

        // When:
        presenter.onOptionsClick();

        // Then:
        verify(display).getCurrentTab();
        verify(eventBus, times(2)).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isFalse();
        verifyNoMoreInteractions(display);
    }

    @Test
    public void testOnNotificationClick() throws Exception {
        // Given: initial side menu is NOT expanded
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

        // When:
        presenter.onNotificationClick();

        // Then:
        verify(display).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isTrue();
        verifyNoMoreInteractions(display);

    }

    @Test
    public void testOnValidationOptionsClick() throws Exception {
        // Given: initial side menu is NOT expanded
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

        // When:
        presenter.onValidationOptionsClick();

        // Then:
        verify(display).setSelectedTab(SideMenuDisplay.VALIDATION_OPTION_VIEW);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isTrue();
        verifyNoMoreInteractions(display);

    }

    @Test
    public void testOnChatClick() throws Exception {
        // Given: initial side menu is NOT expanded
        when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

        // When:
        presenter.onChatClick();

        // Then:
        verify(display).setSelectedTab(SideMenuDisplay.WORKSPACEUSER_VIEW);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isTrue();
        verifyNoMoreInteractions(display);
    }

    @Test
    public void testSetNotificationLabel() throws Exception {
        presenter.setNotificationLabel(9, NotificationEvent.Severity.Error);

        verify(display)
                .setNotificationText(9, NotificationEvent.Severity.Error);

    }

    @Test
    public void testShowNotificationIfNotExpanded() throws Exception {
        presenter.showNotification();

        verify(display).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
    }

    @Test
    public void showNotificationIfExpanded() {
        // Given: expanded and current tab is editor options
        presenter.expendSideMenu(true);
        verify(eventBus).fireEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().isShowing()).isTrue();
        when(display.getCurrentTab()).thenReturn(SideMenuDisplay.OPTION_VIEW);

        // When:
        presenter.showNotification();

        // Then:
        verify(display, atLeastOnce()).setSelectedTab(
                SideMenuDisplay.NOTIFICATION_VIEW);
    }
}
