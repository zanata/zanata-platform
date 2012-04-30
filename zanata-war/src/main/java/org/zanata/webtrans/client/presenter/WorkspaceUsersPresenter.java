package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;

import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   public interface Display extends WidgetDisplay
   {
      void addUser(SessionId sessionId, Person person);

      void removeUser(Person person);

      int getUserSize();
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void initUserList(Map<SessionId, Person> users)
   {
      for (SessionId sessionId : users.keySet())
      {
         display.addUser(sessionId, users.get(sessionId));
      }
   }

   public void removeTranslator(SessionId sessionId, Person person)
   {
      display.removeUser(person);
   }

   public void addTranslator(SessionId sessionId, Person person)
   {
      display.addUser(sessionId, person);
   }

   public int getTranslatorsSize()
   {
      return display.getUserSize();
   }

}
