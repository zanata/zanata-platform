package org.zanata.webtrans.client.presenter;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.SideMenuDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData;

import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class SideMenuPresenterTest
{
   private SideMenuPresenter presenter;
   @Mock
   private SideMenuDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private EditorOptionsPresenter editorOptionsPresenter;
   @Mock
   private ValidationOptionsPresenter validationOptionsPresenter;
   @Mock
   private WorkspaceUsersPresenter workspaceUsersPresenter;
   @Mock
   private NotificationPresenter notificationPresenter;
   @Mock
   private UserWorkspaceContext userWorkspaceContext;
   @Mock
   private WebTransMessages messages;
   @Captor
   private ArgumentCaptor<ShowSideMenuEvent> eventCaptor;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      presenter = new SideMenuPresenter(display, eventBus, dispatcher, editorOptionsPresenter, validationOptionsPresenter, workspaceUsersPresenter, notificationPresenter, userWorkspaceContext, messages);
      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      presenter.onBind();

      verify(editorOptionsPresenter).bind();
      verify(validationOptionsPresenter).bind();
      verify(workspaceUsersPresenter).bind();
      verify(notificationPresenter).bind();
      verify(notificationPresenter).setNotificationListener(presenter);

      verify(eventBus).addHandler(EnterWorkspaceEvent.getType(), presenter);
      verify(eventBus).addHandler(ExitWorkspaceEvent.getType(), presenter);
      verify(eventBus).addHandler(PublishWorkspaceChatEvent.getType(), presenter);

      // Given: on workspace context update event: project active is true but userWorkspaceContext has read only access
      ArgumentCaptor<WorkspaceContextUpdateEventHandler> workspaceContextUpdateEventHandlerCaptor = ArgumentCaptor.forClass(WorkspaceContextUpdateEventHandler.class);
      WorkspaceContextUpdateEvent workspaceContextEvent = mock(WorkspaceContextUpdateEvent.class);
      when(workspaceContextEvent.isProjectActive()).thenReturn(true);
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(true);

      // When:
      verify(eventBus).addHandler(eq(WorkspaceContextUpdateEvent.getType()), workspaceContextUpdateEventHandlerCaptor.capture());
      WorkspaceContextUpdateEventHandler contextUpdateEventHandler = workspaceContextUpdateEventHandlerCaptor.getValue();
      contextUpdateEventHandler.onWorkspaceContextUpdated(workspaceContextEvent);

      // Then:
      verify(userWorkspaceContext).setProjectActive(workspaceContextEvent.isProjectActive());
      verify(display).setChatTabVisible(false);
      verify(display).setEditorOptionsTabVisible(false);
      verify(display).setValidationOptionsTabVisible(false);
   }

   @Test
   @SuppressWarnings("unchecked")
   public void onBindWillLoadTranslatorList()
   {
      ArgumentCaptor<AsyncCallback> callbackCaptor = ArgumentCaptor.forClass(AsyncCallback.class);
      GetTranslatorListResult result = mock(GetTranslatorListResult.class);

      presenter.onBind();

      // on calling get translator list callback success
      verify(dispatcher).execute(Mockito.eq(GetTranslatorList.ACTION), callbackCaptor.capture());
      AsyncCallback callback = callbackCaptor.getValue();
      callback.onSuccess(result);
      verify(result).getTranslatorList();
      verify(workspaceUsersPresenter).initUserList(result.getTranslatorList());
   }

   @Test
   public void onEnterWorkspaceEvent()
   {
      EnterWorkspaceEvent event = mock(EnterWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);
      when(messages.hasJoinedWorkspace(person.getId().toString())).thenReturn("someone entered");

      presenter.onEnterWorkspace(event);

      verify(workspaceUsersPresenter).addTranslator(editorClientId, person, null);
      verify(workspaceUsersPresenter).dispatchChatAction(null, "someone entered", HasWorkspaceChatData.MESSAGE_TYPE.SYSTEM_MSG);
   }

   private static Person person()
   {
      return new Person(new PersonId("pid"), "someone", "url");
   }

   private static EditorClientId editorClientId()
   {
      return new EditorClientId("session", 1);
   }

   @Test
   public void onExitWorkspaceEvent()
   {
      ExitWorkspaceEvent event = mock(ExitWorkspaceEvent.class);
      EditorClientId editorClientId = editorClientId();
      Person person = person();
      when(event.getEditorClientId()).thenReturn(editorClientId);
      when(event.getPerson()).thenReturn(person);

      presenter.onExitWorkspace(event);

      verify(workspaceUsersPresenter).removeTranslator(editorClientId, person);
   }

   @Test
   public void onPublishChatEventAndCurrentTabIsNotWorkspaceUserView()
   {
      when(display.getCurrentTab()).thenReturn(SideMenuDisplay.EDITOR_OPTION_VIEW);

      presenter.onPublishWorkspaceChat(null);

      verify(display).setChatTabAlert(true);
   }

   @Test
   public void onPublishChatEventAndCurrentTabIsWorkspaceUserView()
   {
      when(display.getCurrentTab()).thenReturn(SideMenuDisplay.WORKSPACEUSER_VIEW);

      presenter.onPublishWorkspaceChat(null);

      verify(display).getCurrentTab();
      verifyNoMoreInteractions(display);
   }

   @Test
   public void onUnbind()
   {
      presenter.onUnbind();

      verify(editorOptionsPresenter).unbind();
      verify(validationOptionsPresenter).unbind();
      verify(workspaceUsersPresenter).unbind();
      verify(notificationPresenter).unbind();
   }

   @Test
   public void testShowEditorMenu() throws Exception
   {
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
      presenter.expendSideMenu(true);

      presenter.showEditorMenu(true);

      verify(display).setEditorOptionsTabVisible(true);
      verify(display).setValidationOptionsTabVisible(true);
      verify(display).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
   }

   @Test
   public void testHideEditorMenu()
   {
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      presenter.showEditorMenu(false);

      verify(display).setEditorOptionsTabVisible(false);
      verify(display).setValidationOptionsTabVisible(false);
      verify(display).setSelectedTab(SideMenuDisplay.EMPTY_VIEW);
   }

   @Test
   public void testOnEditorOptionsClickNotExpanded() throws Exception
   {
      // Given: initial side menu is NOT expanded
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onEditorOptionsClick();

      // Then:
      verify(display).setSelectedTab(SideMenuDisplay.EDITOR_OPTION_VIEW);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.equalTo(true));
      verifyNoMoreInteractions(display);
   }

   @Test
   public void testOnEditorOptionsClickExpandedAndCurrentTabIsNotEditorOptionTab() throws Exception
   {
      // Given: initial side menu is expanded and current tab is workspace user view
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
      presenter.expendSideMenu(true);
      when(display.getCurrentTab()).thenReturn(SideMenuDisplay.WORKSPACEUSER_VIEW);

      // When:
      presenter.onEditorOptionsClick();

      // Then:
      verify(display).getCurrentTab();
      verify(display).setSelectedTab(SideMenuDisplay.EDITOR_OPTION_VIEW);
      verifyNoMoreInteractions(display);
   }

   @Test
   public void testOnEditorOptionsClickExpandedAndCurrentTabIsEditorOptionTab() throws Exception
   {
      // Given: initial side menu is expanded and current tab is editor option view
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
      presenter.expendSideMenu(true);
      when(display.getCurrentTab()).thenReturn(SideMenuDisplay.EDITOR_OPTION_VIEW);

      // When:
      presenter.onEditorOptionsClick();

      // Then:
      verify(display).getCurrentTab();
      verify(eventBus, times(2)).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.equalTo(false));
      verify(display).setSelectedTab(SideMenuDisplay.EMPTY_VIEW);
      verifyNoMoreInteractions(display);
   }

   @Test
   public void testOnNotificationClick() throws Exception
   {
      // Given: initial side menu is NOT expanded
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onNotificationClick();

      // Then:
      verify(display).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.equalTo(true));
      verifyNoMoreInteractions(display);

   }

   @Test
   public void testOnValidationOptionsClick() throws Exception
   {
      // Given: initial side menu is NOT expanded
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onValidationOptionsClick();

      // Then:
      verify(display).setSelectedTab(SideMenuDisplay.VALIDATION_OPTION_VIEW);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.equalTo(true));
      verifyNoMoreInteractions(display);

   }

   @Test
   public void testOnChatClick() throws Exception
   {
      // Given: initial side menu is NOT expanded
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);

      // When:
      presenter.onChatClick();

      // Then:
      verify(display).setSelectedTab(SideMenuDisplay.WORKSPACEUSER_VIEW);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.equalTo(true));
      verifyNoMoreInteractions(display);
   }

   @Test
   public void testSetNotificationLabel() throws Exception
   {
      presenter.setNotificationLabel(9, NotificationEvent.Severity.Error);

      verify(display).setNotificationText(9, NotificationEvent.Severity.Error);

   }

   @Test
   public void testShowNotificationIfNotExpanded() throws Exception
   {
      presenter.showNotification();

      verify(display).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
   }

   @Test
   public void showNotificationIfExpanded()
   {
      // Given: expanded and current tab is editor options
      presenter.expendSideMenu(true);
      verify(eventBus).fireEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().isShowing(), Matchers.is(true));
      when(display.getCurrentTab()).thenReturn(SideMenuDisplay.EDITOR_OPTION_VIEW);

      // When:
      presenter.showNotification();

      // Then:
      verify(display, atLeastOnce()).setSelectedTab(SideMenuDisplay.NOTIFICATION_VIEW);
   }
}
