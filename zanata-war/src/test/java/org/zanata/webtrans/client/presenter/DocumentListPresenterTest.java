package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.captureInt;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.replay;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.easymock.CaptureType;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

@SuppressWarnings("rawtypes")
@Test(groups = { "unit-tests" })
public class DocumentListPresenterTest extends PresenterTest
{
   private static final String TEST_PROJECT_SLUG = "test-project";
   private static final String TEST_ITERATION_SLUG = "test-iteration";
   private static final String TEST_LOCALE_ID = "es";
   private static final String TEST_DOC_LOAD_FAIL_MESSAGE = "test document load fail message";
   private static final String TEST_BY_WORDS_MESSAGE = "By Words";
   private static final String TEST_BY_MESSAGE_MESSAGE = "By Message";

   // field for document list presenter under test
   private DocumentListPresenter documentListPresenter;

   // mocks for interacting classes
   HasValue mockCaseSensitiveCheckbox; // Boolean
   ListDataProvider mockDataProvider;
   DocumentListPresenter.Display mockDisplay;
   HasData mockDocListTable;
   HasChangeHandlers mockStatsOption;
   EventBus mockEventBus;
   HasValue mockExactSearchCheckbox; // Boolean
   HasValue mockFilterTextbox; // String
   History mockHistory;
   WebTransMessages mockMessages;
   UserWorkspaceContext mockUserWorkspaceContext;
   WorkspaceContext mockWorkspaceContext;

   // this list is updated to update display table
   private List<DocumentNode> dataProviderList;

   // captured events and handlers used in several tests
   Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   Capture<GetDocumentList> capturedDocListRequest;
   Capture<AsyncCallback<GetDocumentListResult>> capturedDocListRequestCallback;
   Capture<TransUnitUpdatedEventHandler> capturedTransUnitUpdatedEventHandler;
   Capture<GwtEvent> capturedEventBusEvent;

   Capture<Integer> capturedPageSize;
   Capture<String> capturedHistoryTokenString;
   Capture<HistoryToken> capturedHistoryToken;

   Capture<SingleSelectionModel<DocumentNode>> capturedSingleSelectionModel;

   @BeforeClass
   public void createMocks()
   {
      mockCaseSensitiveCheckbox = createAndAddMock(HasValue.class);
      mockDataProvider = createAndAddMock(ListDataProvider.class);
      mockDisplay = createAndAddMock(DocumentListPresenter.Display.class);
      mockStatsOption = createAndAddMock(HasChangeHandlers.class);
      mockDocListTable = createAndAddMock(HasData.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockExactSearchCheckbox = createAndAddMock(HasValue.class);
      mockFilterTextbox = createAndAddMock(HasValue.class);
      mockHistory = createAndAddMock(History.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockWorkspaceContext = createAndAddMock(WorkspaceContext.class);

      capturedDocListRequest = addCapture(new Capture<GetDocumentList>());
      capturedDocListRequestCallback = addCapture(new Capture<AsyncCallback<GetDocumentListResult>>());
      capturedTransUnitUpdatedEventHandler = addCapture(new Capture<TransUnitUpdatedEventHandler>());
      capturedEventBusEvent = addCapture(new Capture<GwtEvent>(CaptureType.ALL));
      capturedPageSize = addCapture(new Capture<Integer>());
      capturedHistoryTokenString = addCapture(new Capture<String>());
      capturedHistoryToken = addCapture(new Capture<HistoryToken>());
      capturedSingleSelectionModel = addCapture(new Capture<SingleSelectionModel<DocumentNode>>());
   }

   @BeforeMethod
   public void resetMocks()
   {
      resetAll();
      documentListPresenter = new DocumentListPresenter(mockDisplay, mockEventBus, mockUserWorkspaceContext, mockMessages, mockHistory);
   }

   @Test
   public void setsPageSize()
   {
      setDefaultMockBehaviour();
      // default test document list has 3 documents
      replayAllMocks();
      documentListPresenter.bind();

      verifyAllMocks();
      assertThat("display page size should be set on bind to a high enough value to show all documents on one page", capturedPageSize.getValue(), greaterThanOrEqualTo(3));
   }

   @Test
   public void loadDocsIntoDataProvider()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.setDocuments(buildSampleDocumentArray());

      verifyAllMocks();

      // right amount of docs
      assertThat("the data provider should have the same sized document list returned from the server", dataProviderList.size(), is(3));

      // used in setupAndBind method above
      ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();

      ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
      for (DocumentNode node : dataProviderList)
      {
         assertThat("the data provider should have only documents that were returned from the server", node.getDocInfo(), isIn(expectedDocs));
         actualDocInfos.add(node.getDocInfo());
      }
      assertThat("the data provider should have all documents returned from the server", actualDocInfos, hasItems(expectedDocs.get(0), expectedDocs.get(1), expectedDocs.get(2)));
   }

   @Test
   public void generatesDocumentStatsOnTuUpdate()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();
      ArrayList<DocumentInfo> documentInfos = buildSampleDocumentArray();
      TranslationStats stats = new TranslationStats();
      for (DocumentInfo documentInfo : documentInfos)
      {
         stats.add(documentInfo.getStats());
      }
      documentListPresenter.setDocuments(documentInfos);
      documentListPresenter.setProjectStats(stats);

      simulateTransUnitUpdateInDocTwo();

      DocumentStatsUpdatedEvent docStatsEvent = null;
      for (GwtEvent event : capturedEventBusEvent.getValues())
         if (event.getAssociatedType().equals(DocumentStatsUpdatedEvent.getType()))
            docStatsEvent = (DocumentStatsUpdatedEvent) event;

      assertThat("a document stats event should be fired when a TU update event occurs, not found", docStatsEvent, notNullValue());

      // document stats
      assertThat("document id in document stats event shoudl match updated TU document id", docStatsEvent.getDocId(), equalTo(new DocumentId(2222L)));

      // check actual counts (approved/fuzzy/untranslated)
      // default TUs: 1/2/3
      // approving 1 fuzzy, expect 2/1/3
      assertThat("document Approved TU count should increase by 1 when a TU is updated from NeedsReview to Approved", docStatsEvent.getNewStats().getUnitCount().getApproved(), is(2));
      assertThat("document NeedsReview TU count should decrease by 1 when a TU is updated from NeedsReview to Approved", docStatsEvent.getNewStats().getUnitCount().getNeedReview(), is(1));
      assertThat("document Untranslated TU count should remain the same when a TU is updated from NeedsReview to Approved", docStatsEvent.getNewStats().getUnitCount().getUntranslated(), is(3));

      // default words: 4/5/6
      // approving 3 fuzzy so expect 7/2/6
      assertThat("document Approved words should increase when TU changes to Approved", docStatsEvent.getNewStats().getWordCount().getApproved(), is(7));
      assertThat("document NeedsReview words should decrease when a TU changes from NeedsReview", docStatsEvent.getNewStats().getWordCount().getNeedReview(), is(2));
      assertThat("document Untranslated words should not change when TU changes between NeedsReview and Approved", docStatsEvent.getNewStats().getWordCount().getNeedReview(), is(2));
   }

   @Test
   public void generatesProjectStatsOnTuUpdate()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();
      ArrayList<DocumentInfo> documentInfos = buildSampleDocumentArray();
      TranslationStats stats = new TranslationStats();
      for (DocumentInfo documentInfo : documentInfos)
      {
         stats.add(documentInfo.getStats());
      }
      documentListPresenter.setDocuments(documentInfos);
      documentListPresenter.setProjectStats(stats);

      simulateTransUnitUpdateInDocTwo();

      ProjectStatsUpdatedEvent projectStatsEvent = null;

      for (GwtEvent event : capturedEventBusEvent.getValues())
         if (event.getAssociatedType().equals(ProjectStatsUpdatedEvent.getType()))
            projectStatsEvent = (ProjectStatsUpdatedEvent) event;

      assertThat("a project stats event should be fired when a TU update event occurs, not found", projectStatsEvent, notNullValue());

      // default TUs: 3/6/9 (approved/fuzzy/untranslated)
      // approving 1 fuzzy, expect 4/5/9
      assertThat("project Approved TU count should increase by 1 when a TU changes to Approved status", projectStatsEvent.getProjectStats().getUnitCount().getApproved(), is(4));
      assertThat("project NeedsReview TU count should decrease by 1 when a TU changes from Approved status", projectStatsEvent.getProjectStats().getUnitCount().getNeedReview(), is(5));
      assertThat("project Untranslates TU count should not change when TU changes between NeedsReview and Approved", projectStatsEvent.getProjectStats().getUnitCount().getUntranslated(), is(9));

      // default words: 12/15/18
      // approving 3 fuzzy, expect 15/12/18
      assertThat("project Approved words should increase when TU changes to Approved", projectStatsEvent.getProjectStats().getWordCount().getApproved(), is(15));
      assertThat("project NeedsReview words should decrease when a TU changes from NeedsReview", projectStatsEvent.getProjectStats().getWordCount().getNeedReview(), is(12));
      assertThat("project Untranslated words should not change when TU changes between NeedsReview and Approved", projectStatsEvent.getProjectStats().getWordCount().getUntranslated(), is(18));
   }

   private void simulateTransUnitUpdateInDocTwo()
   {
      // simulate TU updated in second document
      TransUnitUpdatedEvent mockEvent = createMock(TransUnitUpdatedEvent.class);
      ArrayList<String> sources = new ArrayList<String>();
      sources.add("this is the source");
      boolean plural = false;

      ArrayList<String> targets = new ArrayList<String>();
      targets.add("this is the target");

      TransUnit newTransUnit = TransUnit.Builder.newTransUnitBuilder()
            .setId(12345L).setResId("resId").setLocaleId("es").setPlural(plural)
            .setSources(sources).setSourceComment("this is the source comment")
            .setTargets(targets).setStatus(ContentState.Approved).setLastModifiedBy("lastModifiedBy")
            .setLastModifiedTime("lastModifiedTime").setMsgContext("msgContext").setRowIndex(1)
            .setVerNum(1)
            .build();
      TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(true, true, new DocumentId(2222L), newTransUnit, 3, 0, ContentState.NeedReview);
      expect(mockEvent.getUpdateInfo()).andReturn(updateInfo).anyTimes();
      replay(mockEvent);
      capturedTransUnitUpdatedEventHandler.getValue().onTransUnitUpdated(mockEvent);
   }

   @Test
   public void filterTextUpdateGeneratesHistoryToken()
   {
      setDefaultMockBehaviour();
      String filterText = "path/doc12";
      // these seem to persist beyond verify, so setting them up here is fine
      expect(mockFilterTextbox.getValue()).andReturn(filterText).anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.fireFilterToken(filterText);

      verifyAllMocks();

      HistoryToken capturedHistoryToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("generated history token filter text should match the filter textbox", capturedHistoryToken.getDocFilterText(), is(filterText));
      assertThat("generated history token filter exact flag should match the exact match checkbox", capturedHistoryToken.getDocFilterExact(), is(false));
   }

   @Test
   public void checkExactSearchCheckboxGeneratesHistoryToken()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();

      // simulate checking 'exact search' checkbox
      documentListPresenter.fireExactSearchToken(true);

      verifyAllMocks();

      HistoryToken exactSearchToken = new HistoryToken();
      exactSearchToken.setDocFilterExact(true);
      assertThat("checking the 'exact search' checkbox should be reflected in a new history token", capturedHistoryTokenString.getValue(), is(exactSearchToken.toTokenString()));
   }

   @Test
   public void uncheckExactSearchCheckboxGeneratesHistoryToken()
   {
      // history reflects checkbox already checked
      HistoryToken exactSearchToken = new HistoryToken();
      exactSearchToken.setDocFilterExact(true);
      setupMockHistory(exactSearchToken.toTokenString());

      replayAllMocks();
      documentListPresenter.bind();

      documentListPresenter.fireExactSearchToken(false);

      verifyAllMocks();

      HistoryToken inexactSearchToken = new HistoryToken();
      inexactSearchToken.setDocFilterExact(false);
      assertThat("unchecking the 'exact search' checkbox should be reflected in a new history token", capturedHistoryTokenString.getValue(), is(inexactSearchToken.toTokenString()));
   }

   // TODO tests for check and uncheck case sensitive check

   @Test
   public void documentSelectUpdatesHistoryToken()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();

      // simulate document click on second document
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(2222L), "doc122", "second/path/", LocaleId.EN_US, new TranslationStats());
      documentListPresenter.fireDocumentSelection(docInfo);

      verifyAllMocks();
      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("path of selected document should be set in history token", newToken.getDocumentPath(), is("second/path/doc122"));
      assertThat("view in history token should change to individual document view when a new document is selected", newToken.getView(), is(MainView.Editor));
   }

   @Test
   public void exactSearchMatchesExactOnly()
   {
      setDefaultMockBehaviour();
      // should match 1 of the 3 sample documents
      String filterText = "match/exact/filter";

      mockDisplay.updateFilter(false, true, filterText);

      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.setDocuments(buildSampleDocumentArray());
      documentListPresenter.updateFilterAndRun(filterText, true, false);

      // simulate firing history change event
      HistoryToken historyTokenWithExactFilter = new HistoryToken();
      historyTokenWithExactFilter.setDocFilterText(filterText);
      historyTokenWithExactFilter.setDocFilterExact(true);

      verifyAllMocks();

      ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
      expectedDocs.remove(2); // third doc does not match the filter
      expectedDocs.remove(0); // first doc does not match the filter
      ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
      for (DocumentNode node : dataProviderList)
      {
         assertThat("the data provider should have only documents that exactly match the current filter", node.getDocInfo(), isIn(expectedDocs));
         actualDocInfos.add(node.getDocInfo());
      }
      assertThat("the data provider should have all documents that exactly match the filter", actualDocInfos, hasItems(expectedDocs.get(0)));
      assertThat("the data provider list should contain exactly the number of documents matching the filter", dataProviderList.size(), is(1));
   }

   // TODO test case sensitivity option

   @Test
   public void commaSeparatedFilter()
   {
      setDefaultMockBehaviour();
      // should match first and last of the 3 sample documents
      // multiple matching strings for third to check that there is no
      // duplication, also variable whitespace
      String filterText = " does/not, not/match ,no/filter ";
      
      mockDisplay.updateFilter(false, false, filterText);

      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.setDocuments(buildSampleDocumentArray());
      documentListPresenter.updateFilterAndRun(filterText, false, false);

      // simulate firing history change event
      HistoryToken historyTokenWithFilter = new HistoryToken();
      historyTokenWithFilter.setDocFilterText(filterText);

      verifyAllMocks();

      ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
      expectedDocs.remove(1); // second doc does not match any of the filter
                              // strings
      ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
      for (DocumentNode node : dataProviderList)
      {
         assertThat("the data provider should have only documents that match the current filter", node.getDocInfo(), isIn(expectedDocs));
         actualDocInfos.add(node.getDocInfo());
      }
      assertThat("the data provider should have all documents that match the filter", actualDocInfos, hasItems(expectedDocs.get(0), expectedDocs.get(1)));
      assertThat("the data provider list should contain exactly the number of documents matching the filter", dataProviderList.size(), is(2));
   }

   // TODO test case sensitive check updated from history

   // TODO test: update selected document when different from doc selection
   // event, as in DocumentListPresenter.setSelection()

   @Test
   public void getDocumentId()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.setDocuments(buildSampleDocumentArray());
      verifyAllMocks();

      // third document from buildSampleDocumentArray()
      DocumentId docId = documentListPresenter.getDocumentId("does/not/match/exact/filter");
      assertThat(docId.getId(), is(3333L));

      // second document from buildSampleDocumentArray()
      docId = documentListPresenter.getDocumentId("match/exact/filter");
      assertThat(docId.getId(), is(2222L));
   }

   @Test
   public void getDocumentInfo()
   {
      setDefaultMockBehaviour();
      replayAllMocks();
      documentListPresenter.bind();
      documentListPresenter.setDocuments(buildSampleDocumentArray());
      verifyAllMocks();

      DocumentInfo docInfo = documentListPresenter.getDocumentInfo(new DocumentId(1111L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(1111L), "doc111", "first/path/", LocaleId.EN_US, new TranslationStats()))));

      docInfo = documentListPresenter.getDocumentInfo(new DocumentId(3333L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(3333L), "doc123", "third/path/", LocaleId.EN_US, new TranslationStats()))));
   }

   private ArrayList<DocumentInfo> buildSampleDocumentArray()
   {
      ArrayList<DocumentInfo> docList = new ArrayList<DocumentInfo>();

      TransUnitCount unitCount = new TransUnitCount(1, 2, 3);
      TransUnitWords wordCount = new TransUnitWords(4, 5, 6);

      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1111L), "matches", "no/filter", LocaleId.EN_US, new TranslationStats(unitCount, wordCount));
      docList.add(docInfo);

      docInfo = new DocumentInfo(new DocumentId(2222L), "filter", "match/exact/", LocaleId.EN_US, new TranslationStats(unitCount, wordCount));
      docList.add(docInfo);

      docInfo = new DocumentInfo(new DocumentId(3333L), "filter", "does/not/match/exact/", LocaleId.EN_US, new TranslationStats(unitCount, wordCount));
      docList.add(docInfo);

      return docList;
   }

   /**
    * Default mock behaviour in addition to setDefaultBindExpectations, suitable
    * for most tests but kept separate for a few requiring different behaviour
    */
   private void setDefaultMockBehaviour()
   {
      setupMockHistory("");
   }

   @SuppressWarnings("unchecked")
   @Override
   protected void setDefaultBindExpectations()
   {
      setupMockDataProvider();

      setupMockDisplay();

      expect(mockMessages.loadDocFailed()).andReturn(TEST_DOC_LOAD_FAIL_MESSAGE).anyTimes();
      expect(mockMessages.byWords()).andReturn(TEST_BY_WORDS_MESSAGE).anyTimes();
      expect(mockMessages.byMessages()).andReturn(TEST_BY_MESSAGE_MESSAGE).anyTimes();

      // expect(mockDocList.addSelectionHandler(and(capture(capturedDocumentSelectionHandler),
      // isA(SelectionHandler.class)))).andReturn(mockHandlerRegistration());
      // expect(mockStatsOption.addChangeHandler(and(capture(capturedStatsOptionChangeHandler),
      // isA(ChangeHandler.class)))).andReturn(mockHandlerRegistration());
      setupMockEventBus(true);
      // expectValueChangeHandlerRegistration(mockExactSearchCheckbox,
      // capturedCheckboxChangeHandler);
      // expectValueChangeHandlerRegistration(mockCaseSensitiveCheckbox,
      // capturedCaseSensitiveCheckboxChangeHandler);
      // expectValueChangeHandlerRegistration(mockFilterTextbox,
      // capturedTextboxChangeHandler);

      expect(mockUserWorkspaceContext.getWorkspaceContext()).andReturn(mockWorkspaceContext).anyTimes();
      expect(mockWorkspaceContext.getWorkspaceId()).andReturn(new WorkspaceId(new ProjectIterationId(TEST_PROJECT_SLUG, TEST_ITERATION_SLUG), new LocaleId(TEST_LOCALE_ID))).anyTimes();

      mockUserWorkspaceContext.setSelectedDoc(new DocumentInfo(new DocumentId(2222L), "doc122", "second/path/", LocaleId.EN_US, new TranslationStats()));
      expectLastCall().anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockHistory(String tokenToReturn)
   {
      // TODO set up History to properly extend ValueChangeHandler<String>, then use convenience method for handler registration
      expect(mockHistory.getToken()).andReturn(tokenToReturn).anyTimes();
      expect(mockHistory.getHistoryToken()).andReturn(HistoryToken.fromTokenString(tokenToReturn)).anyTimes();
      mockHistory.fireCurrentHistoryState();
      expectLastCall().anyTimes();

      mockHistory.newItem(capture(capturedHistoryTokenString));
      expectLastCall().anyTimes();

      mockHistory.newItem(capture(capturedHistoryToken));
      expectLastCall().anyTimes();
   }

   private void setupMockDataProvider()
   {
      dataProviderList = new ArrayList<DocumentNode>();
      mockDataProvider.refresh();
      expectLastCall().anyTimes();
      expect(mockDataProvider.getList()).andReturn(dataProviderList).anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockEventBus(boolean expectAllEvents)
   {
      expectEventHandlerRegistration(mockEventBus, TransUnitUpdatedEvent.getType(), TransUnitUpdatedEventHandler.class, capturedTransUnitUpdatedEventHandler);
      expect(mockEventBus.addHandler((GwtEvent.Type<EventHandler>) notNull(), (EventHandler) notNull())).andReturn(mockHandlerRegistration()).anyTimes();

      mockEventBus.fireEvent(and(capture(capturedEventBusEvent), isA(GwtEvent.class)));
      expectLastCall().anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockDisplay()
   {
      mockDisplay.setPageSize(captureInt(capturedPageSize));
      expectLastCall().anyTimes();

      mockDisplay.setListener(isA(HasDocumentListListener.class));

      mockDisplay.renderTable(and(capture(capturedSingleSelectionModel), isA(SingleSelectionModel.class)));
      expectLastCall().anyTimes();

      expect(mockDisplay.getDataProvider()).andReturn(mockDataProvider).anyTimes();
      // expect(mockDisplay.getDocumentList()).andReturn(mockDocList).anyTimes();
      // expect(mockDisplay.getFilterTextBox()).andReturn(mockFilterTextbox).anyTimes();
      // expect(mockDisplay.getCaseSensitiveCheckbox()).andReturn(mockCaseSensitiveCheckbox).anyTimes();
      // expect(mockDisplay.getExactSearchCheckbox()).andReturn(mockExactSearchCheckbox).anyTimes();
      expect(mockDisplay.getDocumentListTable()).andReturn(mockDocListTable).anyTimes();
      // expect(mockDisplay.getStatsOption()).andReturn(mockStatsOption).anyTimes();

      mockDisplay.addStatsOption(TEST_BY_MESSAGE_MESSAGE, "Message");
      mockDisplay.addStatsOption(TEST_BY_WORDS_MESSAGE, "Words");
      mockDisplay.setStatsFilter("Words");

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
         GetDocumentListResult result = new GetDocumentListResult(new ProjectIterationId(TEST_PROJECT_SLUG, TEST_ITERATION_SLUG), docsToReturn);
         capturedDocListRequestCallback.getValue().onSuccess(result);
         return null;
      }
   }

   private class DocListFailAnswer implements IAnswer<GetDocumentListResult>
   {
      @Override
      public GetDocumentListResult answer() throws Throwable
      {
         capturedDocListRequestCallback.getValue().onFailure(new Throwable("test"));
         return null;
      }
   }

}
