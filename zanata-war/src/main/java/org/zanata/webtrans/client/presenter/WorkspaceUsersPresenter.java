package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.TransUnitEditEvent;
import org.zanata.webtrans.client.events.TransUnitEditEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.HasManageUserSession;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.auth.SessionId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatResult;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{
   private final HashMap<Person, UserPanelSessionItem> userSessionMap;

   private final Identity identity;

   private final CachingDispatchAsync dispatcher;

   private final WebTransMessages messages;

   public interface Display extends WidgetDisplay
   {
      HasManageUserSession addUser(Person person);

      HasClickHandlers getSendButton();

      HasText getInputText();

      void appendChat(String user, String timestamp, String msg);

      void removeUser(HasManageUserSession userPanel);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus, final Identity identity, final CachingDispatchAsync dispatcher, final WebTransMessages messages)
   {
      super(display, eventBus);
      this.identity = identity;
      this.dispatcher = dispatcher;
      this.messages = messages;
      userSessionMap = new HashMap<Person, UserPanelSessionItem>();

   }

   @Override
   protected void onBind()
   {
      display.getSendButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!Strings.isNullOrEmpty(display.getInputText().getText()))
            {
               dispatchChatAction(identity.getPerson().getId().toString(), display.getInputText().getText());
               display.getInputText().setText("");
            }
         }
      });

      registerHandler(eventBus.addHandler(TransUnitEditEvent.getType(), new TransUnitEditEventHandler()
      {
         @Override
         public void onTransUnitEdit(TransUnitEditEvent event)
         {
            updateTranslatorStatus(event.getSessionId(), event.getPerson(), event.getSelectedTransUnit());
         }
      }));

      registerHandler(eventBus.addHandler(PublishWorkspaceChatEvent.getType(), new PublishWorkspaceChatEventHandler()
      {
         @Override
         public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event)
         {
            display.appendChat(event.getPersonId(), event.getTimestamp(), event.getMsg());
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

         dispatchChatAction(person.getId().toString(), messages.hasQuitWorkspace());
      }
   }

   public void dispatchChatAction(String person, String msg)
   {
      dispatcher.execute(new PublishWorkspaceChatAction(person, msg), new AsyncCallback<PublishWorkspaceChatResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
         }

         @Override
         public void onSuccess(PublishWorkspaceChatResult result)
         {
            // display.appendChat("timestamp", "dummy user", "dummy msg");
         }
      });
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
