package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.zanata.webtrans.client.events.TranslatorStatusUpdateEvent;
import org.zanata.webtrans.client.events.TranslatorStatusUpdateEventHandler;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.TransUnit;
import com.google.common.base.Strings;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{

   private final HashMap<Person, UserPanelSessionItem> userPanelMap;

   public interface Display extends WidgetDisplay
   {
      HasManageUserSession addUser(Person person);

      void removeUser(HasManageUserSession userPanel);

      int getUserSize();
   }

   public static class UserPanelSessionItem
   {
      private HasManageUserSession panel;
      private ArrayList<String> sessionList;

      public UserPanelSessionItem(HasManageUserSession panel, ArrayList<String> sessionList)
      {
         this.panel = panel;
         this.sessionList = sessionList;
      }

      public HasManageUserSession getPanel()
      {
         return panel;
      }

      public ArrayList<String> getSessionList()
      {
         if(sessionList == null)
         {
            sessionList = new ArrayList<String>();
         }
         return sessionList;
      }
   }



   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
      userPanelMap = new HashMap<Person, UserPanelSessionItem>();
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(TranslatorStatusUpdateEvent.getType(), new TranslatorStatusUpdateEventHandler()
      {
         @Override
         public void onTranslatorStatusUpdate(TranslatorStatusUpdateEvent event)
         {
            updateTranslatorStatus(event.getSessionId(), event.getPerson(), event.getSelectedTransUnit());
         }
      }));
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

   public void updateTranslatorStatus(SessionId sessionId, Person person, TransUnit selectedTransUnit)
   {
      if (userPanelMap.containsKey(person))
      {
         UserPanelSessionItem item = userPanelMap.get(person);
         item.getPanel().updateStatusLabel(selectedTransUnit.getSources().toString());
         item.getPanel().updateStatusTitle("Resource ID: " + selectedTransUnit.getResId());
      }
   }

   public void removeTranslator(SessionId sessionId, Person person)
   {
      if (userPanelMap.containsKey(person))
      {
         UserPanelSessionItem item = userPanelMap.get(person);
         item.getSessionList().remove(sessionId.toString());

         if(item.getSessionList().size() == 1)
         {
            item.getPanel().updateTitle(sessionId.toString());
            item.getPanel().updateSessionLabel("");
         }
         else if (item.getSessionList().isEmpty())
         {
            userPanelMap.remove(person);
            display.removeUser(item.getPanel());
         }
         else
         {
            String title = "";
            for (String session : item.getSessionList())
            {
               title = Strings.isNullOrEmpty(title) ? session : title + " : " + session;
            }

            item.getPanel().updateTitle(title);
            item.getPanel().updateSessionLabel("(" + item.getSessionList().size() + ")");
         }
      }
   }

   public void addTranslator(SessionId sessionId, Person person)
   {
      if (!userPanelMap.containsKey(person))
      {
         HasManageUserSession panel = display.addUser(person);

         UserPanelSessionItem item = new UserPanelSessionItem(panel, new ArrayList<String>());
         item.getSessionList().add(sessionId.toString());
         panel.updateTitle(sessionId.toString());
         panel.updateSessionLabel("");
         userPanelMap.put(person, item);
      }
      else
      {
         UserPanelSessionItem item = userPanelMap.get(person);
         item.getSessionList().add(sessionId.toString());

         String title = "";
         for (String session : item.getSessionList())
         {
            title = Strings.isNullOrEmpty(title) ? session : title + " : " + session;
         }

         item.getPanel().updateTitle(title);
         item.getPanel().updateSessionLabel("(" + item.getSessionList().size() + ")");
      }
   }

   public int getTranslatorsSize()
   {
      return display.getUserSize();
   }

}
