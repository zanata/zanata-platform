package org.zanata.webtrans.client.presenter;

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatResult;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersPresenter.Display>
{
   // private final HashMap<Person, UserPanelSessionItem> userSessionMap;

   private final Identity identity;

   private final CachingDispatchAsync dispatcher;

   private final WebTransMessages messages;

   private final UserSessionService sessionService;

   public interface Display extends WidgetDisplay
   {
      HasManageUserPanel addUser(Person person);

      HasClickHandlers getSendButton();

      HasText getInputText();

      void appendChat(String user, String timestamp, String msg);

      void removeUser(HasManageUserPanel userPanel);
   }

   @Inject
   public WorkspaceUsersPresenter(final Display display, final EventBus eventBus, final Identity identity, final CachingDispatchAsync dispatcher, final WebTransMessages messages, final UserSessionService sessionService)
   {
      super(display, eventBus);
      this.identity = identity;
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.sessionService = sessionService;
   }

   @Override
   protected void onBind()
   {
      /**
       * <!-- disabled chat room until 1.7 -->
       * display.getSendButton().addClickHandler(new ClickHandler() {
       * 
       * @Override public void onClick(ClickEvent event) { if
       *           (!Strings.isNullOrEmpty(display.getInputText().getText())) {
       *           dispatchChatAction(identity.getPerson().getId().toString(),
       *           display.getInputText().getText());
       *           display.getInputText().setText(""); } } });
       **/

      registerHandler(eventBus.addHandler(PublishWorkspaceChatEvent.getType(), new PublishWorkspaceChatEventHandler()
      {
         @Override
         public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event)
         {
            display.appendChat(event.getPersonId(), event.getTimestamp(), event.getMsg());
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

   public void initUserList(Map<EditorClientId, PersonSessionDetails> users)
   {
      for (EditorClientId editorClientId : users.keySet())
      {
         addTranslator(editorClientId, users.get(editorClientId).getPerson(), users.get(editorClientId).getSelectedTransUnit());
      }
   }

   public void removeTranslator(EditorClientId editorClientId, Person person)
   {
      UserPanelSessionItem item = sessionService.getUserPanel(editorClientId);
      sessionService.removeUser(editorClientId);

      display.removeUser(item.getPanel());

      dispatchChatAction(person.getId().toString(), messages.hasQuitWorkspace());
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
         }
      });

   }

   public void addTranslator(EditorClientId editorClientId, Person person, TransUnit selectedTransUnit)
   {
      String color = sessionService.getColor(editorClientId.getValue());

      UserPanelSessionItem item = sessionService.getUserPanel(editorClientId);
      if (item == null)
      {
         HasManageUserPanel panel = display.addUser(person);
         item = new UserPanelSessionItem(panel, person);
         sessionService.addUser(editorClientId, item);
      }

      item.setSelectedTransUnit(selectedTransUnit);

      item.getPanel().setColor(color);

      sessionService.updateTranslatorStatus(editorClientId, selectedTransUnit);
   }

   public int getTranslatorsSize()
   {
      return sessionService.getTranslatorsSize();
   }

}
