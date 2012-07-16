package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;

import java.util.HashMap;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.EnterWorkspaceEvent;
import org.zanata.webtrans.client.events.EnterWorkspaceEventHandler;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.NativeEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.TranslationPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.PersonId;
import org.zanata.webtrans.shared.model.PersonSessionDetails;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslatorList;
import org.zanata.webtrans.shared.rpc.GetTranslatorListResult;
import org.zanata.webtrans.shared.rpc.HasWorkspaceChatData.MESSAGE_TYPE;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class TranslationPresenterTest
{

   private static final String TEST_USERS_ONLINE_MESSAGE = "some users online";
   private static final String TEST_HAS_JONINED_WORKSPACE_MESSAGE = "has joined workspace";
   private static final String TEST_SHOW_OPTIONS_TOOLTIP = "tooltip to show options";
   private static final String TEST_HIDE_OPTIONS_TOOLTIP = "tooltip to hide options";
   private static final String COPY_FROM_TM = "Copy from TM";

   // object under test
   private TranslationPresenter translationPresenter;

   // mock injected entities
   private CachingDispatchAsync mockDispatcher;
   private Display mockDisplay;
   private EventBus mockEventBus;
   private GlossaryPresenter mockGlossaryPresenter;
   private WebTransMessages mockMessages;
   private NativeEvent mockNativeEvent;

   // TODO use real presenters
   private OptionsPanelPresenter mockSidePanelPresenter;
   private TranslationEditorPresenter mockTranslationEditorPresenter;
   private TransMemoryPresenter mockTransMemoryPresenter;
   private UserWorkspaceContext mockUserWorkspaceContext;
   private WorkspaceUsersPresenter mockWorkspaceUsersPresenter;
   private TargetContentsPresenter mockTargetContentsPresenter;
   private KeyShortcutPresenter mockKeyShortcutPresenter;

   // mock view components
   private HasValue<Boolean> mockOptionsToggle;
   private HasValue<Boolean> mockSouthPanelToggle;
   private HasSelectionHandlers<Integer> mockSouthPanel;

   private Capture<EnterWorkspaceEventHandler> capturedEnterWorkspaceEventHandler;
   private Capture<ExitWorkspaceEventHandler> capturedExitWorkspaceEventHandler;
   private Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdateEventHandler;
   private Capture<GetTranslatorList> capturedTranslatorListRequest;
   private Capture<AsyncCallback<GetTranslatorListResult>> capturedTranslatorListRequestCallback;
   private Capture<ValueChangeHandler<Boolean>> capturedOptionsToggleValueChangeHandler;
   private Capture<ValueChangeHandler<Boolean>> capturedSouthPanelToggleValueChangeHandler;
   private Capture<SelectionHandler<Integer>> capturedSouthPanelSelectionHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;
   private Capture<PublishWorkspaceChatEventHandler> capturedPublishWorkspaceChatEventHandler;

   @SuppressWarnings("unchecked")
   @BeforeClass
   public void createMocks()
   {
      mockDispatcher = createMock(CachingDispatchAsync.class);
      mockDisplay = createMock(TranslationPresenter.Display.class);
      mockEventBus = createMock(EventBus.class);
      mockGlossaryPresenter = createMock(GlossaryPresenter.class);
      mockMessages = createMock(WebTransMessages.class);
      mockNativeEvent = createMock(NativeEvent.class);
      mockSidePanelPresenter = createMock(OptionsPanelPresenter.class);
      mockTranslationEditorPresenter = createMock(TranslationEditorPresenter.class);
      mockTransMemoryPresenter = createMock(TransMemoryPresenter.class);
      mockUserWorkspaceContext = createMock(UserWorkspaceContext.class);
      mockWorkspaceUsersPresenter = createMock(WorkspaceUsersPresenter.class);
      mockTargetContentsPresenter = createMock(TargetContentsPresenter.class);
      mockKeyShortcutPresenter = createMock(KeyShortcutPresenter.class);

      mockOptionsToggle = createMock(HasValue.class);
      mockSouthPanelToggle = createMock(HasValue.class);
      mockSouthPanel = createMock(HasSelectionHandlers.class);

      capturedEnterWorkspaceEventHandler = new Capture<EnterWorkspaceEventHandler>();
      capturedExitWorkspaceEventHandler = new Capture<ExitWorkspaceEventHandler>();
      capturedWorkspaceContextUpdateEventHandler = new Capture<WorkspaceContextUpdateEventHandler>();
      capturedTranslatorListRequest = new Capture<GetTranslatorList>();
      capturedTranslatorListRequestCallback = new Capture<AsyncCallback<GetTranslatorListResult>>();
      capturedOptionsToggleValueChangeHandler = new Capture<ValueChangeHandler<Boolean>>();
      capturedSouthPanelToggleValueChangeHandler = new Capture<ValueChangeHandler<Boolean>>();
      capturedSouthPanelSelectionHandler = new Capture<SelectionHandler<Integer>>();
      capturedKeyShortcuts = new Capture<KeyShortcut>();
      capturedPublishWorkspaceChatEventHandler = new Capture<PublishWorkspaceChatEventHandler>();
   }
   
   @BeforeMethod
   public void resetEverything()
   {
      resetAllMocks();
      resetAllCaptures();
      setupDefaultMockExpectations();

      translationPresenter = new TranslationPresenter(mockDisplay, mockEventBus, mockDispatcher, mockTargetContentsPresenter, mockWorkspaceUsersPresenter, mockTranslationEditorPresenter, mockSidePanelPresenter, mockTransMemoryPresenter, mockGlossaryPresenter, mockMessages, mockNativeEvent, mockUserWorkspaceContext, mockKeyShortcutPresenter);
   }

   @Test
   public void performsRequiredActionsOnBind()
   {
      replayAllMocks();
      translationPresenter.bind();
      verifyAllMocks();
   }

   @Test
   public void hidesOptionsPanel()
   {
      // simulate options toggle released
      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> optionsToggleDeactivated = createMock(ValueChangeEvent.class);
      expect(optionsToggleDeactivated.getValue()).andReturn(false).anyTimes();

      mockDisplay.setOptionsToggleTooltip(TEST_SHOW_OPTIONS_TOOLTIP);
      mockDisplay.setSidePanelVisible(false);

      replay(optionsToggleDeactivated);
      replayAllMocks();

      translationPresenter.bind();
      capturedOptionsToggleValueChangeHandler.getValue().onValueChange(optionsToggleDeactivated);

      verifyAllMocks();
   }

   @Test
   public void showsOptionsPanel()
   {
      // simulate options toggle depressed
      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> optionsToggleActivated = createMock(ValueChangeEvent.class);
      expect(optionsToggleActivated.getValue()).andReturn(true).anyTimes();

      // simulate options panel hidden
      mockDisplay.setOptionsToggleTooltip(TEST_HIDE_OPTIONS_TOOLTIP);
      mockDisplay.setSidePanelVisible(true);

      replay(optionsToggleActivated);
      replayAllMocks();

      translationPresenter.bind();
      capturedOptionsToggleValueChangeHandler.getValue().onValueChange(optionsToggleActivated);

      verifyAllMocks();
   }

   @Test
   public void hidesSouthPanel()
   {
      expectHideSouthPanel();
      replayAllMocks();
      translationPresenter.bind();
      simulateShowSouthPanel(false);
      verifyAllMocks();
   }

   @Test
   public void showsSouthPanel()
   {
      expectHideSouthPanel();
      expectShowSouthPanel(null);
      replayAllMocks();
      translationPresenter.bind();
      simulateShowSouthPanel(false);
      simulateShowSouthPanel(true);
      verifyAllMocks();
   }


   /**
    * similar to showsSouthPanel() but with non-null selected TU
    */
   @Test
   public void fireTMGlossarySearchOnShowSouthPanel()
   {
      expectHideSouthPanel();
      TransUnit mockTU = createMock(TransUnit.class);
      expectShowSouthPanel(mockTU);
      // these called for non-null TU
      mockTransMemoryPresenter.createTMRequestForTransUnit(mockTU);
      mockGlossaryPresenter.createGlossaryRequestForTransUnit(mockTU);
      replayAllMocks();
      translationPresenter.bind();
      simulateShowSouthPanel(false);
      simulateShowSouthPanel(true);
      verifyAllMocks();
   }

   @Test
   public void updateParticipantsOnEnterWorkspace()
   {
      int numUsersOnline = 5;
      expect(mockMessages.nUsersOnline(numUsersOnline)).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
      expect(mockMessages.hasJoinedWorkspace("bob")).andReturn(TEST_HAS_JONINED_WORKSPACE_MESSAGE).once();
      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);

      expect(mockWorkspaceUsersPresenter.getTranslatorsSize()).andReturn(numUsersOnline);
      mockWorkspaceUsersPresenter.dispatchChatAction(null, TEST_HAS_JONINED_WORKSPACE_MESSAGE, MESSAGE_TYPE.SYSTEM_MSG);
      mockWorkspaceUsersPresenter.addTranslator(new EditorClientId("sessionId1", 1), new Person(new PersonId("bob"), "Bob Smith", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"), null);

      replayAllMocks();
      translationPresenter.bind();
      simulateEnterWorkspaceEvent();
      verifyAllMocks();
   }

   @Test
   public void updateParticipantsOnExitWorkspace()
   {
      replayAllMocks();
      translationPresenter.bind();
      reset(mockDispatcher, mockDisplay, mockMessages, mockWorkspaceUsersPresenter);

      // TODO this map is not used, check why before deleting
      // expect lookup translator list
      Map<EditorClientId, Person> participants = new HashMap<EditorClientId, Person>();
      participants.put(new EditorClientId("sessionId1", 1), new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      participants.put(new EditorClientId("sessionId2", 1), new Person(new PersonId("jones"), "Jones John", "http://www.gravatar.com/avatar/jones@zanata.org?d=mm&s=16"));
      participants.put(new EditorClientId("sessionId2", 1), new Person(new PersonId("jim"), "Jim Jones", "http://www.gravatar.com/avatar/jim@zanata.org?d=mm&s=16"));

      expect(mockMessages.nUsersOnline(participants.size())).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
      expectLastCall().once(); // once for now

      mockWorkspaceUsersPresenter.removeTranslator(new EditorClientId("sessionId1", 1), new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      expectLastCall().once();

      // simulate enter workspace event
      ExitWorkspaceEvent event = createMock(ExitWorkspaceEvent.class);

      expect(event.getEditorClientId()).andReturn(new EditorClientId("sessionId1", 1));
      expect(event.getPerson()).andReturn(new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      expect(mockWorkspaceUsersPresenter.getTranslatorsSize()).andReturn(2);

      replay(mockDispatcher, mockDisplay, mockMessages, mockWorkspaceUsersPresenter, event);

      capturedExitWorkspaceEventHandler.getValue().onExitWorkspace(event);

      verify(mockDispatcher, mockDisplay, mockMessages, mockWorkspaceUsersPresenter);
   }

   @Test
   public void disablesTmOnReadOnly()
   {
      replayAllMocks();
      translationPresenter.bind();
      fireReadOnlyAndCheckResponse();
   }

   @Test
   public void enablesTmOnNotReadOnly()
   {
      replayAllMocks();
      translationPresenter.bind();
      fireReadOnlyAndCheckResponse();

      reset(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockSouthPanelToggle, mockUserWorkspaceContext);

      // re-expansion of south panel depends on toggle state (from before it was
      // hidden). Simulating contracted in this test, so no re-binding of
      // presenters is expected.
      // might be good to have presenter store this rather than keeping state in
      // view.
      expect(mockSouthPanelToggle.getValue()).andReturn(false).anyTimes();
      expect(mockDisplay.getSouthPanelToggle()).andReturn(mockSouthPanelToggle);

      mockUserWorkspaceContext.setProjectActive(true);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false);
      
      mockDisplay.setSouthPanelVisible(true);

      replay(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockSouthPanelToggle, mockUserWorkspaceContext);

      // fire readonly event
      WorkspaceContextUpdateEvent notReadOnlyEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(notReadOnlyEvent.isProjectActive()).andReturn(true).anyTimes();
      replay(notReadOnlyEvent);
      capturedWorkspaceContextUpdateEventHandler.getValue().onWorkspaceContextUpdated(notReadOnlyEvent);

      verify(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockSouthPanelToggle, mockUserWorkspaceContext);
   }

   private void expectShowSouthPanel(TransUnit selectedTransUnit)
   {
      // south panel shown
      mockDisplay.setSouthPanelExpanded(true);
      mockTransMemoryPresenter.bind();
      mockGlossaryPresenter.bind();
      mockWorkspaceUsersPresenter.bind();

      // When shown, TM will try to fire a search for currently selected TU.
      // simulating no selected TU to simplify test
      expect(mockTranslationEditorPresenter.getSelectedTransUnit()).andReturn(selectedTransUnit);
   }

   private void expectHideSouthPanel()
   {
      mockDisplay.setSouthPanelExpanded(false);
      mockTransMemoryPresenter.unbind();
      mockGlossaryPresenter.unbind();
      mockWorkspaceUsersPresenter.unbind();
   }

   private void simulateShowSouthPanel(boolean show)
   {
      // simulate south panel toggle released
      @SuppressWarnings("unchecked")
      ValueChangeEvent<Boolean> southPanelToggleEvent = createMock(ValueChangeEvent.class);
      expect(southPanelToggleEvent.getValue()).andReturn(show).anyTimes();
      replay(southPanelToggleEvent);
      capturedSouthPanelToggleValueChangeHandler.getValue().onValueChange(southPanelToggleEvent);
   }

   private void simulateEnterWorkspaceEvent()
   {
      EnterWorkspaceEvent event = createMock(EnterWorkspaceEvent.class);
      expect(event.getEditorClientId()).andReturn(new EditorClientId("sessionId1", 1));
      expect(event.getPerson()).andReturn(new Person(new PersonId("bob"), "Bob Smith", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16")).anyTimes();
      replay(event);
      capturedEnterWorkspaceEventHandler.getValue().onEnterWorkspace(event);
   }

   /**
    * Fire a mock read-only event and check that south panel is hidden and
    * presenters on south panel are unbound
    */
   private void fireReadOnlyAndCheckResponse()
   {
      reset(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockUserWorkspaceContext);

      mockDisplay.setSouthPanelExpanded(false);
      mockDisplay.setSouthPanelVisible(false);

      mockUserWorkspaceContext.setProjectActive(false);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(true);
      
      mockTransMemoryPresenter.unbind();
      mockGlossaryPresenter.unbind();
      mockWorkspaceUsersPresenter.unbind();
      

      replay(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockUserWorkspaceContext);

      // fire readonly event
      WorkspaceContextUpdateEvent readOnlyEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(readOnlyEvent.isProjectActive()).andReturn(false).anyTimes();
      replay(readOnlyEvent);
      capturedWorkspaceContextUpdateEventHandler.getValue().onWorkspaceContextUpdated(readOnlyEvent);

      verify(mockDisplay, mockTransMemoryPresenter, mockGlossaryPresenter, mockWorkspaceUsersPresenter, mockUserWorkspaceContext);
   }

   // TODO test for starting in read-only mode

   // TODO test failed participants list request (what behaviour is desired
   // here? Ignore? Clear list? Display 'unable to retrieve participants list'?)

   // TODO test key bindings

   private void setupDefaultMockExpectations()
   {
      Map<EditorClientId, PersonSessionDetails> people = new HashMap<EditorClientId, PersonSessionDetails>();
      people.put(new EditorClientId("sessionId", 1), new PersonSessionDetails(new Person(new PersonId("jones"), "Joey Jones", "http://www.gravatar.com/avatar/joey@zanata.org?d=mm&s=16"), null));

      setupDefaultMockExpectations(people);
   }

   private HandlerRegistration mockHandlerRegistration()
   {
      return createMock(HandlerRegistration.class);
   }
   
   @SuppressWarnings("unchecked")
   private void setupDefaultMockExpectations(Map<EditorClientId, PersonSessionDetails> initialParticipants)
   {
      mockTransMemoryPresenter.bind();
      expectLastCall().once();
      mockWorkspaceUsersPresenter.bind();
      expectLastCall().once();
      mockGlossaryPresenter.bind();
      expectLastCall().once();
      mockTranslationEditorPresenter.bind();
      expectLastCall().once();
      mockSidePanelPresenter.bind();
      expectLastCall().once();

      expect(mockMessages.navigateToNextRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.navigateToPreviousRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.openEditorInSelectedRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      
      expect(mockMessages.showEditorOptions()).andReturn(TEST_SHOW_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.hideEditorOptions()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();

      expect(mockKeyShortcutPresenter.register(capture(capturedKeyShortcuts))).andReturn(mockHandlerRegistration()).times(3);

      expect(mockEventBus.addHandler(eq(EnterWorkspaceEvent.getType()), and(capture(capturedEnterWorkspaceEventHandler), isA(EnterWorkspaceEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(ExitWorkspaceEvent.getType()), and(capture(capturedExitWorkspaceEventHandler), isA(ExitWorkspaceEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler(eq(WorkspaceContextUpdateEvent.getType()), and(capture(capturedWorkspaceContextUpdateEventHandler), isA(WorkspaceContextUpdateEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      setupUserListRequestResponse(initialParticipants);
      
      mockDisplay.setSouthPanelVisible(true);
      expectLastCall().once();
      
      expect(mockDisplay.getSouthTabPanel()).andReturn(mockSouthPanel).anyTimes();
      expect(mockSouthPanel.addSelectionHandler(and(capture(capturedSouthPanelSelectionHandler), isA(SelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getOptionsToggle()).andReturn(mockOptionsToggle).anyTimes();
      expect(mockOptionsToggle.addValueChangeHandler(and(capture(capturedOptionsToggleValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getSouthPanelToggle()).andReturn(mockSouthPanelToggle).anyTimes();
      expect(mockSouthPanelToggle.addValueChangeHandler(and(capture(capturedSouthPanelToggleValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockSouthPanelToggle.getValue()).andReturn(true).anyTimes();

      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false).anyTimes();

      expect(mockEventBus.addHandler(eq(PublishWorkspaceChatEvent.getType()), and(capture(capturedPublishWorkspaceChatEventHandler), isA(PublishWorkspaceChatEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   /**
    * @param participants
    */
   @SuppressWarnings("unchecked")
   private void setupUserListRequestResponse(Map<EditorClientId, PersonSessionDetails> participants)
   {
      mockDispatcher.execute(and(capture(capturedTranslatorListRequest), isA(Action.class)), and(capture(capturedTranslatorListRequestCallback), isA(AsyncCallback.class)));
      expectLastCall().andAnswer(new TranslatorListSuccessAnswer(participants)).once();

      mockWorkspaceUsersPresenter.initUserList(participants);
      expectLastCall().once(); // once for now

      expect(mockMessages.nUsersOnline(participants.size())).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
      expectLastCall().once(); // once for now
   }

   private void resetAllCaptures()
   {
      capturedEnterWorkspaceEventHandler.reset();
      capturedExitWorkspaceEventHandler.reset();
      capturedWorkspaceContextUpdateEventHandler.reset();
      capturedTranslatorListRequest.reset();
      capturedTranslatorListRequestCallback.reset();
      capturedOptionsToggleValueChangeHandler.reset();
      capturedSouthPanelToggleValueChangeHandler.reset();
      capturedSouthPanelSelectionHandler.reset();
      capturedKeyShortcuts.reset();
      capturedPublishWorkspaceChatEventHandler.reset();
   }

   private void resetAllMocks()
   {
      reset(mockDispatcher, mockDisplay, mockEventBus, mockUserWorkspaceContext, mockGlossaryPresenter);
      reset(mockMessages, mockNativeEvent, mockSidePanelPresenter, mockTranslationEditorPresenter, mockTransMemoryPresenter);
      reset(mockWorkspaceUsersPresenter, mockKeyShortcutPresenter);

      reset(mockOptionsToggle, mockSouthPanelToggle, mockSouthPanel);
   }

   private void replayAllMocks()
   {
      replay(mockDispatcher, mockDisplay, mockEventBus, mockUserWorkspaceContext, mockGlossaryPresenter);
      replay(mockMessages, mockNativeEvent, mockSidePanelPresenter, mockTranslationEditorPresenter, mockTransMemoryPresenter);
      replay(mockWorkspaceUsersPresenter, mockKeyShortcutPresenter);

      replay(mockOptionsToggle, mockSouthPanelToggle, mockSouthPanel);
   }

   private void verifyAllMocks()
   {
      verify(mockDispatcher, mockDisplay, mockEventBus, mockUserWorkspaceContext, mockGlossaryPresenter);
      verify(mockMessages, mockNativeEvent, mockSidePanelPresenter, mockTranslationEditorPresenter, mockTransMemoryPresenter);
      verify(mockWorkspaceUsersPresenter, mockKeyShortcutPresenter);

      verify(mockOptionsToggle, mockSouthPanelToggle, mockSouthPanel);
   }

   private class TranslatorListSuccessAnswer implements IAnswer<GetTranslatorListResult>
   {
      private Map<EditorClientId, PersonSessionDetails> translatorsToReturn;

      public TranslatorListSuccessAnswer(Map<EditorClientId, PersonSessionDetails> translatorsToReturn)
      {
         this.translatorsToReturn = translatorsToReturn;
      }

      @Override
      public GetTranslatorListResult answer() throws Throwable
      {
         GetTranslatorListResult result = new GetTranslatorListResult(translatorsToReturn, translatorsToReturn.size());
         capturedTranslatorListRequestCallback.getValue().onSuccess(result);
         return null;
      }
   }

   // TODO test that failed participant list request is handled appropriately

   private class TranslatorListFailAnswer implements IAnswer<GetTranslatorListResult>
   {
      @Override
      public GetTranslatorListResult answer() throws Throwable
      {
         capturedTranslatorListRequestCallback.getValue().onFailure(new Throwable("test"));
         return null;
      }
   }

}
