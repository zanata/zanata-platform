package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;


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
      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<SessionId, Person>());

      verify(mockDisplay, mockEventBus);
   }

   public void setNonEmptyUserList()
   {
      HasManageUserSession mockHasManageUserSession = createMock(HasManageUserSession.class);

      expect(mockDisplay.addUser(new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);
      expect(mockDisplay.addUser(new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"))).andReturn(mockHasManageUserSession);

      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();

      Map<SessionId, Person> people = new HashMap<SessionId, Person>();
      people.put(new SessionId("sessionId1"), new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      people.put(new SessionId("sessionId2"), new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"));
      people.put(new SessionId("sessionId3"), new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"));
      workspaceUsersPresenter.initUserList(people);

      verify(mockDisplay, mockEventBus);
   }
}
