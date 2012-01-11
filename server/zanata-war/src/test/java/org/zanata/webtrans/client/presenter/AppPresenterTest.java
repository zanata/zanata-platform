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
import net.customware.gwt.presenter.client.EventBus;

import static org.hamcrest.MatcherAssert.assertThat;
//import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.easymock.Capture;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.events.NotificationEventHandler;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.presenter.AppPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.Person;
import org.zanata.webtrans.shared.model.WorkspaceContext;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
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


   private AppPresenter appPresenter;


   private CachingDispatchAsync mockDispatcher;
   private Display mockDisplay;
   private DocumentListPresenter mockDocumentListPresenter;
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

   private Capture<DocumentSelectionHandler> capturedDocumentSelectionHandler;
   private Capture<NotificationEventHandler> capturedNotificationEventHandler;
   private Capture<DocumentStatsUpdatedEventHandler> capturedDocumentStatsUpdatedEventHandler;
   private Capture<ProjectStatsUpdatedEventHandler> capturedProjectStatsUpdatedEventHandler;
   private Capture<ClickHandler> capturedLeaveWorkspaceLinkClickHandler;
   private Capture<ClickHandler> capturedSignoutLinkClickHandler;
   private Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;

   // private Capture<String> capturedHistoryTokenString;



   @BeforeClass
   public void createMocks()
   {
      mockDispatcher = createMock(CachingDispatchAsync.class);
      mockDisplay = createMock(AppPresenter.Display.class);
      mockDocumentListPresenter = createMock(DocumentListPresenter.class);
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

      assertThat(capturedTranslationStats.getValue().getUnitCount().getApproved(), is(6));
      assertThat(capturedTranslationStats.getValue().getUnitCount().getNeedReview(), is(5));
      assertThat(capturedTranslationStats.getValue().getUnitCount().getUntranslated(), is(4));
      assertThat(capturedTranslationStats.getValue().getWordCount().getApproved(), is(3));
      assertThat(capturedTranslationStats.getValue().getWordCount().getNeedReview(), is(2));
      assertThat(capturedTranslationStats.getValue().getWordCount().getUntranslated(), is(1));
   }

   // TODO tests:
   // - document selection (shows stats, shows name and path)
   // - document stats updated (show for current doc, not for different doc, not
   // while in project view, show when switching back to doc view)
   // - see notes for more

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

      capturedNotificationEventHandler = new Capture<NotificationEventHandler>();
      expect(mockEventBus.addHandler(eq(NotificationEvent.getType()), and(capture(capturedNotificationEventHandler), isA(NotificationEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedDocumentSelectionHandler = new Capture<DocumentSelectionHandler>();
      expect(mockEventBus.addHandler(eq(DocumentSelectionEvent.getType()), and(capture(capturedDocumentSelectionHandler), isA(DocumentSelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedDocumentStatsUpdatedEventHandler = new Capture<DocumentStatsUpdatedEventHandler>();
      expect(mockEventBus.addHandler(eq(DocumentStatsUpdatedEvent.getType()), and(capture(capturedDocumentStatsUpdatedEventHandler), isA(DocumentStatsUpdatedEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedProjectStatsUpdatedEventHandler = new Capture<ProjectStatsUpdatedEventHandler>();
      expect(mockEventBus.addHandler(eq(ProjectStatsUpdatedEvent.getType()), and(capture(capturedProjectStatsUpdatedEventHandler), isA(ProjectStatsUpdatedEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();

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
      reset(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockEventBus);
      reset(mockHistory, mockIdentity, mockLeaveWorkspaceLink, mockMessages);
      reset(mockPerson, mockSignoutLink, mockTranslationPresenter, mockWindow);
      reset(mockWindowLocation, mockWorkspaceContext);
   }

   private void replayAllMocks()
   {
      replay(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockEventBus);
      replay(mockHistory, mockIdentity, mockLeaveWorkspaceLink, mockMessages);
      replay(mockPerson, mockSignoutLink, mockTranslationPresenter, mockWindow);
      replay(mockWindowLocation, mockWorkspaceContext);
   }

   private void verifyAllMocks()
   {
      verify(mockDispatcher, mockDisplay, mockDocumentListPresenter, mockEventBus);
      verify(mockHistory, mockIdentity, mockLeaveWorkspaceLink, mockMessages);
      verify(mockPerson, mockSignoutLink, mockTranslationPresenter, mockWindow);
      verify(mockWindowLocation, mockWorkspaceContext);
   }

}
