package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.createMock;
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
      expectLastCall().once();

      replay(mockDisplay, mockEventBus);

      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<SessionId, Person>());

      verify(mockDisplay, mockEventBus);
   }

   public void setNonEmptyUserList()
   {
      expectLastCall().once();
      mockDisplay.addUser(new SessionId("sessionId1"), new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      expectLastCall().once();
      mockDisplay.addUser(new SessionId("sessionId1"), new Person(new PersonId("Smith John"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16"));
      expectLastCall().once();
      mockDisplay.addUser(new SessionId("sessionId1"), new Person(new PersonId("Smohn Jith"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16"));
      expectLastCall().once();

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
