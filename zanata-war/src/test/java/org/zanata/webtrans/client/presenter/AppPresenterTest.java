package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.PresenterRevealedEvent;
import net.customware.gwt.presenter.client.PresenterRevealedHandler;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.history.Window.Location;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.AppPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasCommand;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;

@Test(groups = { "unit-tests" })
public class AppPresenterTest
{

   private static final String TEST_PERSON_NAME = "Mister Ed";
   private static final String TEST_WORKSPACE_NAME = "Test Workspace Name";
   private static final String TEST_LOCALE_NAME = "Test Locale Name";
   private static final String TEST_WINDOW_TITLE = "Test Window Title";

   private static final String WORKSPACE_TITLE_QUERY_PARAMETER_KEY = "title";
   private static final String NO_DOCUMENTS_STRING = "No document selected";
   private static final String TEST_DOCUMENT_NAME = "test_document_name";
   private static final String TEST_DOCUMENT_PATH = "test/document/path/";
   private static final String TEST_WORKSPACE_TITLE = "test workspace title";
   private static final String SEARCH_PAGE_LABEL = "Project-wide Search and Replace";
   private static final String DOCUMENT_LIST_KEY_SHORTCUT_DESCRIPTION = "show document list";
   private static final String SHOW_EDITOR_KEY_SHORTCUT_DESCRIPTION = "show editor view";
   private static final String SHOW_PROJECT_WIDE_SEARCH_KEY_SHORTCUT_DESCRIPTION = "show project-wide search";


   private AppPresenter appPresenter;

   HasClickHandlers mockDocumentsLink;
   HasClickHandlers mockErrorNotificationBtn;
   HasClickHandlers mockSearchLink;

   Display mockDisplay;
   EventBus mockEventBus;
   History mockHistory;
   Identity mockIdentity;
   WebTransMessages mockMessages;
   Person mockPerson;


   HasCommand mockLeaveWorkspaceMenuItem;
   HasCommand mockSignoutMenuItem;
   HasCommand mockHelpMenuItem;

   KeyShortcutPresenter mockKeyShortcutPresenter;
   DocumentListPresenter mockDocumentListPresenter;
   SearchResultsPresenter mockSearchResultsPresenter;
   TranslationPresenter mockTranslationPresenter;
   NotificationPresenter mockNotificationPresenter;

   Window mockWindow;
   Location mockWindowLocation;
   WorkspaceContext mockWorkspaceContext;

   private Capture<ClickHandler> capturedDocumentLinkClickHandler;
   private Capture<ClickHandler> capturedSearchLinkClickHandler;
   private Capture<ClickHandler> capturedErrorNotificationBtnHandler;

   private Capture<DocumentSelectionEvent> capturedDocumentSelectionEvent;
   private Capture<DocumentStatsUpdatedEventHandler> capturedDocumentStatsUpdatedEventHandler;
   private Capture<String> capturedHistoryTokenString;
   private Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;
   private Capture<ProjectStatsUpdatedEventHandler> capturedProjectStatsUpdatedEventHandler;
   private Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdatedEventHandler;
   private Capture<PresenterRevealedHandler> capturedPresenterRevealedHandler;


   private Capture<Command> capturedLeaveWorkspaceLinkCommand;
   private Capture<Command> capturedSignoutLinkCommand;
   private Capture<Command> capturedHelpLinkCommand;


   private DocumentInfo testDocInfo;
   private DocumentId testDocId;
   private TranslationStats testDocStats;
   private TranslationStats emptyProjectStats;

   @BeforeClass
   public void createMocks()
   {
      mockDisplay = createMock(AppPresenter.Display.class);
      mockDocumentListPresenter = createMock(DocumentListPresenter.class);
      mockDocumentsLink = createMock(HasClickHandlers.class);
      mockErrorNotificationBtn = createMock(HasClickHandlers.class);
      mockSearchLink = createMock(HasClickHandlers.class);
      mockEventBus = createMock(EventBus.class);
      mockHistory = createMock(History.class);
      mockIdentity = createMock(Identity.class);
      mockKeyShortcutPresenter = createMock(KeyShortcutPresenter.class);
      mockLeaveWorkspaceMenuItem = createMock(HasCommand.class);
      mockMessages = createMock(WebTransMessages.class);
      mockPerson = createMock(Person.class);
      mockSearchResultsPresenter = createMock(SearchResultsPresenter.class);
      mockSignoutMenuItem = createMock(HasCommand.class);
      mockTranslationPresenter = createMock(TranslationPresenter.class);
      mockWindow = createMock(Window.class);
      mockWindowLocation = createMock(Window.Location.class);
      mockWorkspaceContext = createMock(WorkspaceContext.class);
      mockNotificationPresenter = createMock(NotificationPresenter.class);
      mockHelpMenuItem = createMock(HasCommand.class);

      capturedSearchLinkClickHandler = new Capture<ClickHandler>();
      capturedDocumentLinkClickHandler = new Capture<ClickHandler>();
      capturedErrorNotificationBtnHandler = new Capture<ClickHandler>();
      capturedDocumentSelectionEvent = new Capture<DocumentSelectionEvent>();
      capturedDocumentStatsUpdatedEventHandler = new Capture<DocumentStatsUpdatedEventHandler>();
      capturedHistoryTokenString = new Capture<String>();
      capturedHistoryValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      capturedKeyShortcuts = new Capture<KeyShortcut>();
      capturedProjectStatsUpdatedEventHandler = new Capture<ProjectStatsUpdatedEventHandler>();
      capturedWorkspaceContextUpdatedEventHandler = new Capture<WorkspaceContextUpdateEventHandler>();
      capturedPresenterRevealedHandler = new Capture<PresenterRevealedHandler>();

      capturedSignoutLinkCommand = new Capture<Command>();
      capturedLeaveWorkspaceLinkCommand = new Capture<Command>();
      capturedHelpLinkCommand = new Capture<Command>();


   }

   @BeforeMethod
   void beforeMethod()
   {
      resetAllMocks();
      resetAllCaptures();

      emptyProjectStats = new TranslationStats();
      buildTestDocumentInfo();

      setupDefaultMockExpectations();

      appPresenter = new AppPresenter(mockDisplay, mockEventBus,
            mockKeyShortcutPresenter, mockTranslationPresenter,
            mockDocumentListPresenter, mockSearchResultsPresenter,
            mockNotificationPresenter, mockIdentity, mockWorkspaceContext,
            mockMessages, mockHistory, mockWindow, mockWindowLocation);

      mockNotificationPresenter.setNotificationListener(appPresenter);
      expectLastCall().once();
   }

   // Note: unable to test 'sign out' and 'close window' links as these have
   // static method calls to Application

   // TODO test that initial history state is handled properly

   public void testPerformsRequiredActionsOnBind()
   {
      replayAllMocks();
      appPresenter.bind();
      verifyAllMocks();
   }

   public void testShowsUpdatedProjectStats()
   {
      Capture<TranslationStats> capturedTranslationStats = new Capture<TranslationStats>();
      mockDisplay.setStats(and(capture(capturedTranslationStats), isA(TranslationStats.class)));
      expectLastCall().once();

      replayAllMocks();

      appPresenter.bind();
      TranslationStats testStats = new TranslationStats(new TransUnitCount(6, 5, 4), new TransUnitWords(3, 2, 1));
      ProjectStatsUpdatedEvent event = new ProjectStatsUpdatedEvent(testStats);
      capturedProjectStatsUpdatedEventHandler.getValue().onProjectStatsRetrieved(event);

      verifyAllMocks();
      assertThat(capturedTranslationStats.getValue(), is(equalTo(testStats)));
   }

   public void testUpdateProjectStatsFromEditorView()
   {
      TranslationStats newProjectStats = new TranslationStats(new TransUnitCount(9, 9, 9), new TransUnitWords(9, 9, 9));

      expectLoadDocAndViewEditor();
      expectViewTransitionFromEditorToDoclist(newProjectStats);

      replayAllMocks();

      appPresenter.bind();
      HistoryToken token = simulateLoadDocAndViewEditor();
      // set stats to allow differentiation from doc stats
      capturedProjectStatsUpdatedEventHandler.getValue().onProjectStatsRetrieved(new ProjectStatsUpdatedEvent(newProjectStats));
      simulatePageChangeFromEditor(token, MainView.Documents);

      verifyAllMocks();
   }

   public void testHistoryTriggersDocumentSelectionEvent()
   {
      // not testing for specific values for the following in this test
      mockDisplay.setDocumentLabel(notNull(String.class), notNull(String.class));
      expectLastCall().anyTimes();
      mockDisplay.setStats(notNull(TranslationStats.class));
      expectLastCall().anyTimes();
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();

      replayAllMocks();

      appPresenter.bind();

      HistoryToken docSelectionToken = new HistoryToken();
      docSelectionToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docSelectionToken.toTokenString())
      {
      });

      assertThat("a new document path in history should trigger a document selection event with the correct id", capturedDocumentSelectionEvent.getValue().getDocumentId(), is(testDocId));

      verifyAllMocks();
   }

   public void testHistoryTriggersViewChange()
   {
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
      // avoid checking name or stats for this test
      mockDisplay.setDocumentLabel(notNull(String.class), notNull(String.class));
      expectLastCall().anyTimes();
      mockDisplay.setStats(notNull(TranslationStats.class));
      expectLastCall().anyTimes();

      replayAllMocks();

      appPresenter.bind();
      HistoryToken docInEditorToken = buildDocInEditorToken();
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docInEditorToken.toTokenString())
      {
      });

      verifyAllMocks();
   }

   public void testNoEditorWithoutValidDocument()
   {
      // return invalid document
      expect(mockDocumentListPresenter.getDocumentId(notNull(String.class))).andReturn(null).anyTimes();
      // not expecting show view editor
      replayAllMocks();

      appPresenter.bind();
      HistoryToken editorWithoutDocToken = new HistoryToken();
      simulateReturnToEditorView(editorWithoutDocToken);

      verifyAllMocks();
   }

   public void testHistoryTriggersDocumentNameStatsUpdate()
   {
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      // avoid checking for view change, tested elsewhere
      mockDisplay.showInMainView(isA(MainView.class));
      expectLastCall().anyTimes();
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(testDocStats));
      expectLastCall().once();
      replayAllMocks();

      appPresenter.bind();
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(buildDocInEditorToken().toTokenString())
      {
      });

      verifyAllMocks();
   }


   /**
    * Note: this also verifies that editor pending change is saved when changing
    * from editor to document list
    */
   public void testStatsAndNameChangeWithView()
   {
      expectLoadDocAndViewEditor();
      expectViewTransitionFromEditorToDoclist(emptyProjectStats);
      expectReturnToEditorView(testDocStats);

      replayAllMocks();
      appPresenter.bind();
      HistoryToken token = simulateLoadDocAndViewEditor();
      simulatePageChangeFromEditor(token, MainView.Documents);
      simulateReturnToEditorView(token);

      verifyAllMocks();
   }

   public void testStatsAndNameChangeForSearchPageView()
   {
      expectLoadDocAndViewEditor();
      expectViewTransitionFromEditor(MainView.Search, emptyProjectStats, SEARCH_PAGE_LABEL);
      expectReturnToEditorView(testDocStats);

      replayAllMocks();
      appPresenter.bind();
      HistoryToken token = simulateLoadDocAndViewEditor();
      simulatePageChangeFromEditor(token, MainView.Search);
      simulateReturnToEditorView(token);
   }

   public void testShowsUpdatedDocumentStats()
   {
      TranslationStats updatedStats = new TranslationStats(new TransUnitCount(9, 9, 9), new TransUnitWords(9, 9, 9));

      expectLoadDocAndViewEditor();
      mockDisplay.setStats(eq(updatedStats));
      expectLastCall().once();
      replayAllMocks();

      appPresenter.bind();
      // must be in editor to see document stats
      simulateLoadDocAndViewEditor();
      capturedDocumentStatsUpdatedEventHandler.getValue().onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(testDocId, updatedStats));

      verifyAllMocks();
   }

   public void testDoesNotShowWrongDocumentStats()
   {
      TranslationStats updatedStats = new TranslationStats(new TransUnitCount(9, 9, 9), new TransUnitWords(9, 9, 9));
      DocumentId notSelectedDocId = new DocumentId(7777L);

      expectLoadDocAndViewEditor();
      replayAllMocks();

      appPresenter.bind();
      simulateLoadDocAndViewEditor();
      capturedDocumentStatsUpdatedEventHandler.getValue().onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(notSelectedDocId, updatedStats));

      verifyAllMocks();
   }

   public void testUpdateDocumentStatsFromDoclistView()
   {
      TranslationStats updatedStats = new TranslationStats(new TransUnitCount(9, 9, 9), new TransUnitWords(9, 9, 9));

      expectLoadDocAndViewEditor();
      expectViewTransitionFromEditorToDoclist(emptyProjectStats);
      expectReturnToEditorView(updatedStats);
      replayAllMocks();

      appPresenter.bind();
      HistoryToken token = simulateLoadDocAndViewEditor();
      simulatePageChangeFromEditor(token, MainView.Documents);
      //update document stats
      capturedDocumentStatsUpdatedEventHandler.getValue().onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(testDocId, updatedStats));
      simulateReturnToEditorView(token);

      verifyAllMocks();
   }

   public void testDocumentsLinkGeneratesHistoryToken()
   {
      ClickEvent docLinkClickEvent = createMock(ClickEvent.class);

      // 1 - click doc link from default state
      expect(mockHistory.getToken()).andReturn("").once();

      // 2 - load a document in the editor
      expectLoadDocAndViewEditor();

      // 3 - click doc link to return to doclist
      HistoryToken expectedDocInEditorToken = buildDocInEditorToken();
      expect(mockHistory.getToken()).andReturn(expectedDocInEditorToken.toTokenString()).once();
      expectViewTransitionFromEditorToDoclist(emptyProjectStats);

      // 4 - click doc link to return to editor
      HistoryToken expectedDocListWithLoadedDocToken = new HistoryToken();
      expectedDocListWithLoadedDocToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      expect(mockHistory.getToken()).andReturn(expectedDocListWithLoadedDocToken.toTokenString()).once();

      // NOTE not expecting return to editor view as this test does not simulate
      // the event for the new history item

      replayAllMocks();


      appPresenter.bind();
      //discard captured tokens from bind to allow easy check for new token
      capturedHistoryTokenString.reset();

      // 1 - no doc loaded, don't generate MainView.Editor token
      capturedDocumentLinkClickHandler.getValue().onClick(docLinkClickEvent);
      assertThat(capturedHistoryTokenString.hasCaptured(), is(false));

      // 2 - load doc in editor
      HistoryToken token = simulateLoadDocAndViewEditor();

      // 3 - doc loaded in editor, return to doclist
      capturedDocumentLinkClickHandler.getValue().onClick(docLinkClickEvent);
      HistoryToken returnToDoclistToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("clicking documents link should always show doclist when editor is visible", returnToDoclistToken.getView(), is(MainView.Documents));
      assertThat("document path should be maintained when clicking documents link", returnToDoclistToken.getDocumentPath(), is(token.getDocumentPath()));

      // simulate history token event for new token
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(returnToDoclistToken.toTokenString())
      {
      });

      // 4 - doc loaded, return to editor
      capturedDocumentLinkClickHandler.getValue().onClick(docLinkClickEvent);
      HistoryToken returnToEditorToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("clicking documents link should show editor when doclist is visible and a valid document is selected", returnToEditorToken.getView(), is(MainView.Editor));
      assertThat("document path should be maintained when clicking documents link", returnToEditorToken.getDocumentPath(), is(token.getDocumentPath()));

      // NOTE not simulating history change event for newest history token

      // TODO could check that filter parameters haven't changed as well

      verifyAllMocks();
   }

   public void testSearchLinkGeneratesHistoryToken()
   {
      ClickEvent searchLinkClickEvent = createMock(ClickEvent.class);
      expect(mockHistory.getToken()).andReturn("").once();
      replayAllMocks();
      appPresenter.bind();
      //simulate click
      capturedSearchLinkClickHandler.getValue().onClick(searchLinkClickEvent);
      HistoryToken capturedToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("clicking search link should set view in history token to search", capturedToken.getView(), is(MainView.Search));
      //TODO could check that nothing else has changed in token
      verifyAllMocks();
   }

   public void testShowsHidesReadonlyLabel()
   {
      //receives readonly event, label shown
      WorkspaceContextUpdateEvent readOnlyEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(readOnlyEvent.isReadOnly()).andReturn(true).anyTimes();
      mockDisplay.setReadOnlyVisible(true);
      expectLastCall().once();
      //receives not-readonly event, label hidden
      WorkspaceContextUpdateEvent editableEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(editableEvent.isReadOnly()).andReturn(false).anyTimes();
      mockDisplay.setReadOnlyVisible(false);
      expectLastCall().once();

      replayAllMocks();
      replay(readOnlyEvent, editableEvent);

      appPresenter.bind();
      // simulate workspace readonly event
      capturedWorkspaceContextUpdatedEventHandler.getValue().onWorkspaceContextUpdated(readOnlyEvent);
      // simulate workspace editable event
      capturedWorkspaceContextUpdatedEventHandler.getValue().onWorkspaceContextUpdated(editableEvent);

      verifyAllMocks();
   }

   /**
    * generates new test doc id and doc info ready for use in tests
    */
   private void buildTestDocumentInfo()
   {
      testDocId = new DocumentId(2222L);
      TransUnitCount unitCount = new TransUnitCount(1, 2, 3);
      TransUnitWords wordCount = new TransUnitWords(4, 5, 6);
      testDocStats = new TranslationStats(unitCount, wordCount);
      testDocInfo = new DocumentInfo(testDocId, TEST_DOCUMENT_NAME, TEST_DOCUMENT_PATH, LocaleId.EN_US, testDocStats);
   }

   /**
    * @see #simulateLoadDocAndViewEditor()
    */
   private void expectLoadDocAndViewEditor()
   {
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();

      // test document name and stats should be shown
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(testDocStats));
      expectLastCall().once();

      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
   }

   /**
    * @see #expectLoadDocAndViewEditor()
    * @return history token representing the editor view and loaded document
    */
   private HistoryToken simulateLoadDocAndViewEditor()
   {
      HistoryToken docInEditorToken = buildDocInEditorToken();
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docInEditorToken.toTokenString())
      {
      });
      return docInEditorToken;
   }

   /**
    * Generate a token representing the default test document being viewed in
    * the editor
    * 
    * @return the newly generated token
    */
   private HistoryToken buildDocInEditorToken()
   {
      HistoryToken docInEditorToken = new HistoryToken();
      docInEditorToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      docInEditorToken.setView(MainView.Editor);
      return docInEditorToken;
   }


   /**
    * Sets expectations to show the documents view, update the document label,
    * show the given project stats, and save pending editor changes
    * 
    * @param projectStats the current project stats that should be displayed
    */
   private void expectViewTransitionFromEditorToDoclist(TranslationStats projectStats)
   {
      expectViewTransitionFromEditor(MainView.Documents, projectStats, NO_DOCUMENTS_STRING);
   }

   /**
    * Sets expectations to show the given view, update the document label,
    * show the given stats, and save pending editor changes.
    * 
    * @param toView the view to show
    * @param expectedStats the stats that should be displayed
    * @param expectedDocLabel the text that the document label is expected to show
    */
   private void expectViewTransitionFromEditor(MainView toView, TranslationStats expectedStats, String expectedDocLabel)
   {
      //expect return to given view
      mockDisplay.showInMainView(toView);
      expectLastCall().once();
      if (toView == MainView.Search)
      {
         mockSearchResultsPresenter.revealDisplay();
         expectLastCall().once();
      }
      else
      {
         mockSearchResultsPresenter.concealDisplay();
         expectLastCall().once();
      }
      mockDisplay.setDocumentLabel("", expectedDocLabel);
      expectLastCall().once();
      mockDisplay.setStats(eq(expectedStats));
      expectLastCall().once();
      mockTranslationPresenter.saveEditorPendingChange();
      expectLastCall().once();
   }

   /**
    * @see {@link #expectUpdateProjectStatsThenReturnToDocListView(TranslationStats)}
    */
   private void simulatePageChangeFromEditor(HistoryToken fromHistoryState, MainView toView)
   {
      fromHistoryState.setView(toView);
      //simulate page transition
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(fromHistoryState.toTokenString())
      {
      });
      if (toView == MainView.Search)
      {
         capturedPresenterRevealedHandler.getValue().onPresenterRevealed(new PresenterRevealedEvent(mockSearchResultsPresenter));
      }
   }

   /**
    * Return to the editor view (requires test document already loaded)
    * 
    * @param previousToken a token representing the state of the application
    *           before returning to the editor view
    * @see #expectReturnToEditorView(TranslationStats)
    */
   private void simulateReturnToEditorView(HistoryToken previousToken)
   {
      previousToken.setView(MainView.Editor);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(previousToken.toTokenString())
      {
      });
   }

   /**
    * @see #simulateReturnToEditorView(HistoryToken)
    * @param documentStats the stats object that has been set for the given document
    */
   private void expectReturnToEditorView(TranslationStats documentStats)
   {
      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(documentStats));
      expectLastCall().once();
   }

   private void setupDefaultMockExpectations()
   {
      expectSubPresenterBindings();
      expectHandlerRegistrations();
      expectPresenterSetupActions();
      setupMockGetterReturnValues();

      //misc and capture setup
      mockHistory.fireCurrentHistoryState();
      expectLastCall().anyTimes();
      mockHistory.newItem(capture(capturedHistoryTokenString));
      expectLastCall().anyTimes();
      mockEventBus.fireEvent(and(capture(capturedDocumentSelectionEvent), isA(DocumentSelectionEvent.class)));
      expectLastCall().anyTimes();
   }

   /**
    * Set up expectations to bind child presenters
    */
   private void expectSubPresenterBindings()
   {
      mockKeyShortcutPresenter.bind();
      expectLastCall().once();
      mockDocumentListPresenter.bind();
      expectLastCall().once();
      mockSearchResultsPresenter.bind();
      expectLastCall().once();
      mockTranslationPresenter.bind();
      expectLastCall().once();
      mockNotificationPresenter.bind();
      expectLastCall().once();

   }

   @SuppressWarnings("unchecked")
   private void expectHandlerRegistrations()
   {
      expect(mockHistory.addValueChangeHandler(and(capture(capturedHistoryValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expectClickHandlerRegistration(mockDocumentsLink, capturedDocumentLinkClickHandler);
      expectClickHandlerRegistration(mockErrorNotificationBtn, capturedErrorNotificationBtnHandler);
      expectClickHandlerRegistration(mockSearchLink, capturedSearchLinkClickHandler);

      expectEventHandlerRegistration(DocumentStatsUpdatedEvent.getType(), DocumentStatsUpdatedEventHandler.class, capturedDocumentStatsUpdatedEventHandler);
      expectEventHandlerRegistration(ProjectStatsUpdatedEvent.getType(), ProjectStatsUpdatedEventHandler.class, capturedProjectStatsUpdatedEventHandler);
      expectEventHandlerRegistration(WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdatedEventHandler);
      expectEventHandlerRegistration(PresenterRevealedEvent.getType(), PresenterRevealedHandler.class, capturedPresenterRevealedHandler);
   }

   /**
    * Expect a single handler registration on a mock object, and capture the
    * click handler in the given {@link Capture}
    * 
    * @param mockObjectToClick
    * @param captureForHandler
    */
   private void expectClickHandlerRegistration(HasClickHandlers mockObjectToClick, Capture<ClickHandler> captureForHandler)
   {
      expect(mockObjectToClick.addClickHandler(and(capture(captureForHandler), isA(ClickHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private <H extends EventHandler> void expectEventHandlerRegistration(Type<H> expectedType, Class<H> expectedClass, Capture<H> handlerCapture)
   {
      expect(mockEventBus.addHandler(eq(expectedType), and(capture(handlerCapture), isA(expectedClass)))).andReturn(createMock(HandlerRegistration.class)).once();
   }

   private void expectPresenterSetupActions()
   {
      mockWindow.setTitle(TEST_WINDOW_TITLE);
      expectLastCall().once();
      mockDisplay.setUserLabel(TEST_PERSON_NAME);
      expectLastCall().anyTimes();
      mockDisplay.setWorkspaceNameLabel(TEST_WORKSPACE_NAME, TEST_WORKSPACE_TITLE);
      expectLastCall().anyTimes();
      mockDisplay.setReadOnlyVisible(false);
      expectLastCall().once();
      // initially empty project stats
      mockDisplay.setStats(eq(emptyProjectStats));
      expectLastCall().once();

      mockDisplay.setDocumentLabel("", NO_DOCUMENTS_STRING);
      expectLastCall().once();

      mockDisplay.showInMainView(MainView.Documents);
      expectLastCall().once(); //starts on document list view

      // due to this display beginning as concealed
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();

      mockLeaveWorkspaceMenuItem.setCommand(and(capture(capturedLeaveWorkspaceLinkCommand), isA(Command.class)));
      expectLastCall().once();

      mockHelpMenuItem.setCommand(and(capture(capturedHelpLinkCommand), isA(Command.class)));
      expectLastCall().once();

      mockSignoutMenuItem.setCommand(and(capture(capturedSignoutLinkCommand), isA(Command.class)));
      expectLastCall().once();

      expect(mockKeyShortcutPresenter.registerKeyShortcut(and(capture(capturedKeyShortcuts), isA(KeyShortcut.class)))).andReturn(null).anyTimes();
   }

   private void setupMockGetterReturnValues()
   {
      expect(mockDisplay.getSignOutMenuItem()).andReturn(mockSignoutMenuItem).anyTimes();
      expect(mockDisplay.getHelpMenuItem()).andReturn(mockHelpMenuItem).anyTimes();
      expect(mockDisplay.getLeaveWorkspaceMenuItem()).andReturn(mockLeaveWorkspaceMenuItem).anyTimes();
      expect(mockDisplay.getDocumentsLink()).andReturn(mockDocumentsLink).anyTimes();
      expect(mockDisplay.getNotificationBtn()).andReturn(mockErrorNotificationBtn).anyTimes();

      expect(mockDisplay.getSearchAndReplaceLink()).andReturn(mockSearchLink).anyTimes();

      expect(mockIdentity.getPerson()).andReturn(mockPerson).anyTimes();
      expect(mockPerson.getName()).andReturn(TEST_PERSON_NAME).anyTimes();

      expect(mockWindowLocation.getParameter(WORKSPACE_TITLE_QUERY_PARAMETER_KEY)).andReturn(TEST_WORKSPACE_TITLE).anyTimes();

      expect(mockMessages.windowTitle(TEST_WORKSPACE_NAME, TEST_LOCALE_NAME)).andReturn(TEST_WINDOW_TITLE).anyTimes();
      expect(mockMessages.noDocumentSelected()).andReturn(NO_DOCUMENTS_STRING).anyTimes();
      expect(mockMessages.projectWideSearchAndReplace()).andReturn(SEARCH_PAGE_LABEL).anyTimes();
      expect(mockMessages.showDocumentListKeyShortcut()).andReturn(DOCUMENT_LIST_KEY_SHORTCUT_DESCRIPTION).anyTimes();
      expect(mockMessages.showEditorKeyShortcut()).andReturn(SHOW_EDITOR_KEY_SHORTCUT_DESCRIPTION).anyTimes();
      expect(mockMessages.showProjectWideSearch()).andReturn(SHOW_PROJECT_WIDE_SEARCH_KEY_SHORTCUT_DESCRIPTION).anyTimes();

      expect(mockWorkspaceContext.getWorkspaceName()).andReturn(TEST_WORKSPACE_NAME).anyTimes();
      expect(mockWorkspaceContext.getLocaleName()).andReturn(TEST_LOCALE_NAME).anyTimes();
      expect(mockWorkspaceContext.isReadOnly()).andReturn(false).anyTimes();

      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();
   }

   private void resetAllMocks()
   {
      reset(mockDisplay, mockDocumentListPresenter, mockDocumentsLink, mockErrorNotificationBtn);
      reset(mockEventBus, mockHistory, mockIdentity, mockKeyShortcutPresenter);
      reset(mockMessages, mockPerson, mockSearchResultsPresenter);
      reset(mockTranslationPresenter, mockWindow, mockWindowLocation, mockWorkspaceContext);
      reset(mockNotificationPresenter);

      reset(mockHelpMenuItem, mockLeaveWorkspaceMenuItem, mockSignoutMenuItem, mockSearchLink);
   }

   private void resetAllCaptures()
   {
      capturedDocumentLinkClickHandler.reset();
      capturedDocumentSelectionEvent.reset();
      capturedDocumentStatsUpdatedEventHandler.reset();
      capturedHistoryTokenString.reset();
      capturedHistoryValueChangeHandler.reset();
      capturedLeaveWorkspaceLinkCommand.reset();
      capturedHelpLinkCommand.reset();
      capturedKeyShortcuts.reset();
      capturedProjectStatsUpdatedEventHandler.reset();
      capturedSearchLinkClickHandler.reset();
      capturedSignoutLinkCommand.reset();
      capturedWorkspaceContextUpdatedEventHandler.reset();
      capturedPresenterRevealedHandler.reset();
      capturedErrorNotificationBtnHandler.reset();
   }

   private void replayAllMocks()
   {
      replay(mockDisplay, mockDocumentListPresenter, mockDocumentsLink, mockErrorNotificationBtn);
      replay(mockEventBus, mockHistory, mockIdentity, mockKeyShortcutPresenter);
      replay(mockMessages, mockPerson, mockSearchResultsPresenter);
      replay(mockTranslationPresenter, mockWindow, mockWindowLocation, mockWorkspaceContext);
      replay(mockNotificationPresenter);

      replay(mockHelpMenuItem, mockLeaveWorkspaceMenuItem, mockSignoutMenuItem, mockSearchLink);
   }

   private void verifyAllMocks()
   {
      verify(mockDisplay, mockDocumentListPresenter, mockDocumentsLink, mockErrorNotificationBtn);
      verify(mockEventBus, mockHistory, mockIdentity, mockKeyShortcutPresenter);
      verify(mockMessages, mockPerson, mockSearchResultsPresenter);
      verify(mockTranslationPresenter, mockWindow, mockWindowLocation, mockWorkspaceContext);
      verify(mockNotificationPresenter);

      verify(mockHelpMenuItem, mockLeaveWorkspaceMenuItem, mockSignoutMenuItem, mockSearchLink);
   }
}
