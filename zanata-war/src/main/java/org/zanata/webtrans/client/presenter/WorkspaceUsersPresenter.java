package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.TranslatorStatusUpdateEvent;
import org.zanata.webtrans.client.events.TranslatorStatusUpdateEventHandler;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{
   private final HashMap<Person, UserPanelSessionItem> userSessionMap;

   public interface Display extends WidgetDisplay
   {
      HasManageUserSession addUser(Person person);

      void removeUser(HasManageUserSession userPanel);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus)
   {
      super(display, eventBus);
      userSessionMap = new HashMap<Person, UserPanelSessionItem>();
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

   public Map<Person, UserPanelSessionItem> getUserSessionMap()
   {
      return userSessionMap;
   }
   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void initUserList(Map<SessionId, PersonSessionDetails> users)
   {
      for (SessionId sessionId : users.keySet())
      {
         addTranslator(sessionId, users.get(sessionId).getPerson(), users.get(sessionId).getSelectedTransUnit());
      }
   }

   public void updateTranslatorStatus(SessionId sessionId, Person person, TransUnit selectedTransUnit)
   {
      if (userSessionMap.containsKey(person) && selectedTransUnit != null)
      {
         UserPanelSessionItem item = userSessionMap.get(person);
         item.setSelectedTransUnit(selectedTransUnit);
         item.getPanel().updateStatusLabel(selectedTransUnit.getSources().toString());
         item.getPanel().updateStatusTitle("Resource ID: " + selectedTransUnit.getResId());
      }
   }

   public void removeTranslator(SessionId sessionId, Person person)
   {
      if (userSessionMap.containsKey(person))
      {
         UserPanelSessionItem item = userSessionMap.get(person);
         item.getSessionList().remove(sessionId.toString());

         if (item.getSessionList().size() == 1)
         {
            item.getPanel().updateTitle(sessionId.toString());
            item.getPanel().updateSessionLabel("");
         }
         else if (item.getSessionList().isEmpty())
         {
            userSessionMap.remove(person);
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

   public void addTranslator(SessionId sessionId, Person person, TransUnit selectedTransUnit)
   {
      UserPanelSessionItem item = userSessionMap.get(person);

      if (item == null)
      {
         HasManageUserSession panel = display.addUser(person);
         item = new UserPanelSessionItem(panel, new ArrayList<String>());
         userSessionMap.put(person, item);
      }

      item.setSelectedTransUnit(selectedTransUnit);
      item.getSessionList().add(sessionId.toString());

      String sessionTitle = "";
      for (String session : item.getSessionList())
      {
         sessionTitle = Strings.isNullOrEmpty(sessionTitle) ? session : sessionTitle + " : " + session;
      }

      item.getPanel().updateTitle(sessionTitle);
      if (item.getSessionList().size() > 1)
      {
         item.getPanel().updateSessionLabel("(" + item.getSessionList().size() + ")");
      }
      else
      {
         item.getPanel().updateSessionLabel("");
      }
      updateTranslatorStatus(sessionId, person, selectedTransUnit);
   }

   public int getTranslatorsSize()
   {
      return userSessionMap.size();
   }

}
