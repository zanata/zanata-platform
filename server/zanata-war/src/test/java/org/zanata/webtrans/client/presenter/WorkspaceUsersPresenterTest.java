package org.zanata.webtrans.client.presenter;

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

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TranslatorStatusUpdateEvent;
import org.zanata.webtrans.client.events.TranslatorStatusUpdateEventHandler;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;

import com.google.gwt.event.shared.HandlerRegistration;


@Test(groups = { "unit-tests" })
public class WorkspaceUsersPresenterTest
{
   // object under test
   WorkspaceUsersPresenter workspaceUsersPresenter;

   //injected mocks
   Display mockDisplay = createMock(Display.class);
   EventBus mockEventBus = createMock(EventBus.class);

   @BeforeMethod
   public void resetMocks()
   {
      reset(mockDisplay, mockEventBus);
      workspaceUsersPresenter = new WorkspaceUsersPresenter(mockDisplay, mockEventBus);
   }

   public void setEmptyUserList()
   {
      expect(mockEventBus.addHandler(eq(TranslatorStatusUpdateEvent.getType()), isA(TranslatorStatusUpdateEventHandler.class)) ).andReturn(createMock(HandlerRegistration.class));
      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<SessionId, PersonSessionDetails>());

      verify(mockDisplay, mockEventBus);
   }

   public void setNonEmptyUserList()
   {
      HasManageUserSession mockHasManageUserSession = createMock(HasManageUserSession.class);

      expect(mockDisplay.addUser(new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);

      expect(mockEventBus.addHandler(eq(TranslatorStatusUpdateEvent.getType()), isA(TranslatorStatusUpdateEventHandler.class)) ).andReturn(createMock(HandlerRegistration.class));
      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();

      Map<SessionId, PersonSessionDetails> people = new HashMap<SessionId, PersonSessionDetails>();
      people.put(new SessionId("sessionId1"), new PersonSessionDetails(new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"), null));
      people.put(new SessionId("sessionId2"), new PersonSessionDetails(new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"), null));
      people.put(new SessionId("sessionId3"), new PersonSessionDetails(new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"), null));
      workspaceUsersPresenter.initUserList(people);

      verify(mockDisplay, mockEventBus);
   }
}
