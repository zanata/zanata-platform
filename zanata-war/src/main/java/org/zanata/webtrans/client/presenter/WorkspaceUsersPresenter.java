package org.zanata.webtrans.client.presenter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;

import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   private final HashMap<Person, HasManageUserSession> userPanelMap;

   public interface Display extends WidgetDisplay
   {
      HasManageUserSession addUser(SessionId sessionId, Person person);

      void removeUser(HasManageUserSession userPanel);

      int getUserSize();
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
      userPanelMap = new HashMap<Person, HasManageUserSession>();
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
         addTranslator(sessionId, users.get(sessionId));
      }
   }

   public void removeTranslator(SessionId sessionId, Person person)
   {
      if (userPanelMap.containsKey(person))
      {
         HasManageUserSession panel = userPanelMap.get(person);

         panel.removeSession(sessionId.toString());
         if (panel.isEmptySession())
         {
            userPanelMap.remove(person);
            display.removeUser(panel);
         }
      }
   }

   public void addTranslator(SessionId sessionId, Person person)
   {
      if (!userPanelMap.containsKey(person))
      {
         HasManageUserSession panel = display.addUser(sessionId, person);
         userPanelMap.put(person, panel);
      }
      else
      {
         HasManageUserSession panel = userPanelMap.get(person);
         panel.addSession(sessionId.toString());
      }
   }

   public int getTranslatorsSize()
   {
      return display.getUserSize();
   }

}
