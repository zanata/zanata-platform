package org.zanata.webtrans.client.presenter;

import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.rpc.NoOpAsyncCallback;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.client.view.WorkspaceUsersDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatResult;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class WorkspaceUsersPresenter extends WidgetPresenter<WorkspaceUsersDisplay> implements
      WorkspaceUsersDisplay.Listener, PublishWorkspaceChatEventHandler,
      ExitWorkspaceEventHandler, EnterWorkspaceEventHandler
{
   private final Identity identity;
   private final CachingDispatchAsync dispatcher;
   private final WebTransMessages messages;
   private final UserSessionService sessionService;
   private final KeyShortcutPresenter keyShortcutPresenter;

   @Inject
   public WorkspaceUsersPresenter(WorkspaceUsersDisplay display, EventBus eventBus, Identity identity, CachingDispatchAsync dispatcher, WebTransMessages messages, UserSessionService sessionService, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(display, eventBus);
      this.identity = identity;
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.sessionService = sessionService;
      this.keyShortcutPresenter = keyShortcutPresenter;
      display.setListener(this);
   }

   @Override
   protected void onBind()
   {
      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.Chat, messages.publishChatContent(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            dispatchChatAction(identity.getPerson().getId().toString(), display.getChatInputText(), MESSAGE_TYPE.USER_MSG);
         }
      }));

      registerHandler(eventBus.addHandler(PublishWorkspaceChatEvent.getType(), this));
      registerHandler(eventBus.addHandler(ExitWorkspaceEvent.getType(), this));
      registerHandler(eventBus.addHandler(EnterWorkspaceEvent.getType(), this));

      display.appendChat(null, null, messages.thisIsAPublicChannel(), MESSAGE_TYPE.SYSTEM_WARNING);
   }

   @Override
   public void onEnterWorkspace(EnterWorkspaceEvent event)
   {
      addTranslator(event.getEditorClientId(), event.getPerson(), null);
      dispatchChatAction(null, messages.hasJoinedWorkspace(event.getPerson().getId().toString()), MESSAGE_TYPE.SYSTEM_MSG);
   }

   @Override
   public void onExitWorkspace(ExitWorkspaceEvent event)
   {
      EditorClientId editorClientId = event.getEditorClientId();
      UserPanelSessionItem item = sessionService.getUserPanel(editorClientId);
      sessionService.removeUser(editorClientId);

      display.removeUser(item.getPanel());

      dispatchChatAction(null, messages.hasQuitWorkspace(event.getPerson().getId().toString()), MESSAGE_TYPE.SYSTEM_MSG);
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

   protected void dispatchChatAction(String person, String msg, MESSAGE_TYPE messageType)
   {
      if (!Strings.isNullOrEmpty(msg))
      {
         dispatcher.execute(new PublishWorkspaceChatAction(person, msg, messageType), new NoOpAsyncCallback<PublishWorkspaceChatResult>());
         display.setChatInputText("");
      }
   }

   private void addTranslator(EditorClientId editorClientId, Person person, TransUnit selectedTransUnit)
   {
      String color = sessionService.getColor(editorClientId);

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

   @Override
   public void onSendButtonClicked()
   {
      dispatchChatAction(identity.getPerson().getId().toString(), display.getChatInputText(), MESSAGE_TYPE.USER_MSG);
   }

   @Override
   public void onChatInputFocused()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Chat, true);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
   }

   @Override
   public void onChatInputBlur()
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.Chat, false);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
   }

   @Override
   public void onPublishWorkspaceChat(PublishWorkspaceChatEvent event)
   {
      display.appendChat(event.getPersonId(), event.getTimestamp(), event.getMsg(), event.getMessageType());
   }
}
