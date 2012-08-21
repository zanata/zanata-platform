package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;

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
import org.zanata.webtrans.client.events.PublishWorkspaceChatEvent;
import org.zanata.webtrans.client.events.PublishWorkspaceChatEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.TranslationPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.NavigationController;
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
public class TranslationPresenterTest extends PresenterTest
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

   // TODO use real presenters
   private OptionsPanelPresenter mockSidePanelPresenter;
   private TranslationEditorPresenter mockTranslationEditorPresenter;
   private TransMemoryPresenter mockTransMemoryPresenter;
   private UserWorkspaceContext mockUserWorkspaceContext;
   private WorkspaceUsersPresenter mockWorkspaceUsersPresenter;
   private TargetContentsPresenter mockTargetContentsPresenter;
   private KeyShortcutPresenter mockKeyShortcutPresenter;
   private NavigationController navigationController;

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
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockDisplay = createAndAddMock(TranslationPresenter.Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockGlossaryPresenter = createAndAddMock(GlossaryPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockSidePanelPresenter = createAndAddMock(OptionsPanelPresenter.class);
      mockTranslationEditorPresenter = createAndAddMock(TranslationEditorPresenter.class);
      mockTransMemoryPresenter = createAndAddMock(TransMemoryPresenter.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockWorkspaceUsersPresenter = createAndAddMock(WorkspaceUsersPresenter.class);
      mockTargetContentsPresenter = createAndAddMock(TargetContentsPresenter.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      navigationController = createAndAddMock(NavigationController.class);

      mockOptionsToggle = createAndAddMock(HasValue.class);
      mockSouthPanelToggle = createAndAddMock(HasValue.class);
      mockSouthPanel = createAndAddMock(HasSelectionHandlers.class);

      capturedEnterWorkspaceEventHandler = addCapture(new Capture<EnterWorkspaceEventHandler>());
      capturedExitWorkspaceEventHandler = addCapture(new Capture<ExitWorkspaceEventHandler>());
      capturedWorkspaceContextUpdateEventHandler = addCapture(new Capture<WorkspaceContextUpdateEventHandler>());
      capturedTranslatorListRequest = addCapture(new Capture<GetTranslatorList>());
      capturedTranslatorListRequestCallback = addCapture(new Capture<AsyncCallback<GetTranslatorListResult>>());
      capturedOptionsToggleValueChangeHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedSouthPanelToggleValueChangeHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedSouthPanelSelectionHandler = addCapture(new Capture<SelectionHandler<Integer>>());
      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
      capturedPublishWorkspaceChatEventHandler = addCapture(new Capture<PublishWorkspaceChatEventHandler>());
   }
   
   @BeforeMethod
   public void resetEverything()
   {
      resetAll();
      translationPresenter = new TranslationPresenter(mockDisplay, mockEventBus, mockDispatcher, mockTargetContentsPresenter, mockWorkspaceUsersPresenter, mockTranslationEditorPresenter, mockSidePanelPresenter, mockTransMemoryPresenter, mockGlossaryPresenter, mockMessages, mockUserWorkspaceContext, mockKeyShortcutPresenter, navigationController);
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
      int numUsersOnline = 2;
      expect(mockMessages.nUsersOnline(numUsersOnline)).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
      mockWorkspaceUsersPresenter.removeTranslator(new EditorClientId("sessionId1", 1), new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      expect(mockWorkspaceUsersPresenter.getTranslatorsSize()).andReturn(2);
      mockTargetContentsPresenter.updateTranslators();

      replayAllMocks();
      translationPresenter.bind();

      ExitWorkspaceEvent event = createMock(ExitWorkspaceEvent.class);
      expect(event.getEditorClientId()).andReturn(new EditorClientId("sessionId1", 1));
      expect(event.getPerson()).andReturn(new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
      replay(event);
      capturedExitWorkspaceEventHandler.getValue().onExitWorkspace(event);

      verifyAllMocks();
   }

   @Test
   public void disablesTmOnReadOnly()
   {
      expectSetReadOnly();
      replayAllMocks();
      translationPresenter.bind();
      boolean readOnly = true;
      simulateReadOnlyEvent(readOnly);
      verifyAllMocks();
   }

   @Test
   public void enablesTmOnNotReadOnly()
   {
      expectSetReadOnly();

      // re-expansion of south panel depends on toggle state (from before it was
      // hidden). Simulating contracted in this test, so no re-binding of
      // presenters is expected.
      // might be good to have presenter store this rather than keeping state in
      // view.
      expect(mockSouthPanelToggle.getValue()).andReturn(false);
      mockUserWorkspaceContext.setProjectActive(true);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false);
      mockDisplay.setSouthPanelVisible(true);

      replayAllMocks();
      translationPresenter.bind();
      simulateReadOnlyEvent(true);
      simulateReadOnlyEvent(false);
      verifyAllMocks();
   }

   private void expectSetReadOnly()
   {
      mockDisplay.setSouthPanelExpanded(false);
      mockDisplay.setSouthPanelVisible(false);
      mockUserWorkspaceContext.setProjectActive(false);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(true);
      mockTransMemoryPresenter.unbind();
      mockGlossaryPresenter.unbind();
      mockWorkspaceUsersPresenter.unbind();
   }

   private void simulateReadOnlyEvent(boolean readOnly)
   {
      WorkspaceContextUpdateEvent readOnlyEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(readOnlyEvent.isProjectActive()).andReturn(!readOnly).anyTimes();
      replay(readOnlyEvent);
      capturedWorkspaceContextUpdateEventHandler.getValue().onWorkspaceContextUpdated(readOnlyEvent);
   }

   private void expectShowSouthPanel(TransUnit selectedTransUnit)
   {
      // south panel shown
      mockDisplay.setSouthPanelExpanded(true);
      mockTransMemoryPresenter.bind();
      mockGlossaryPresenter.bind();
      mockWorkspaceUsersPresenter.bind();

      // When shown, TM will try to fire a search for currently selected TU.
      expect(navigationController.getSelectedOrNull()).andReturn(selectedTransUnit);
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

   // TODO test for starting in read-only mode

   // TODO test failed participants list request (what behaviour is desired
   // here? Ignore? Clear list? Display 'unable to retrieve participants list'?)

   // TODO test key shortcuts

   @SuppressWarnings("unchecked")
   protected void setDefaultBindExpectations() {
      mockTransMemoryPresenter.bind();
      mockWorkspaceUsersPresenter.bind();
      mockGlossaryPresenter.bind();
      mockTranslationEditorPresenter.bind();
      mockSidePanelPresenter.bind();
      
      expect(mockMessages.navigateToNextRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.navigateToPreviousRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.openEditorInSelectedRow()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();
      
      expect(mockMessages.showOptions()).andReturn(TEST_SHOW_OPTIONS_TOOLTIP).anyTimes();
      expect(mockMessages.hideOptions()).andReturn(TEST_HIDE_OPTIONS_TOOLTIP).anyTimes();

      expect(mockKeyShortcutPresenter.register(capture(capturedKeyShortcuts))).andReturn(mockHandlerRegistration()).times(3);
      
      expectEventHandlerRegistration(mockEventBus, EnterWorkspaceEvent.getType(), EnterWorkspaceEventHandler.class, capturedEnterWorkspaceEventHandler);
      expectEventHandlerRegistration(mockEventBus, ExitWorkspaceEvent.getType(), ExitWorkspaceEventHandler.class, capturedExitWorkspaceEventHandler);
      expectEventHandlerRegistration(mockEventBus, WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdateEventHandler);
      expectEventHandlerRegistration(mockEventBus, PublishWorkspaceChatEvent.getType(), PublishWorkspaceChatEventHandler.class, capturedPublishWorkspaceChatEventHandler);

      setupUserListRequestResponse();

      mockDisplay.setSouthPanelVisible(true);

      expect(mockDisplay.getSouthTabPanel()).andReturn(mockSouthPanel).anyTimes();
      expect(mockSouthPanel.addSelectionHandler(and(capture(capturedSouthPanelSelectionHandler), isA(SelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockDisplay.getOptionsToggle()).andReturn(mockOptionsToggle).anyTimes();
      expectValueChangeHandlerRegistration(mockOptionsToggle, capturedOptionsToggleValueChangeHandler);
      
      expect(mockDisplay.getSouthPanelToggle()).andReturn(mockSouthPanelToggle).anyTimes();
      expectValueChangeHandlerRegistration(mockSouthPanelToggle, capturedSouthPanelToggleValueChangeHandler);
      expect(mockSouthPanelToggle.getValue()).andReturn(true);
      
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false);

      mockOptionsToggle.setValue(false, true);
      expectLastCall().once();
   }

   @SuppressWarnings("unchecked")
   private void setupUserListRequestResponse()
   {
      Map<EditorClientId, PersonSessionDetails> participants = new HashMap<EditorClientId, PersonSessionDetails>();
      participants.put(new EditorClientId("sessionId", 1), new PersonSessionDetails(new Person(new PersonId("jones"), "Joey Jones", "http://www.gravatar.com/avatar/joey@zanata.org?d=mm&s=16"), null));

      mockDispatcher.execute(and(capture(capturedTranslatorListRequest), isA(Action.class)), and(capture(capturedTranslatorListRequestCallback), isA(AsyncCallback.class)));
      expectLastCall().andAnswer(new TranslatorListSuccessAnswer(participants)).once();

      mockWorkspaceUsersPresenter.initUserList(participants);

      expect(mockMessages.nUsersOnline(participants.size())).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
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
