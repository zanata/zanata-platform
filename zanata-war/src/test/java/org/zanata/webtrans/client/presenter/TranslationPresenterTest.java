package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.ExitWorkspaceEvent;
import org.zanata.webtrans.client.events.ExitWorkspaceEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.TranslationPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.event.logical.shared.ValueChangeEvent;

@Test(groups = { "unit-tests" })
public class TranslationPresenterTest extends PresenterTest
{

   // object under test
   private TranslationPresenter translationPresenter;

   // mock injected entities
   private Display mockDisplay;
   private EventBus mockEventBus;
   private GlossaryPresenter mockGlossaryPresenter;
   private WebTransMessages mockMessages;

   // TODO use real presenters
   private TranslationEditorPresenter mockTranslationEditorPresenter;
   private TransMemoryPresenter mockTransMemoryPresenter;
   private UserWorkspaceContext mockUserWorkspaceContext;
   private TargetContentsPresenter mockTargetContentsPresenter;
   private KeyShortcutPresenter mockKeyShortcutPresenter;
   private NavigationService navigationService;

   // mock view components
   private Capture<ExitWorkspaceEventHandler> capturedExitWorkspaceEventHandler;
   private Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdateEventHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;

   @SuppressWarnings("unchecked")
   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createAndAddMock(TranslationPresenter.Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockGlossaryPresenter = createAndAddMock(GlossaryPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);

      mockTranslationEditorPresenter = createAndAddMock(TranslationEditorPresenter.class);
      mockTransMemoryPresenter = createAndAddMock(TransMemoryPresenter.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockTargetContentsPresenter = createAndAddMock(TargetContentsPresenter.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      navigationService = createAndAddMock(NavigationService.class);

      capturedExitWorkspaceEventHandler = addCapture(new Capture<ExitWorkspaceEventHandler>());
      capturedWorkspaceContextUpdateEventHandler = addCapture(new Capture<WorkspaceContextUpdateEventHandler>());
      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
   }
   
   @BeforeMethod
   public void resetEverything()
   {
      resetAll();
      translationPresenter = new TranslationPresenter(mockDisplay, mockEventBus, mockTargetContentsPresenter, mockTranslationEditorPresenter, mockTransMemoryPresenter, mockGlossaryPresenter, mockMessages, mockUserWorkspaceContext, mockKeyShortcutPresenter, navigationService);
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

      replay(optionsToggleDeactivated);
      replayAllMocks();

      translationPresenter.bind();

      verifyAllMocks();
   }

   @Test
   public void hidesSouthPanel()
   {
      expectHideSouthPanel();
      replayAllMocks();
      translationPresenter.bind();
      translationPresenter.setSouthPanelExpanded(false);
      verifyAllMocks();
   }

   @Test
   public void showsSouthPanel()
   {
      expectHideSouthPanel();
      expectShowSouthPanel(null);
      replayAllMocks();
      translationPresenter.bind();
      translationPresenter.setSouthPanelExpanded(false);
      translationPresenter.setSouthPanelExpanded(true);
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
      translationPresenter.setSouthPanelExpanded(false);
      translationPresenter.setSouthPanelExpanded(true);
      verifyAllMocks();
   }

//   @Test
//   public void updateParticipantsOnEnterWorkspace()
//   {
//      int numUsersOnline = 5;
//      expect(mockMessages.nUsersOnline(numUsersOnline)).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
//      expect(mockMessages.hasJoinedWorkspace("bob")).andReturn(TEST_HAS_JONINED_WORKSPACE_MESSAGE).once();
//      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
//
//      expect(mockWorkspaceUsersPresenter.getTranslatorsSize()).andReturn(numUsersOnline);
//      mockWorkspaceUsersPresenter.dispatchChatAction(null, TEST_HAS_JONINED_WORKSPACE_MESSAGE, MESSAGE_TYPE.SYSTEM_MSG);
//      mockWorkspaceUsersPresenter.addTranslator(new EditorClientId("sessionId1", 1), new Person(new PersonId("bob"), "Bob Smith", "http://www.gravatar.com/avatar/bob@zanata.org?d=mm&s=16"), null);
//
//      replayAllMocks();
//      translationPresenter.bind();
//      simulateEnterWorkspaceEvent();
//      verifyAllMocks();
//   }
//
//   @Test
//   public void updateParticipantsOnExitWorkspace()
//   {
//      int numUsersOnline = 2;
//      expect(mockMessages.nUsersOnline(numUsersOnline)).andReturn(TEST_USERS_ONLINE_MESSAGE).anyTimes();
//      mockDisplay.setParticipantsTitle(TEST_USERS_ONLINE_MESSAGE);
//      mockWorkspaceUsersPresenter.removeTranslator(new EditorClientId("sessionId1", 1), new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
//      expect(mockWorkspaceUsersPresenter.getTranslatorsSize()).andReturn(2);
//      mockTargetContentsPresenter.updateTranslators();
//
//      replayAllMocks();
//      translationPresenter.bind();
//
//      ExitWorkspaceEvent event = createMock(ExitWorkspaceEvent.class);
//      expect(event.getEditorClientId()).andReturn(new EditorClientId("sessionId1", 1));
//      expect(event.getPerson()).andReturn(new Person(new PersonId("john"), "John Jones", "http://www.gravatar.com/avatar/john@zanata.org?d=mm&s=16"));
//      replay(event);
//      capturedExitWorkspaceEventHandler.getValue().onExitWorkspace(event);
//
//      verifyAllMocks();
//   }

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
      mockUserWorkspaceContext.setProjectActive(true);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false);

      expectShowSouthPanel(null);
      
      replayAllMocks();
      translationPresenter.bind();
      simulateReadOnlyEvent(true);
      simulateReadOnlyEvent(false);
      verifyAllMocks();
   }

   private void expectSetReadOnly()
   {
      mockDisplay.setSouthPanelExpanded(false);
      mockUserWorkspaceContext.setProjectActive(false);
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(true);
      mockTransMemoryPresenter.unbind();
      mockGlossaryPresenter.unbind();
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

      // When shown, TM will try to fire a search for currently selected TU.
      expect(navigationService.getSelectedOrNull()).andReturn(selectedTransUnit);
   }

   private void expectHideSouthPanel()
   {
      mockDisplay.setSouthPanelExpanded(false);
      mockTransMemoryPresenter.unbind();
      mockGlossaryPresenter.unbind();
   }

   // TODO test for starting in read-only mode

   // TODO test failed participants list request (what behaviour is desired
   // here? Ignore? Clear list? Display 'unable to retrieve participants list'?)

   // TODO test key shortcuts

   @SuppressWarnings("unchecked")
   protected void setDefaultBindExpectations() {
      mockTransMemoryPresenter.bind();
      mockGlossaryPresenter.bind();
      mockTranslationEditorPresenter.bind();
      
      expect(mockMessages.navigateToNextRow()).andReturn("Next Row").anyTimes();
      expect(mockMessages.navigateToPreviousRow()).andReturn("Prev Row").anyTimes();
      expect(mockMessages.openEditorInSelectedRow()).andReturn("Open Editor").anyTimes();
      
      
      expect(mockKeyShortcutPresenter.register(capture(capturedKeyShortcuts))).andReturn(mockHandlerRegistration()).times(3);
      
      expectEventHandlerRegistration(mockEventBus, ExitWorkspaceEvent.getType(), ExitWorkspaceEventHandler.class, capturedExitWorkspaceEventHandler);
      expectEventHandlerRegistration(mockEventBus, WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdateEventHandler);

      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false);
   }

}
