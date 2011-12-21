package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.WindowLocation;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

@Test(groups = { "unit-tests" })
public class DocumentListPresenterTest
{
   private DocumentListPresenter dlp;

   @SuppressWarnings("rawtypes")
   private ListDataProvider mockDataProvider;

   private CachingDispatchAsync mockDispatcher;

   private DocumentListPresenter.Display mockDisplay;

   @SuppressWarnings("rawtypes")
   private HasSelectionHandlers mockDocList;

   @SuppressWarnings("rawtypes")
   private HasData mockDocListTable;

   private EventBus mockEventBus;

   @SuppressWarnings("rawtypes")
   private HasValue mockExactSearchCheckbox; // Boolean

   @SuppressWarnings("rawtypes")
   private HasValue mockFilterTextbox; // String

   private History mockHistory;

   private WebTransMessages mockMessages;

   private WindowLocation mockWindowLocation;

   private WorkspaceContext mockWorkspaceContext;

   // this list is updated to update display
   private List<DocumentNode> dataProviderList;

   @BeforeClass
   public void createMocks()
   {
      mockDataProvider = createMock(ListDataProvider.class);
      mockDispatcher = createMock(CachingDispatchAsync.class);
      mockDisplay = createMock(DocumentListPresenter.Display.class);
      mockDocList = createMock(HasSelectionHandlers.class);
      mockDocListTable = createMock(HasData.class);
      mockEventBus = createMock(EventBus.class);
      mockExactSearchCheckbox = createMock(HasValue.class);
      mockFilterTextbox = createMock(HasValue.class);
      mockHistory = createMock(History.class);
      mockMessages = createMock(WebTransMessages.class);
      mockWindowLocation = createMock(WindowLocation.class);
      mockWorkspaceContext = createMock(WorkspaceContext.class);
   }

   private DocumentListPresenter newDocListPresenter()
   {
      return new DocumentListPresenter(mockDisplay, mockEventBus, mockWorkspaceContext, mockDispatcher, mockMessages, mockHistory, mockWindowLocation);
   }

   /**
    * Display's page size should be set to the document list size when the
    * presenter is bound.
    */
   @Test
   public void setsPageSize()
   {
      ArrayList<DocumentInfo> testDocList;
      boolean setupMockDisplay = false;
      boolean setupMockDispatcher = false;

      resetAllMocks();
      setupDefaultMockExpectations(true, setupMockDispatcher, setupMockDisplay, true, true, true, true, true, true, true, true);

      // set up dispatcher to return 0 documents, expect page size 0 to be set
      testDocList = new ArrayList<DocumentInfo>();
      setupMockDispatcher(testDocList);
      setupMockDisplay(true, testDocList.size());
      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();

      verifyAllMocks();

      // another trial with 5 documents
      resetAllMocks();
      setupDefaultMockExpectations(true, setupMockDispatcher, setupMockDisplay, true, true, true, true, true, true, true, true);

      testDocList = buildSampleDocumentArray(); // 3 docs

      // add another 2 docs
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(4444L), "doc4444", "fourth/path/", new TranslationStats());
      testDocList.add(docInfo);
      docInfo = new DocumentInfo(new DocumentId(5555L), "doc5555", "fifth/path/", new TranslationStats());
      testDocList.add(docInfo);

      setupMockDispatcher(testDocList);
      setupMockDisplay(true, testDocList.size());
      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();

      verifyAllMocks();
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   @Test
   public void docListFailure()
   {
      boolean setupMockDataProvider = false;
      boolean setupMockDisplay = false;
      boolean setupMockDispatcher = false;
      boolean setupMockEventBus = false;

      resetAllMocks();
      setupDefaultMockExpectations(setupMockDataProvider, setupMockDispatcher, setupMockDisplay, true, setupMockEventBus, true, true, true, true, true, true);

      String failMessage = "test document load fail message";

      // not expecting any data provider interactions if the list did not load

      // make sure general event expectation does not prevent capture
      setupMockEventBus(false);

      Capture<NotificationEvent> capturedNotificationEvent = new Capture<NotificationEvent>();
      mockEventBus.fireEvent(and(capture(capturedNotificationEvent), isA(NotificationEvent.class)));
      expectLastCall().atLeastOnce();

      mockDispatcher.execute((Action) notNull(), (AsyncCallback) notNull());
      expectLastCall().andAnswer(new DocListFailAnswer());

      setupMockDisplay(false, -1);

      expect(mockMessages.loadDocFailed()).andReturn(failMessage).once();

      replayAllMocks();

      dlp = newDocListPresenter();
      dlp.bind();

      verifyAllMocks();

      // make sure the right notification happened
      assertThat(capturedNotificationEvent.getValue().getSeverity(), is(NotificationEvent.Severity.Error));
      assertThat(capturedNotificationEvent.getValue().getMessage(), is(failMessage));
   }

   // TODO TESTS:

   // bind method:
   // if success, expect documents loaded into display via mockDataProvider
   // expect display refresh?

   // pre-filter is forwarded in rpc request for docs

   // local filter text box causes filtering of doc list
   // exact filter matches properly
   // comma-separated filter works

   // simulated document click works as expected

   @Test
   public void getDocumentId()
   {
      setupAndBindDocListPresenter();

      // third document from buildSampleDocumentArray()
      DocumentId docId = dlp.getDocumentId("third/path/doc3333");
      assertThat(docId.getId(), is(3333L));

      // second document from buildSampleDocumentArray()
      docId = dlp.getDocumentId("second/path/doc2222");
      assertThat(docId.getId(), is(2222L));
   }

   @Test
   public void getDocumentInfo()
   {
      setupAndBindDocListPresenter();

      DocumentInfo docInfo = dlp.getDocumentInfo(new DocumentId(1111L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(1111L), "doc1111", "first/path/", new TranslationStats()))));

      docInfo = dlp.getDocumentInfo(new DocumentId(3333L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(3333L), "doc3333", "third/path/", new TranslationStats()))));
   }

   private void setupAndBindDocListPresenter()
   {
      resetAllMocks();
      setupDefaultMockExpectations();
      replayAllMocks();

      dlp = newDocListPresenter();
      dlp.bind();
   }

   private ArrayList<DocumentInfo> buildSampleDocumentArray()
   {
      ArrayList<DocumentInfo> docList = new ArrayList<DocumentInfo>();

      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1111L), "doc1111", "first/path/", new TranslationStats());
      docList.add(docInfo);

      docInfo = new DocumentInfo(new DocumentId(2222L), "doc2222", "second/path/", new TranslationStats());
      docList.add(docInfo);

      docInfo = new DocumentInfo(new DocumentId(3333L), "doc3333", "third/path/", new TranslationStats());
      docList.add(docInfo);

      return docList;
   }

   private void setupDefaultMockExpectations()
   {
      setupDefaultMockExpectations(true, true, true, true, true, true, true, true, true, true, true);
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void setupDefaultMockExpectations(boolean dataProvider, boolean dispatcher, boolean display, boolean docList, boolean eventBus, boolean checkbox, boolean textbox, boolean history, boolean messages, boolean windowLoc, boolean workspaceContext)
   {
      if (dataProvider)
      {
         dataProviderList = new ArrayList<DocumentNode>();

         expect(mockDataProvider.getList()).andReturn(dataProviderList).anyTimes();
         mockDataProvider.addDataDisplay(mockDocListTable);
         expectLastCall().once();
      }

      int sampleDocArraySize = -1;
      if (dispatcher)
      {
         ArrayList<DocumentInfo> sampleArray = buildSampleDocumentArray();
         setupMockDispatcher(sampleArray);
         sampleDocArraySize = sampleArray.size();
      }

      if (display)
      {
         setupMockDisplay(dispatcher, sampleDocArraySize);
      }

      if (docList)
      {
         expect(mockDocList.addSelectionHandler((SelectionHandler) notNull())).andReturn(createMock(HandlerRegistration.class));
      }

      if (eventBus)
      {
         setupMockEventBus(true);
      }

      if (checkbox)
      {
         expect(mockExactSearchCheckbox.addValueChangeHandler((ValueChangeHandler<Boolean>) notNull())).andReturn(createMock(HandlerRegistration.class));
      }

      if (textbox)
      {
         expect(mockFilterTextbox.addValueChangeHandler((ValueChangeHandler<String>) notNull())).andReturn(createMock(HandlerRegistration.class));
      }

      if (history)
      {
         expect(mockHistory.addValueChangeHandler((ValueChangeHandler<String>) notNull())).andReturn(createMock(HandlerRegistration.class));
         mockHistory.fireCurrentHistoryState();
         expectLastCall().anyTimes();
      }

      if (windowLoc)
      {
         expect(mockWindowLocation.getParameterMap()).andReturn(Collections.EMPTY_MAP).anyTimes();
      }

      if (workspaceContext)
      {
         expect(mockWorkspaceContext.getWorkspaceId()).andReturn(new WorkspaceId(new ProjectIterationId("mockProjectSlug", "mockIterationSlug"), new LocaleId("es"))).anyTimes();
      }
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void setupMockEventBus(boolean expectAllEvents)
   {
      // TODO may need to mock handler registrations here?
      expect(mockEventBus.addHandler((GwtEvent.Type<EventHandler>) notNull(), (EventHandler) notNull())).andReturn(createMock(HandlerRegistration.class)).anyTimes();

      if (expectAllEvents)
      {
         mockEventBus.fireEvent((GwtEvent) notNull());
         expectLastCall().anyTimes();
      }
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void setupMockDispatcher(ArrayList<DocumentInfo> docListToReturn)
   {
      mockDispatcher.execute((Action) notNull(), (AsyncCallback) notNull());
      expectLastCall().andAnswer(new DoclistSuccessAnswer(docListToReturn));
   }

   @SuppressWarnings("unchecked")
   private void setupMockDisplay(boolean expectPageSize, int expectedSetPageSize)
   {
      if (expectPageSize)
      {
         mockDisplay.setPageSize(expectedSetPageSize);
         expectLastCall().once();
      }

      expect(mockDisplay.getDataProvider()).andReturn(mockDataProvider).anyTimes();
      expect(mockDisplay.getDocumentList()).andReturn(mockDocList).anyTimes();
      expect(mockDisplay.getFilterTextBox()).andReturn(mockFilterTextbox).anyTimes();
      expect(mockDisplay.getExactSearchCheckbox()).andReturn(mockExactSearchCheckbox).anyTimes();
      expect(mockDisplay.getDocumentListTable()).andReturn(mockDocListTable).anyTimes();
   }

   private void resetAllMocks()
   {
      reset(mockDataProvider);
      reset(mockDispatcher);
      reset(mockDisplay);
      reset(mockDocList);
      reset(mockDocListTable);
      reset(mockEventBus);
      reset(mockExactSearchCheckbox);
      reset(mockFilterTextbox);
      reset(mockHistory);
      reset(mockMessages);
      reset(mockWindowLocation);
      reset(mockWorkspaceContext);
   }

   private void replayAllMocks()
   {
      replay(mockDataProvider);
      replay(mockDispatcher);
      replay(mockDisplay);
      replay(mockDocList);
      replay(mockDocListTable);
      replay(mockEventBus);
      replay(mockExactSearchCheckbox);
      replay(mockFilterTextbox);
      replay(mockHistory);
      replay(mockMessages);
      replay(mockWindowLocation);
      replay(mockWorkspaceContext);
   }

   private void verifyAllMocks()
   {
      verify(mockDataProvider);
      verify(mockDispatcher);
      verify(mockDisplay);
      verify(mockDocList);
      verify(mockDocListTable);
      verify(mockEventBus);
      verify(mockExactSearchCheckbox);
      verify(mockFilterTextbox);
      verify(mockHistory);
      verify(mockMessages);
      verify(mockWindowLocation);
      verify(mockWorkspaceContext);
   }

   private class DoclistSuccessAnswer implements IAnswer<GetDocumentListResult>
   {

      private ArrayList<DocumentInfo> docsToReturn;

      public DoclistSuccessAnswer(ArrayList<DocumentInfo> docsToReturn)
      {
         this.docsToReturn = docsToReturn;
      }

      @Override
      public GetDocumentListResult answer() throws Throwable
      {
         // TODO make a useful result object or pass it in the constructor
         GetDocumentListResult result = new GetDocumentListResult(new ProjectIterationId("mock-slug", "mock-iteration-slug"), docsToReturn);

         // get the most recent argument before this call - should be the
         // callback function as this is the last parameter for the execute
         // method
         Object[] arguments = EasyMock.getCurrentArguments();
         @SuppressWarnings("unchecked")
         AsyncCallback<GetDocumentListResult> callback = (AsyncCallback<GetDocumentListResult>) arguments[arguments.length - 1];
         callback.onSuccess(result);
         return null;
      }
   }

   private class DocListFailAnswer implements IAnswer<GetDocumentListResult>
   {

      @Override
      public GetDocumentListResult answer() throws Throwable
      {
         Object[] arguments = EasyMock.getCurrentArguments();
         @SuppressWarnings("unchecked")
         AsyncCallback<GetDocumentListResult> callback = (AsyncCallback<GetDocumentListResult>) arguments[arguments.length - 1];
         callback.onFailure(new Throwable("test"));
         return null;
      }

   }

}
