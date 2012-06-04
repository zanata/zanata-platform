package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;


@Test(groups = { "unit-tests" })
public class WorkspaceUsersPresenterTest
{
   // object under test
   WorkspaceUsersPresenter workspaceUsersPresenter;

   //injected mocks
   Display mockDisplay = createMock(Display.class);
   EventBus mockEventBus = createMock(EventBus.class);
   
   Identity mockIdentity = createMock(Identity.class);
   CachingDispatchAsync mockDispatcher = createMock(CachingDispatchAsync.class);
   HasClickHandlers mockSendButton = createMock(HasClickHandlers.class);
   WebTransMessages mockMessages = createMock(WebTransMessages.class);

   Capture<ClickHandler> capturedSendButtonClickHandler = new Capture<ClickHandler>();

   UserSessionService mockSessionService = createMock(UserSessionService.class);

   private final static String PUBLIC_CHANNEL_WARN = "Warning! This is a public channel";

   @BeforeMethod
   public void resetMocks()
   {
      reset(mockDisplay, mockEventBus, mockSendButton, mockSessionService, mockMessages, mockDispatcher, mockIdentity);
      workspaceUsersPresenter = new WorkspaceUsersPresenter(mockDisplay, mockEventBus, mockIdentity, mockDispatcher, mockMessages, mockSessionService);
   }

   public void setEmptyUserList()
   {
      expect(mockDisplay.getSendButton()).andReturn(mockSendButton);
      expect(mockSendButton.addClickHandler(capture(capturedSendButtonClickHandler))).andReturn(createMock(HandlerRegistration.class));

      expect(mockMessages.thisIsAPublicChannel()).andReturn(PUBLIC_CHANNEL_WARN);

      mockDisplay.appendChat(null, null, PUBLIC_CHANNEL_WARN, MESSAGE_TYPE.SYSTEM_WARNING);
      expectLastCall().once();
      
      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), isA(PublishWorkspaceChatEventHandler.class))).andReturn(createMock(HandlerRegistration.class));
      replayAll();

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<EditorClientId, PersonSessionDetails>());

      verifyAll();
   }

   public void setNonEmptyUserList()
   {
      Person person1 = new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16");
      Person person2 = new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16");
      Person person3 = new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16");
      
      EditorClientId editorClientId1 = new EditorClientId("sessionId1");
      EditorClientId editorClientId2 = new EditorClientId("sessionId2");
      EditorClientId editorClientId3 = new EditorClientId("sessionId3");

      HasManageUserPanel mockPanel1 = createMock(HasManageUserPanel.class);
      HasManageUserPanel mockPanel2 = createMock(HasManageUserPanel.class);
      HasManageUserPanel mockPanel3 = createMock(HasManageUserPanel.class);

      UserPanelSessionItem mockItem1 = new UserPanelSessionItem(mockPanel1, person1);
      UserPanelSessionItem mockItem2 = new UserPanelSessionItem(mockPanel2, person2);
      UserPanelSessionItem mockItem3 = new UserPanelSessionItem(mockPanel3, person3);

      expect(mockMessages.thisIsAPublicChannel()).andReturn(PUBLIC_CHANNEL_WARN);

      mockDisplay.appendChat(null, null, PUBLIC_CHANNEL_WARN, MESSAGE_TYPE.SYSTEM_WARNING);
      expectLastCall().once();

      expect(mockSessionService.getColor("sessionId1")).andReturn("color1");
      expect(mockSessionService.getColor("sessionId2")).andReturn("color2");
      expect(mockSessionService.getColor("sessionId3")).andReturn("color3");

      expect(mockSessionService.getUserPanel(editorClientId1)).andReturn(mockItem1);
      expect(mockSessionService.getUserPanel(editorClientId2)).andReturn(mockItem2);
      expect(mockSessionService.getUserPanel(editorClientId3)).andReturn(mockItem3);
      
      mockSessionService.updateTranslatorStatus(editorClientId1, null);
      expectLastCall().once();

      mockSessionService.updateTranslatorStatus(editorClientId2, null);
      expectLastCall().once();

      mockSessionService.updateTranslatorStatus(editorClientId3, null);
      expectLastCall().once();

      expect(mockDisplay.getSendButton()).andReturn(mockSendButton);
      expect(mockSendButton.addClickHandler(capture(capturedSendButtonClickHandler))).andReturn(createMock(HandlerRegistration.class));

      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), isA(PublishWorkspaceChatEventHandler.class))).andReturn(createMock(HandlerRegistration.class));
      replayAll();

      workspaceUsersPresenter.bind();

      Map<EditorClientId, PersonSessionDetails> people = new HashMap<EditorClientId, PersonSessionDetails>();
      people.put(new EditorClientId("sessionId1"), new PersonSessionDetails(person1, null));
      people.put(new EditorClientId("sessionId2"), new PersonSessionDetails(person2, null));
      people.put(new EditorClientId("sessionId3"), new PersonSessionDetails(person3, null));
      workspaceUsersPresenter.initUserList(people);

      verifyAll();
   }
   
   private void verifyAll()
   {
      verify(mockDisplay, mockEventBus, mockSendButton, mockSessionService, mockMessages, mockIdentity, mockDispatcher);
   }

   private void replayAll()
   {
      replay(mockDisplay, mockEventBus, mockSendButton, mockSessionService, mockMessages, mockIdentity, mockDispatcher);
   }
}
