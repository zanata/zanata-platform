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
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.ShowSideMenuEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.history.Window.Location;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.AppPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

@Test(groups = { "unit-tests" })
public class AppPresenterTest extends PresenterTest
{

   private static final String TEST_PERSON_NAME = "Mister Ed";
   private static final String TEST_WORKSPACE_NAME = "Test Workspace Name";
   private static final String TEST_LOCALE_NAME = "Test Locale Name";
   private static final String TEST_WINDOW_TITLE = "Test Window Title";
   private static final String TEST_PPROJET_SLUG = "sample-project";
   private static final String TEST_LOCALE_ID = "en-us";
   private static final String TEST_ITERATION_SLUG = "iteration-slug";

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
   HasClickHandlers mockProjectLink;
   HasClickHandlers mockIterationFilesLink;
   HasClickHandlers mockErrorNotificationBtn;
   HasClickHandlers mockSearchButton;
   HasClickHandlers mockDocumentListButton;
   HasClickHandlers mockResizeButton;
   HasClickHandlers mockKeyShortcutButton;

   Display mockDisplay;
   EventBus mockEventBus;
   Boolean mockIsReadOnly;
   History mockHistory;
   WebTransMessages mockMessages;
   Person mockPerson;

   KeyShortcutPresenter mockKeyShortcutPresenter;
   DocumentListPresenter mockDocumentListPresenter;
   SearchResultsPresenter mockSearchResultsPresenter;
   TranslationPresenter mockTranslationPresenter;
   NotificationPresenter mockNotificationPresenter;
   LayoutSelectorPresenter mockLayoutPresenter;
   SideMenuPresenter mockSideMenuPresenter;

   Window mockWindow;
   Location mockWindowLocation;
   UserWorkspaceContext mockUserWorkspaceContext;
   WorkspaceContext mockWorkspaceContext;
   
   private Capture<ClickHandler> capturedDocumentLinkClickHandler;
   private Capture<ClickHandler> capturedProjectLinkClickHandler;
   private Capture<ClickHandler> capturedIterationFilesLinkClickHandler;
   private Capture<ClickHandler> capturedSearchLinkClickHandler;
   private Capture<ClickHandler> capturedDocumentListClickHandler;
   private Capture<ClickHandler> capturedResizeClickHandler;
   private Capture<ClickHandler> capturedKeyShortcutButtonClickHandler;
   private Capture<ClickHandler> capturedErrorNotificationBtnHandler;

   private Capture<DocumentSelectionEvent> capturedDocumentSelectionEvent;
   private Capture<DocumentStatsUpdatedEventHandler> capturedDocumentStatsUpdatedEventHandler;
   private Capture<String> capturedHistoryTokenString;
   private Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   private Capture<KeyShortcut> capturedKeyShortcuts;
   private Capture<ProjectStatsUpdatedEventHandler> capturedProjectStatsUpdatedEventHandler;
   private Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdatedEventHandler;
   private Capture<PresenterRevealedHandler> capturedPresenterRevealedHandler;
   private Capture<ShowSideMenuEventHandler> capturedShowSideMenuHandler;
   

   private DocumentInfo testDocInfo;
   private DocumentId testDocId;
   private TranslationStats testDocStats;
   private TranslationStats emptyProjectStats;

   @BeforeClass
   public void createMocks()
   {
      createAllMocks();
      createAllCaptures();
   }

   private void createAllMocks()
   {
      mockDisplay = createAndAddMock(AppPresenter.Display.class);
      mockDocumentListPresenter = createAndAddMock(DocumentListPresenter.class);
      mockDocumentsLink = createAndAddMock(HasClickHandlers.class);
      mockProjectLink = createAndAddMock(HasClickHandlers.class);
      mockIterationFilesLink = createAndAddMock(HasClickHandlers.class);
      mockErrorNotificationBtn = createAndAddMock(HasClickHandlers.class);
      mockSearchButton = createAndAddMock(HasClickHandlers.class);
      mockDocumentListButton = createAndAddMock(HasClickHandlers.class);
      mockResizeButton = createAndAddMock(HasClickHandlers.class);
      mockKeyShortcutButton = createAndAddMock(HasClickHandlers.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockHistory = createAndAddMock(History.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockPerson = createAndAddMock(Person.class);
      mockSearchResultsPresenter = createAndAddMock(SearchResultsPresenter.class);
      mockTranslationPresenter = createAndAddMock(TranslationPresenter.class);
      mockWindow = createAndAddMock(Window.class);
      mockWindowLocation = createAndAddMock(Window.Location.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockWorkspaceContext = createAndAddMock(WorkspaceContext.class);
      mockNotificationPresenter = createAndAddMock(NotificationPresenter.class);
      mockLayoutPresenter = createAndAddMock(LayoutSelectorPresenter.class);
      mockSideMenuPresenter = createAndAddMock(SideMenuPresenter.class);
   }

   private void createAllCaptures()
   {
      capturedSearchLinkClickHandler = new Capture<ClickHandler>();
      capturedDocumentListClickHandler = new Capture<ClickHandler>();
      capturedResizeClickHandler = new Capture<ClickHandler>();
      capturedKeyShortcutButtonClickHandler = new Capture<ClickHandler>();
      capturedDocumentLinkClickHandler = new Capture<ClickHandler>();
      capturedProjectLinkClickHandler = new Capture<ClickHandler>();
      capturedIterationFilesLinkClickHandler = new Capture<ClickHandler>();
      capturedErrorNotificationBtnHandler = new Capture<ClickHandler>();
      capturedDocumentSelectionEvent = new Capture<DocumentSelectionEvent>();
      capturedDocumentStatsUpdatedEventHandler = new Capture<DocumentStatsUpdatedEventHandler>();
      capturedHistoryTokenString = new Capture<String>();
      capturedHistoryValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      capturedKeyShortcuts = new Capture<KeyShortcut>();
      capturedProjectStatsUpdatedEventHandler = new Capture<ProjectStatsUpdatedEventHandler>();
      capturedWorkspaceContextUpdatedEventHandler = new Capture<WorkspaceContextUpdateEventHandler>();
      capturedPresenterRevealedHandler = new Capture<PresenterRevealedHandler>();
      capturedShowSideMenuHandler = new Capture<ShowSideMenuEventHandler>();
   }

   @BeforeMethod
   void beforeMethod()
   {
      resetAll();
      appPresenter = new AppPresenter(mockDisplay, mockEventBus, mockSideMenuPresenter, mockKeyShortcutPresenter, mockTranslationPresenter, mockDocumentListPresenter, mockSearchResultsPresenter, mockNotificationPresenter, mockLayoutPresenter, mockUserWorkspaceContext, mockMessages, mockHistory, mockWindow, mockWindowLocation);
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
      mockSideMenuPresenter.showEditorMenu(true);
      expectLastCall().once();
      mockDisplay.setResizeVisible(true);
      expectLastCall().once();
      mockTranslationPresenter.revealDisplay();
      expectLastCall().once();

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
      mockSideMenuPresenter.showEditorMenu(true);
      expectLastCall().once();
      mockDisplay.setResizeVisible(true);
      expectLastCall().once();
      mockTranslationPresenter.revealDisplay();
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
      // update document stats
      capturedDocumentStatsUpdatedEventHandler.getValue().onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(testDocId, updatedStats));
      simulateReturnToEditorView(token);

      verifyAllMocks();
   }

   public void testSearchLinkGeneratesHistoryToken()
   {
      ClickEvent searchLinkClickEvent = createMock(ClickEvent.class);
      expect(mockHistory.getToken()).andReturn("").once();
      replayAllMocks();
      appPresenter.bind();
      // simulate click
      capturedSearchLinkClickHandler.getValue().onClick(searchLinkClickEvent);
      HistoryToken capturedToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("clicking search link should set view in history token to search", capturedToken.getView(), is(MainView.Search));
      // TODO could check that nothing else has changed in token
      verifyAllMocks();
   }

   public void testShowsHidesReadonlyLabel()
   {
      // receives readonly event, label shown
      WorkspaceContextUpdateEvent readOnlyEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(readOnlyEvent.isProjectActive()).andReturn(false).anyTimes();
      mockDisplay.setReadOnlyVisible(true);
      expectLastCall().anyTimes();
      mockUserWorkspaceContext.setProjectActive(false);
      expectLastCall().anyTimes();

      // receives not-readonly event, label hidden
      WorkspaceContextUpdateEvent editableEvent = createMock(WorkspaceContextUpdateEvent.class);
      expect(editableEvent.isProjectActive()).andReturn(true).anyTimes();
      mockDisplay.setReadOnlyVisible(false);
      expectLastCall().anyTimes();
      mockUserWorkspaceContext.setProjectActive(true);
      expectLastCall().anyTimes();

      mockEventBus.fireEvent(isA(NotificationEvent.class));
      expectLastCall().anyTimes();
      expect(mockMessages.notifyEditableWorkspace()).andReturn("");
      expectLastCall().anyTimes();
      expect(mockMessages.notifyReadOnlyWorkspace()).andReturn("read only");
      expectLastCall().anyTimes();

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
      
      mockTranslationPresenter.revealDisplay();
      expectLastCall().anyTimes();
      
      mockSideMenuPresenter.showEditorMenu(true);
      expectLastCall().once();
      
      mockDisplay.setResizeVisible(true);
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
    * Sets expectations to show the given view, update the document label, show
    * the given stats, and save pending editor changes.
    * 
    * @param toView the view to show
    * @param expectedStats the stats that should be displayed
    * @param expectedDocLabel the text that the document label is expected to
    *           show
    */
   private void expectViewTransitionFromEditor(MainView toView, TranslationStats expectedStats, String expectedDocLabel)
   {
      // expect return to given view
      mockDisplay.showInMainView(toView);
      expectLastCall().once();
      if (toView == MainView.Search)
      {
         mockSearchResultsPresenter.revealDisplay();
         expectLastCall().once();

         mockTranslationPresenter.concealDisplay();
         expectLastCall().once();
         
         mockSideMenuPresenter.showEditorMenu(false);
         expectLastCall().once();
         
         mockDisplay.setResizeVisible(false);
         expectLastCall().once();
      }
      else
      {
         mockSearchResultsPresenter.concealDisplay();
         expectLastCall().once();
         
         if (toView == MainView.Editor)
         {
            mockTranslationPresenter.revealDisplay();
            expectLastCall().once();
            
            mockSideMenuPresenter.showEditorMenu(true);
            expectLastCall().once();
            
            mockDisplay.setResizeVisible(true);
            expectLastCall().once();
         }
         else if (toView == MainView.Documents)
         {
            mockTranslationPresenter.concealDisplay();
            expectLastCall().once();
            
            mockSideMenuPresenter.showEditorMenu(false);
            expectLastCall().once();
            
            mockDisplay.setResizeVisible(false);
            expectLastCall().once();
         }
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
      // simulate page transition
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
    * @param documentStats the stats object that has been set for the given
    *           document
    */
   private void expectReturnToEditorView(TranslationStats documentStats)
   {
      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
      mockSideMenuPresenter.showEditorMenu(true);
      expectLastCall().once();
      mockDisplay.setResizeVisible(true);
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(documentStats));
      expectLastCall().once();
   }

   @Override
   protected void resetTestObjects()
   {
      buildTestDocumentInfo();
      emptyProjectStats = new TranslationStats();
   }

   private void buildTestDocumentInfo()
   {
      testDocId = new DocumentId(2222L);
      TransUnitCount unitCount = new TransUnitCount(1, 2, 3);
      TransUnitWords wordCount = new TransUnitWords(4, 5, 6);
      testDocStats = new TranslationStats(unitCount, wordCount);
      testDocInfo = new DocumentInfo(testDocId, TEST_DOCUMENT_NAME, TEST_DOCUMENT_PATH, LocaleId.EN_US, testDocStats);
   }

   @Override
   protected void setDefaultBindExpectations()
   {
      expectSubPresenterBindings();
      expectHandlerRegistrations();
      expectPresenterSetupActions();
      setupMockGetterReturnValues();

      // misc and capture setup
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

      mockLayoutPresenter.bind();
      expectLastCall().once();
      mockLayoutPresenter.setLayoutListener(mockTranslationPresenter);
      expectLastCall().once();
      mockSideMenuPresenter.bind();
      expectLastCall().once();
   }

   @SuppressWarnings("unchecked")
   private void expectHandlerRegistrations()
   {
      expect(mockHistory.addValueChangeHandler(and(capture(capturedHistoryValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(mockHandlerRegistration()).once();

      expectClickHandlerRegistration(mockDocumentsLink, capturedDocumentLinkClickHandler);
      expectClickHandlerRegistration(mockProjectLink, capturedProjectLinkClickHandler);
      expectClickHandlerRegistration(mockIterationFilesLink, capturedIterationFilesLinkClickHandler);
      expectClickHandlerRegistration(mockErrorNotificationBtn, capturedErrorNotificationBtnHandler);
      expectClickHandlerRegistration(mockSearchButton, capturedSearchLinkClickHandler);
      expectClickHandlerRegistration(mockKeyShortcutButton, capturedKeyShortcutButtonClickHandler);
      expectClickHandlerRegistration(mockDocumentListButton, capturedDocumentListClickHandler);
      expectClickHandlerRegistration(mockResizeButton, capturedResizeClickHandler);
      
      expectEventHandlerRegistration(mockEventBus, DocumentStatsUpdatedEvent.getType(), DocumentStatsUpdatedEventHandler.class, capturedDocumentStatsUpdatedEventHandler);
      expectEventHandlerRegistration(mockEventBus, ProjectStatsUpdatedEvent.getType(), ProjectStatsUpdatedEventHandler.class, capturedProjectStatsUpdatedEventHandler);
      expectEventHandlerRegistration(mockEventBus, WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdatedEventHandler);
      expectEventHandlerRegistration(mockEventBus, PresenterRevealedEvent.getType(), PresenterRevealedHandler.class, capturedPresenterRevealedHandler);
      expectEventHandlerRegistration(mockEventBus, ShowSideMenuEvent.getType(), ShowSideMenuEventHandler.class, capturedShowSideMenuHandler);
   }

   private void expectPresenterSetupActions()
   {
      mockWindow.setTitle(TEST_WINDOW_TITLE);
      expectLastCall().once();
      // mockDisplay.setProjectLinkLabel(TEST_WORKSPACE_NAME,
      // TEST_WORKSPACE_TITLE);
      // expectLastCall().anyTimes();
      mockDisplay.setReadOnlyVisible(false);
      expectLastCall().once();
      // initially empty project stats
      mockDisplay.setStats(eq(emptyProjectStats));
      expectLastCall().once();

      mockDisplay.setDocumentLabel("", NO_DOCUMENTS_STRING);
      expectLastCall().once();

      mockDisplay.showInMainView(MainView.Documents);
      expectLastCall().once(); // starts on document list view

      mockTranslationPresenter.concealDisplay();
      expectLastCall().once();
      
      mockSideMenuPresenter.showEditorMenu(false);
      expectLastCall().once();
      
      mockDisplay.setResizeVisible(false);
      expectLastCall().once();

      // due to this display beginning as concealed
      mockSearchResultsPresenter.concealDisplay();
      expectLastCall().once();
      
      expect(mockKeyShortcutPresenter.register(and(capture(capturedKeyShortcuts), isA(KeyShortcut.class)))).andReturn(null).anyTimes();
   }

   private void setupMockGetterReturnValues()
   {
      expect(mockDisplay.getProjectLink()).andReturn(mockProjectLink).anyTimes();
      expect(mockDisplay.getDocumentsLink()).andReturn(mockDocumentsLink).anyTimes();
      expect(mockDisplay.getIterationFilesLink()).andReturn(mockIterationFilesLink).anyTimes();
      expect(mockDisplay.getNotificationBtn()).andReturn(mockErrorNotificationBtn).anyTimes();

      mockDisplay.setProjectLinkLabel(TEST_PPROJET_SLUG);
      expectLastCall().once();

      mockDisplay.setIterationFilesLabel(TEST_ITERATION_SLUG + "(" + TEST_LOCALE_ID + ")");
      expectLastCall().once();

      expect(mockDisplay.getSearchAndReplaceButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockDisplay.getDocumentListButton()).andReturn(mockDocumentListButton).anyTimes();
      expect(mockDisplay.getResizeButton()).andReturn(mockResizeButton).anyTimes();
      expect(mockDisplay.getKeyShortcutButton()).andReturn(mockKeyShortcutButton).anyTimes();
      
      expect(mockPerson.getName()).andReturn(TEST_PERSON_NAME).anyTimes();

      expect(mockWindowLocation.getParameter(WORKSPACE_TITLE_QUERY_PARAMETER_KEY)).andReturn(TEST_WORKSPACE_TITLE).anyTimes();

      expect(mockMessages.windowTitle(TEST_WORKSPACE_NAME, TEST_LOCALE_NAME)).andReturn(TEST_WINDOW_TITLE).anyTimes();

      expect(mockMessages.windowTitle2(TEST_WORKSPACE_NAME, TEST_LOCALE_NAME, TEST_WORKSPACE_TITLE)).andReturn(TEST_WINDOW_TITLE).anyTimes();

      expect(mockMessages.noDocumentSelected()).andReturn(NO_DOCUMENTS_STRING).anyTimes();
      expect(mockMessages.projectWideSearchAndReplace()).andReturn(SEARCH_PAGE_LABEL).anyTimes();
      expect(mockMessages.showDocumentListKeyShortcut()).andReturn(DOCUMENT_LIST_KEY_SHORTCUT_DESCRIPTION).anyTimes();
      expect(mockMessages.showEditorKeyShortcut()).andReturn(SHOW_EDITOR_KEY_SHORTCUT_DESCRIPTION).anyTimes();
      expect(mockMessages.showProjectWideSearch()).andReturn(SHOW_PROJECT_WIDE_SEARCH_KEY_SHORTCUT_DESCRIPTION).anyTimes();

      expect(mockUserWorkspaceContext.getWorkspaceContext()).andReturn(mockWorkspaceContext).anyTimes();
      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(false).anyTimes();
      
      expect(mockWorkspaceContext.getWorkspaceId()).andReturn(new WorkspaceId(new ProjectIterationId(TEST_PPROJET_SLUG, TEST_ITERATION_SLUG), new LocaleId(TEST_LOCALE_ID))).anyTimes();

      expect(mockWorkspaceContext.getWorkspaceName()).andReturn(TEST_WORKSPACE_NAME).anyTimes();
      expect(mockWorkspaceContext.getLocaleName()).andReturn(TEST_LOCALE_NAME).anyTimes();

      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();
   }
}
