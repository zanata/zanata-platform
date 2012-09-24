package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserSessionService;
import org.zanata.webtrans.client.ui.HasManageUserPanel;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.UserPanelSessionItem;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatAction;
import org.zanata.webtrans.shared.rpc.PublishWorkspaceChatResult;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;

@Test(groups = { "unit-tests" })
public class WorkspaceUsersPresenterTest extends PresenterTest
{
   private final static String PUBLIC_CHANNEL_WARN = "Warning! This is a public channel";

   // object under test
   WorkspaceUsersPresenter workspaceUsersPresenter;

   // injected mocks
   Display mockDisplay;
   EventBus mockEventBus;

   Identity mockIdentity;
   CachingDispatchAsync mockDispatcher;
   HasClickHandlers mockSendButton;
   WebTransMessages mockMessages;
   UserSessionService mockSessionService;
   KeyShortcutPresenter mockKeyShortcutPresenter;

   HasAllFocusHandlers mockFocusTextBox;

   private Capture<ClickHandler> capturedSendButtonClickHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;
   private Capture<FocusHandler> capturedFocusHandler;
   private Capture<BlurHandler> capturedBlurHandler;
   private Capture<PublishWorkspaceChatAction> capturedDispatchedChat;
   private Capture<AsyncCallback<PublishWorkspaceChatResult>> capturedDispatchedChatCallback;


   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createAndAddMock(Display.class);
      mockEventBus = createAndAddMock(EventBus.class);

      mockIdentity = createAndAddMock(Identity.class);
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockSendButton = createAndAddMock(HasClickHandlers.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockSessionService = createAndAddMock(UserSessionService.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      mockFocusTextBox = createAndAddMock(HasAllFocusHandlers.class);

      capturedSendButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
      capturedFocusHandler = addCapture(new Capture<FocusHandler>());
      capturedBlurHandler = addCapture(new Capture<BlurHandler>());
      capturedDispatchedChat = addCapture(new Capture<PublishWorkspaceChatAction>());
      capturedDispatchedChatCallback = addCapture(new Capture<AsyncCallback<PublishWorkspaceChatResult>>());
   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAll();
      workspaceUsersPresenter = new WorkspaceUsersPresenter(mockDisplay, mockEventBus, mockIdentity, mockDispatcher, mockMessages, mockSessionService, mockKeyShortcutPresenter);
   }

   public void testChat()
   {
      HasText mockHasText = createAndAddMock(HasText.class);
      expect(mockHasText.getText()).andReturn("Test chat message").anyTimes();
      
      mockHasText.setText("");
      expectLastCall().once();
      
      expect(mockDisplay.getInputText()).andReturn(mockHasText).anyTimes();
      
      Person person = new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16");
      expect(mockIdentity.getPerson()).andReturn(person);
      
      IAnswer<PublishWorkspaceChatResult> chatResponse = new IAnswer<PublishWorkspaceChatResult>()
      {
         @Override
         public PublishWorkspaceChatResult answer() throws Throwable
         {
            capturedDispatchedChatCallback.getValue().onSuccess(new PublishWorkspaceChatResult());
            return null;
         }
      };
      
      mockDispatcher.execute(and(capture(capturedDispatchedChat), isA(Action.class)), and(capture(capturedDispatchedChatCallback), isA(AsyncCallback.class)));
      expectLastCall().andAnswer(chatResponse);
      
      replayAllMocks();
      
      //simulate click 'send' button
      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<EditorClientId, PersonSessionDetails>());
      
      ClickEvent clickEvent = createMock(ClickEvent.class);
      capturedSendButtonClickHandler.getValue().onClick(clickEvent);
      
      verifyAllMocks();
   }
   
   public void setEmptyUserList()
   {
      replayAllMocks();
      workspaceUsersPresenter.bind();
      workspaceUsersPresenter.initUserList(new HashMap<EditorClientId, PersonSessionDetails>());
      verifyAllMocks();
   }

   public void setNonEmptyUserList()
   {
      Person person1 = new Person(new PersonId("person1"), "John Smith", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16");
      Person person2 = new Person(new PersonId("person2"), "Smith John", "http://www.gravatar.com/avatar/smith@zanata.org?d=mm&s=16");
      Person person3 = new Person(new PersonId("person3"), "Smohn Jith", "http://www.gravatar.com/avatar/smohn@zanata.org?d=mm&s=16");

      EditorClientId editorClientId1 = new EditorClientId("sessionId1", 1);
      EditorClientId editorClientId2 = new EditorClientId("sessionId2", 1);
      EditorClientId editorClientId3 = new EditorClientId("sessionId3", 1);

      HasManageUserPanel mockPanel1 = createMock(HasManageUserPanel.class);
      HasManageUserPanel mockPanel2 = createMock(HasManageUserPanel.class);
      HasManageUserPanel mockPanel3 = createMock(HasManageUserPanel.class);

      UserPanelSessionItem mockItem1 = new UserPanelSessionItem(mockPanel1, person1);
      UserPanelSessionItem mockItem2 = new UserPanelSessionItem(mockPanel2, person2);
      UserPanelSessionItem mockItem3 = new UserPanelSessionItem(mockPanel3, person3);

      expect(mockSessionService.getColor(editorClientId1)).andReturn("color1");
      expect(mockSessionService.getColor(editorClientId2)).andReturn("color2");
      expect(mockSessionService.getColor(editorClientId3)).andReturn("color3");

      expect(mockSessionService.getUserPanel(editorClientId1)).andReturn(mockItem1);
      expect(mockSessionService.getUserPanel(editorClientId2)).andReturn(mockItem2);
      expect(mockSessionService.getUserPanel(editorClientId3)).andReturn(mockItem3);

      mockSessionService.updateTranslatorStatus(editorClientId1, null);
      expectLastCall().once();

      mockSessionService.updateTranslatorStatus(editorClientId2, null);
      expectLastCall().once();

      mockSessionService.updateTranslatorStatus(editorClientId3, null);
      expectLastCall().once();

      replayAllMocks();
      workspaceUsersPresenter.bind();

      Map<EditorClientId, PersonSessionDetails> people = new HashMap<EditorClientId, PersonSessionDetails>();
      people.put(editorClientId1, new PersonSessionDetails(person1, null));
      people.put(editorClientId2, new PersonSessionDetails(person2, null));
      people.put(editorClientId3, new PersonSessionDetails(person3, null));
      workspaceUsersPresenter.initUserList(people);

      verifyAllMocks();
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), isA(PublishWorkspaceChatEventHandler.class))).andReturn(createMock(HandlerRegistration.class));

      expect(mockDisplay.getSendButton()).andReturn(mockSendButton);
      expect(mockSendButton.addClickHandler(capture(capturedSendButtonClickHandler))).andReturn(createMock(HandlerRegistration.class));

      expect(mockMessages.thisIsAPublicChannel()).andReturn(PUBLIC_CHANNEL_WARN);
      expect(mockMessages.searchGlossary()).andReturn("Glossary");

      mockDisplay.appendChat(null, null, PUBLIC_CHANNEL_WARN, MESSAGE_TYPE.SYSTEM_WARNING);
      expectLastCall().once();

      expect(mockKeyShortcutPresenter.register(and(capture(capturedKeyShortcuts), isA(KeyShortcut.class)))).andReturn(null).once();

      expect(mockDisplay.getFocusInputText()).andReturn(mockFocusTextBox).times(2);
      expect(mockFocusTextBox.addFocusHandler(capture(capturedFocusHandler))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockFocusTextBox.addBlurHandler(capture(capturedBlurHandler))).andReturn(createMock(HandlerRegistration.class)).once();

   }
}
