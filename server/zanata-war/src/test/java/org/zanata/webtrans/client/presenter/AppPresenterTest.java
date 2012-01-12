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
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.equalTo;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.presenter.AppPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;

@Test(groups = { "unit-tests" })
public class AppPresenterTest
{

   private static final String TEST_PERSON_NAME = "Mister Ed";
   private static final String TEST_WORKSPACE_NAME = "Test Workspace Name";
   private static final String TEST_LOCALE_NAME = "Test Locale Name";
   private static final String TEST_WINDOW_TITLE = "Test Window Title";

   private static final String WORKSPACE_TITLE_QUERY_PARAMETER_KEY = "title";
   private static final String NO_DOCUMENTS_STRING = "No document selected";
   private static final String TEST_DOCUMENT_NAME = "test document name";
   private static final String TEST_DOCUMENT_PATH = "test/document/path";


   private AppPresenter appPresenter;


   private CachingDispatchAsync mockDispatcher;
   private Display mockDisplay;
   private DocumentListPresenter mockDocumentListPresenter;
   private HasClickHandlers mockDocumentsLink;
   private EventBus mockEventBus;
   private History mockHistory;
   private Identity mockIdentity;
   private HasClickHandlers mockLeaveWorkspaceLink;
   private WebTransMessages mockMessages;
   private Person mockPerson;
   private HasClickHandlers mockSignoutLink;
   private TranslationPresenter mockTranslationPresenter;
   private Window mockWindow;
   private Window.Location mockWindowLocation;
   private WorkspaceContext mockWorkspaceContext;

   private Capture<NotificationEventHandler> capturedNotificationEventHandler;
   private Capture<DocumentStatsUpdatedEventHandler> capturedDocumentStatsUpdatedEventHandler;
   private Capture<ProjectStatsUpdatedEventHandler> capturedProjectStatsUpdatedEventHandler;
   private Capture<ClickHandler> capturedLeaveWorkspaceLinkClickHandler;
   private Capture<ClickHandler> capturedSignoutLinkClickHandler;
   private Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   private Capture<ClickHandler> capturedDocumentLinkClickHandler;
   private Capture<DocumentSelectionEvent> capturedDocumentSelectionEvent;
   private DocumentInfo testDocInfo;
   private DocumentId testDocId;
   private TranslationStats testDocStats;

   // private Capture<String> capturedHistoryTokenString;



   @BeforeClass
   public void createMocks()
   {
      mockDispatcher = createMock(CachingDispatchAsync.class);
      mockDisplay = createMock(AppPresenter.Display.class);
      mockDocumentListPresenter = createMock(DocumentListPresenter.class);
      mockDocumentsLink = createMock(HasClickHandlers.class);
      mockEventBus = createMock(EventBus.class);
      mockHistory = createMock(History.class);
      mockIdentity = createMock(Identity.class);
      mockMessages = createMock(WebTransMessages.class);
      mockPerson = createMock(Person.class);
      mockTranslationPresenter = createMock(TranslationPresenter.class);
      mockWindow = createMock(Window.class);
      mockWindowLocation = createMock(Window.Location.class);
      mockWorkspaceContext = createMock(WorkspaceContext.class);

      mockLeaveWorkspaceLink = createMock(HasClickHandlers.class);
      mockSignoutLink = createMock(HasClickHandlers.class);
   }

   private AppPresenter newAppPresenter()
   {
      return new AppPresenter(mockDisplay, mockEventBus, mockTranslationPresenter, mockDocumentListPresenter, mockIdentity, mockWorkspaceContext, mockMessages, mockHistory, mockWindow, mockWindowLocation);
   }

   @Test
   public void performsRequiredActionsOnBind()
   {
      resetAllMocks();
      setupDefaultMockExpectations();

      // default mock expectations include:
      // TODO check this is true
      // - bind doclistpresenter
      // - bind translationpresenter
      // - show documents view initially
      // - set user label
      // - set workspace name + title
      // - set window title
      // - show 'No document selected'

      replayAllMocks();

      appPresenter = newAppPresenter();
      appPresenter.bind();

      verifyAllMocks();
   }

   @Test
   public void showsNotificationEvents()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay);
      String testMessage = "test notification message";
      mockDisplay.setNotificationMessage(testMessage);
      expectLastCall().once();
      replay(mockDisplay);

      NotificationEvent notification = new NotificationEvent(Severity.Info, testMessage);
      capturedNotificationEventHandler.getValue().onNotification(notification);

      verify(mockDisplay);
   }

   @Test
   public void showsUpdatedProjectStats()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay);
      Capture<TranslationStats> capturedTranslationStats = new Capture<TranslationStats>();
      mockDisplay.setStats(and(capture(capturedTranslationStats), isA(TranslationStats.class)));
      expectLastCall().once();
      replay(mockDisplay);

      TranslationStats testStats = new TranslationStats(new TransUnitCount(6, 5, 4), new TransUnitWords(3, 2, 1));
      ProjectStatsUpdatedEvent event = new ProjectStatsUpdatedEvent(testStats);
      capturedProjectStatsUpdatedEventHandler.getValue().onProjectStatsRetrieved(event);

      verify(mockDisplay);

      assertThat(capturedTranslationStats.getValue(), is(equalTo(testStats)));
   }

   @Test
   public void historyTriggersDocumentSelectionEvent()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay, mockDocumentListPresenter);

      // not testing for specific values for the following in this test
      mockDisplay.setDocumentLabel(notNull(String.class), notNull(String.class));
      expectLastCall().anyTimes();
      mockDisplay.setStats(notNull(TranslationStats.class));
      expectLastCall().anyTimes();

      buildTestDocumentInfo();
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();

      replay(mockDisplay, mockDocumentListPresenter);

      HistoryToken docSelectionToken = new HistoryToken();
      docSelectionToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docSelectionToken.toTokenString())
      {
      });

      assertThat("a new document path in history should trigger a document selection event with the correct id", capturedDocumentSelectionEvent.getValue().getDocumentId(), is(testDocId));
   }

   @Test
   public void historyTriggersViewChange()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay, mockDocumentListPresenter);

      // not interested in name or stats for this test
      mockDisplay.setDocumentLabel(notNull(String.class), notNull(String.class));
      expectLastCall().anyTimes();
      mockDisplay.setStats(notNull(TranslationStats.class));
      expectLastCall().anyTimes();

      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();

      // requires selected document to change to editor view
      buildTestDocumentInfo();
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();

      replay(mockDisplay, mockDocumentListPresenter);

      HistoryToken docInEditorToken = new HistoryToken();
      docInEditorToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      docInEditorToken.setView(MainView.Editor);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docInEditorToken.toTokenString())
      {
      });
      verify(mockDisplay);
   }

   @Test
   public void noEditorWithoutValidDocument()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay, mockDocumentListPresenter);
      // return invalid document
      expect(mockDocumentListPresenter.getDocumentId(notNull(String.class))).andReturn(null).anyTimes();
      replay(mockDisplay, mockDocumentListPresenter);

      HistoryToken editorWithoutDocToken = new HistoryToken();
      editorWithoutDocToken.setView(MainView.Editor);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(editorWithoutDocToken.toTokenString())
      {
      });

      // not expecting show view editor
      verify(mockDisplay);
   }


   @Test
   public void historyTriggersDocumentNameStatsUpdate()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      reset(mockDisplay, mockDocumentListPresenter);

      buildTestDocumentInfo();
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();

      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(testDocStats));
      expectLastCall().once();

      replay(mockDisplay, mockDocumentListPresenter);

      HistoryToken docInEditorToken = new HistoryToken();
      docInEditorToken.setView(MainView.Editor);
      docInEditorToken.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(docInEditorToken.toTokenString())
      {
      });

      verify(mockDisplay);
   }


   @Test
   public void statsAndNameChangeWithView()
   {
      setupAndBindAppPresenter();
      verifyAllMocks();

      // change to editor
      reset(mockDisplay, mockDocumentListPresenter);
      buildTestDocumentInfo();
      expect(mockDocumentListPresenter.getDocumentId(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME)).andReturn(testDocId).anyTimes();
      expect(mockDocumentListPresenter.getDocumentInfo(testDocId)).andReturn(testDocInfo).anyTimes();
      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(testDocStats));
      expectLastCall().once();
      replay(mockDisplay, mockDocumentListPresenter);
      HistoryToken token = new HistoryToken();
      token.setView(MainView.Editor);
      token.setDocumentPath(TEST_DOCUMENT_PATH + TEST_DOCUMENT_NAME);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(token.toTokenString())
      {
      });
      verify(mockDisplay);

      // return to doc list
      reset(mockDisplay, mockTranslationPresenter);
      mockDisplay.showInMainView(MainView.Documents);
      expectLastCall().once();
      mockDisplay.setDocumentLabel("", NO_DOCUMENTS_STRING);
      expectLastCall().once();
      // project stats are empty in default test setup
      mockDisplay.setStats(eq(new TranslationStats()));
      expectLastCall().once();
      mockTranslationPresenter.saveEditorPendingChange();
      expectLastCall().once();
      replay(mockDisplay, mockTranslationPresenter);
      token.setView(MainView.Documents);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(token.toTokenString())
      {
      });

      // return to editor
      reset(mockDisplay);
      mockDisplay.showInMainView(MainView.Editor);
      expectLastCall().once();
      mockDisplay.setDocumentLabel(TEST_DOCUMENT_PATH, TEST_DOCUMENT_NAME);
      expectLastCall().once();
      mockDisplay.setStats(eq(testDocStats));
      expectLastCall().once();
      replay(mockDisplay);
      token.setView(MainView.Editor);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(token.toTokenString())
      {
      });

   }

   // @Test
   // public void showsUpdatedDocumentStats()
   // {
   // // TODO
   // }

   // @Test
   // public void doesNotShowWrongDocumentStats()
   // {
   // // TODO
   // }

   // @Test
   // public void saveEditorChangeOnViewChange()
   // {
   // // TODO
   // }


   /**
    * generates new test doc id and doc info ready for use in tests
    */
   private void buildTestDocumentInfo()
   {
      testDocId = new DocumentId(2222L);
      TransUnitCount unitCount = new TransUnitCount(1, 2, 3);
      TransUnitWords wordCount = new TransUnitWords(4, 5, 6);
      testDocStats = new TranslationStats(unitCount, wordCount);
      testDocInfo = new DocumentInfo(testDocId, TEST_DOCUMENT_NAME, TEST_DOCUMENT_PATH, testDocStats);
   }

   private void setupAndBindAppPresenter()
   {
      resetAllMocks();
      setupDefaultMockExpectations();
      replayAllMocks();
      appPresenter = newAppPresenter();
      appPresenter.bind();
   }

   private void setupDefaultMockExpectations()
   {
      expect(mockDisplay.getSignOutLink()).andReturn(mockSignoutLink).anyTimes();
      expect(mockDisplay.getLeaveWorkspaceLink()).andReturn(mockLeaveWorkspaceLink).anyTimes();
      expect(mockDisplay.getDocumentsLink()).andReturn(mockDocumentsLink).anyTimes();
      // TODO think about what restrictions are appropriate here
      // (will likely need to specify different display changes)
      mockDisplay.showInMainView(MainView.Documents);
      expectLastCall().anyTimes();
      // TODO this will vary depending on the test
      mockDisplay.setDocumentLabel("", NO_DOCUMENTS_STRING);
      expectLastCall().once();
      mockDisplay.setUserLabel(TEST_PERSON_NAME);
      expectLastCall().anyTimes();
      mockDisplay.setWorkspaceNameLabel(TEST_WORKSPACE_NAME, "");
      expectLastCall().anyTimes();
      // TODO this will vary depending on the specific test
      mockDisplay.setStats(notNull(TranslationStats.class));
      expectLastCall().once();

      mockDocumentListPresenter.bind();
      expectLastCall().once();

      capturedDocumentLinkClickHandler = new Capture<ClickHandler>();
      expect(mockDocumentsLink.addClickHandler(and(capture(capturedDocumentLinkClickHandler), isA(ClickHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      capturedNotificationEventHandler = new Capture<NotificationEventHandler>();
      expect(mockEventBus.addHandler(eq(NotificationEvent.getType()), and(capture(capturedNotificationEventHandler), isA(NotificationEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedDocumentStatsUpdatedEventHandler = new Capture<DocumentStatsUpdatedEventHandler>();
      expect(mockEventBus.addHandler(eq(DocumentStatsUpdatedEvent.getType()), and(capture(capturedDocumentStatsUpdatedEventHandler), isA(DocumentStatsUpdatedEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedProjectStatsUpdatedEventHandler = new Capture<ProjectStatsUpdatedEventHandler>();
      expect(mockEventBus.addHandler(eq(ProjectStatsUpdatedEvent.getType()), and(capture(capturedProjectStatsUpdatedEventHandler), isA(ProjectStatsUpdatedEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      capturedDocumentSelectionEvent = new Capture<DocumentSelectionEvent>();
      mockEventBus.fireEvent(and(capture(capturedDocumentSelectionEvent), isA(DocumentSelectionEvent.class)));
      expectLastCall().anyTimes();

      setupMockHistory("");

      expect(mockIdentity.getPerson()).andReturn(mockPerson).anyTimes();

      capturedLeaveWorkspaceLinkClickHandler = new Capture<ClickHandler>();
      expect(mockLeaveWorkspaceLink.addClickHandler(and(capture(capturedLeaveWorkspaceLinkClickHandler), isA(ClickHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      expect(mockMessages.windowTitle(TEST_WORKSPACE_NAME, TEST_LOCALE_NAME)).andReturn(TEST_WINDOW_TITLE).anyTimes();
      expect(mockMessages.noDocumentSelected()).andReturn(NO_DOCUMENTS_STRING).anyTimes();

      expect(mockPerson.getName()).andReturn(TEST_PERSON_NAME).anyTimes();

      capturedSignoutLinkClickHandler = new Capture<ClickHandler>();
      expect(mockSignoutLink.addClickHandler(and(capture(capturedSignoutLinkClickHandler), isA(ClickHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

      mockTranslationPresenter.bind();
      expectLastCall().once();

      mockWindow.setTitle(TEST_WINDOW_TITLE);
      expectLastCall().once();

      expect(mockWindowLocation.getParameter(WORKSPACE_TITLE_QUERY_PARAMETER_KEY)).andReturn("").anyTimes();

      expect(mockWorkspaceContext.getWorkspaceName()).andReturn(TEST_WORKSPACE_NAME).anyTimes();
      expect(mockWorkspaceContext.getLocaleName()).andReturn(TEST_LOCALE_NAME).anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockHistory(String tokenToReturn)
   {
      capturedHistoryValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      expect(mockHistory.addValueChangeHandler(and(capture(capturedHistoryValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).anyTimes();
      expect(mockHistory.getToken()).andReturn(tokenToReturn).anyTimes();
      mockHistory.fireCurrentHistoryState();
      expectLastCall().anyTimes();

      // capturedHistoryTokenString = new Capture<String>();
      // mockHistory.newItem(capture(capturedHistoryTokenString));
      // expectLastCall().anyTimes();
   }

   private void resetAllMocks()
   {
      reset(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockDocumentsLink);
      reset(mockEventBus, mockHistory, mockIdentity, mockLeaveWorkspaceLink);
      reset(mockMessages, mockPerson, mockSignoutLink, mockTranslationPresenter);
      reset(mockWindow, mockWindowLocation, mockWorkspaceContext);
   }

   private void replayAllMocks()
   {
      replay(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockDocumentsLink);
      replay(mockEventBus, mockHistory, mockIdentity, mockLeaveWorkspaceLink);
      replay(mockMessages, mockPerson, mockSignoutLink, mockTranslationPresenter);
      replay(mockWindow, mockWindowLocation, mockWorkspaceContext);
   }

   private void verifyAllMocks()
   {
      verify(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockDocumentsLink);
      verify(mockEventBus, mockHistory, mockIdentity, mockLeaveWorkspaceLink);
      verify(mockMessages, mockPerson, mockSignoutLink, mockTranslationPresenter);
      verify(mockWindow, mockWindowLocation, mockWorkspaceContext);
   }

}
