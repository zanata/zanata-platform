package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
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
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;

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

   @BeforeMethod
   public void resetMocks()
   {
      reset(mockDisplay, mockEventBus, mockSendButton, mockSessionService);
      workspaceUsersPresenter = new WorkspaceUsersPresenter(mockDisplay, mockEventBus, mockIdentity, mockDispatcher, mockMessages, mockSessionService);
   }

   public void setEmptyUserList()
   {
      expect(mockDisplay.getSendButton()).andReturn(mockSendButton);
      expect(mockSendButton.addClickHandler(capture(capturedSendButtonClickHandler))).andReturn(createMock(HandlerRegistration.class));

      expect(mockEventBus.addHandler(eq(TransUnitEditEvent.getType()), isA(TransUnitEditEventHandler.class)) ).andReturn(createMock(HandlerRegistration.class));
      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), isA(PublishWorkspaceChatEventHandler.class))).andReturn(createMock(HandlerRegistration.class));
      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<SessionId, PersonSessionDetails>());

      verify(mockDisplay, mockEventBus);
   }

   public void setNonEmptyUserList()
   {
      HasManageUserPanel mockHasManageUserSession = createMock(HasManageUserPanel.class);

      expect(mockSessionService.getColor("sessionId1")).andReturn("color1");
      expect(mockSessionService.getColor("sessionId2")).andReturn("color2");
      expect(mockSessionService.getColor("sessionId3")).andReturn("color3");

      expect(mockDisplay.getSendButton()).andReturn(mockSendButton);
      expect(mockSendButton.addClickHandler(capture(capturedSendButtonClickHandler))).andReturn(createMock(HandlerRegistration.class));

      expect(mockDisplay.addUser(new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);

      expect(mockEventBus.addHandler(eq(TransUnitEditEvent.getType()), isA(TransUnitEditEventHandler.class)) ).andReturn(createMock(HandlerRegistration.class));
      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), isA(PublishWorkspaceChatEventHandler.class))).andReturn(createMock(HandlerRegistration.class));
      replay(mockDisplay, mockEventBus, mockSendButton);

      workspaceUsersPresenter.bind();

      Map<SessionId, PersonSessionDetails> people = new HashMap<SessionId, PersonSessionDetails>();
      people.put(new SessionId("sessionId1"), new PersonSessionDetails(new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"), null));
      people.put(new SessionId("sessionId2"), new PersonSessionDetails(new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"), null));
      people.put(new SessionId("sessionId3"), new PersonSessionDetails(new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"), null));
      workspaceUsersPresenter.initUserList(people);

      verify(mockDisplay, mockEventBus, mockSendButton, mockSessionService);
   }
}
