package org.zanata.webtrans.client.presenter;

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.captureInt;
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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isIn;
import static org.hamcrest.Matchers.notNullValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
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
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentList;
import org.zanata.webtrans.shared.rpc.GetDocumentListResult;

import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

@SuppressWarnings("rawtypes")
@Test(groups = { "unit-tests" })
public class DocumentListPresenterTest
{
   private static String testProjectSlug = "test-project";
   private static String testIterationSlug = "test-iteration";
   private static String testLocaleId = "es";

   // field for document list presenter under test
   private DocumentListPresenter dlp;

   // mocks for interacting classes
   private HasValue mockCaseSensitiveCheckbox; // Boolean
   private ListDataProvider mockDataProvider;
   private CachingDispatchAsync mockDispatcher;
   private DocumentListPresenter.Display mockDisplay;
   private HasSelectionHandlers mockDocList;
   private HasData mockDocListTable;
   private EventBus mockEventBus;
   private HasValue mockExactSearchCheckbox; // Boolean
   private HasValue mockFilterTextbox; // String
   private History mockHistory;
   private WebTransMessages mockMessages;
   private Window.Location mockWindowLocation;
   private WorkspaceContext mockWorkspaceContext;

   // this list is updated to update display table
   private List<DocumentNode> dataProviderList;

   // captured events and handlers used in several tests
   private Capture<ValueChangeHandler<String>> capturedHistoryValueChangeHandler;
   private Capture<ValueChangeHandler<String>> capturedTextboxChangeHandler;
   private Capture<ValueChangeHandler<Boolean>> capturedCheckboxChangeHandler;
   private Capture<ValueChangeHandler<Boolean>> capturedCaseSensitiveCheckboxChangeHandler;
   private Capture<SelectionHandler<DocumentInfo>> capturedDocumentSelectionHandler;
   private Capture<GetDocumentList> capturedDocListRequest;
   private Capture<AsyncCallback<GetDocumentListResult>> capturedDocListRequestCallback;
   private Capture<TransUnitUpdatedEventHandler> capturedTransUnitUpdatedEventHandler;
   private Capture<GwtEvent> capturedEventBusEvent;

   private Capture<Integer> capturedPageSize;
   private Capture<String> capturedHistoryTokenString;
   private Capture<HistoryToken> capturedHistoryToken;

   @BeforeClass
   public void createMocks()
   {
      mockCaseSensitiveCheckbox = createMock(HasValue.class);
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
      mockWindowLocation = createMock(Window.Location.class);
      mockWorkspaceContext = createMock(WorkspaceContext.class);

      capturedCaseSensitiveCheckboxChangeHandler = new Capture<ValueChangeHandler<Boolean>>();
      capturedHistoryToken = new Capture<HistoryToken>();
   }

   @BeforeMethod
   public void resetMocks()
   {
      capturedCaseSensitiveCheckboxChangeHandler.reset();
      capturedHistoryToken.reset();
   }

   private DocumentListPresenter newDocListPresenter()
   {
      return new DocumentListPresenter(mockDisplay, mockEventBus, mockWorkspaceContext, mockDispatcher, mockMessages, mockHistory, mockWindowLocation);
   }

   @Test
   public void requestsDocumentsOnBind()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();

      GetDocumentList documentListRequest = capturedDocListRequest.getValue();
      ProjectIterationId requestedProjectIteration = documentListRequest.getProjectIterationId();

      assertThat("requested project slug should match that from workspace context", requestedProjectIteration.getProjectSlug(), is(testProjectSlug));
      assertThat("requested iteration slug should match that from workspace context", requestedProjectIteration.getIterationSlug(), is(testIterationSlug));
      assertThat("request filter parameters should be null when no filters are specified in the query string", documentListRequest.getFilters(), is(equalTo(null)));
   }

   @Test
   public void preFiltersDocuments()
   {
      // simulate "doc" query string parameters
      String firstFilterString = "filter/string/one";
      String secondFilterString = "filter/string/two";
      List<String> filters = new ArrayList<String>();
      filters.add(firstFilterString);
      filters.add(secondFilterString);
      Map<String, List<String>> paramMapWithFilters = new HashMap<String, List<String>>();
      paramMapWithFilters.put("doc", filters);

      resetAllMocks();
      setupDefaultMockExpectations(true, new DoclistSuccessAnswer(buildSampleDocumentArray()), paramMapWithFilters);
      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      String message = "all doc query parameters should be passed as filter strings to the rpc call";
      assertThat(message, firstFilterString, isIn(capturedDocListRequest.getValue().getFilters()));
      assertThat(message, secondFilterString, isIn(capturedDocListRequest.getValue().getFilters()));
      assertThat("only filter strings from query parameters should be passed to the rpc call, too many detected", capturedDocListRequest.getValue().getFilters().size(), is(2));
   }

   @Test
   public void setsPageSize()
   {
      String requirementMessage = "display page size should be set on bind to a high enough value to show all documents on one page";

      // default test document list has 3 documents
      setupAndBindDocListPresenter();
      verifyAllMocks();
      assertThat(requirementMessage, capturedPageSize.getValue(), greaterThanOrEqualTo(3));

      // another trial with 0 documents
      ArrayList<DocumentInfo> testDocList;

      resetAllMocks();
      // returning 0 documents
      testDocList = new ArrayList<DocumentInfo>();
      setupDefaultMockExpectations(true, new DoclistSuccessAnswer(testDocList), Collections.<String, List<String>> emptyMap());
      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();
      assertThat(requirementMessage, capturedPageSize.getValue(), greaterThanOrEqualTo(0));
   }

   @Test
   public void loadDocsIntoDataProvider()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();

      // used in setupAndBind method above
      ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
      ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();

      // right amount of docs
      assertThat("the data provider should have the same sized document list returned from the server", dataProviderList.size(), is(3));
      for (DocumentNode node : dataProviderList)
      {
         assertThat("the data provider should have only documents that were returned from the server", node.getDocInfo(), isIn(expectedDocs));
         actualDocInfos.add(node.getDocInfo());
      }
      assertThat("the data provider should have all documents returned from the server", actualDocInfos, hasItems(expectedDocs.get(0), expectedDocs.get(1), expectedDocs.get(2)));
   }

   @Test
   public void docListFailureNotification()
   {
      resetAllMocks();

      String failMessage = "test document load fail message";
      expect(mockMessages.loadDocFailed()).andReturn(failMessage).anyTimes();

      // not expecting any data provider interactions if the list did not load
      boolean setupMockDataProvider = false;
      setupDefaultMockExpectations(setupMockDataProvider, new DocListFailAnswer(), Collections.<String, List<String>> emptyMap());

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      assertThat("document request failure should be reported in a NotificationEvent", capturedEventBusEvent.getValue().getAssociatedType(), is((Type) NotificationEvent.getType()));
      NotificationEvent capturedNotificationEvent = (NotificationEvent) capturedEventBusEvent.getValue();
      assertThat("when document request form server fails, an error severity notification should occur", capturedNotificationEvent.getSeverity(), is(NotificationEvent.Severity.Error));
      assertThat("the error message from localizable messages should be used to notify of failed document request from server", capturedNotificationEvent.getMessage(), is(failMessage));
   }

   @Test
   public void generatesProjectStatsEvent()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();
      assertThat("when document list is retrieved, project stats should be sent with a ProjectStatsRetrievedEvent", capturedEventBusEvent.getValue().getAssociatedType(), is((Type) ProjectStatsUpdatedEvent.getType()));
      TranslationStats projectStats = ((ProjectStatsUpdatedEvent) capturedEventBusEvent.getValue()).getProjectStats();

      // sample doc stats set to 1, 2, 3, 4, 5, 6 for the following.
      // multiplied by 3 for 3 sample documents.
      assertThat("approved trans unit count", projectStats.getUnitCount().getApproved(), is(3));
      assertThat("needs review trans unit count", projectStats.getUnitCount().getNeedReview(), is(6));
      assertThat("untranslated trans unit count", projectStats.getUnitCount().getUntranslated(), is(9));
      assertThat("approved word count", projectStats.getWordCount().getApproved(), is(12));
      assertThat("needs review word count", projectStats.getWordCount().getNeedReview(), is(15));
      assertThat("untranslated word count", projectStats.getWordCount().getUntranslated(), is(18));
   }

   @Test
   public void generatesDocumentStatsOnTuUpdate()
   {
      setupAndBindDocListPresenter();

      // simulate TU updated in second document
      TransUnitUpdatedEvent mockEvent = createMock(TransUnitUpdatedEvent.class);

      ArrayList<String> sources = new ArrayList<String>();
      sources.add("this is the source");
      boolean plural = false;

      ArrayList<String> targets = new ArrayList<String>();
      sources.add("this is the target");

      TransUnit newTransUnit = TransUnit.Builder.newTransUnitBuilder()
            .setId(12345L).setResId("resId").setLocaleId("es").setPlural(plural)
            .setSources(sources).setSourceComment("this is the source comment")
            .setTargets(targets).setStatus(ContentState.Approved).setLastModifiedBy("lastModifiedBy")
            .setLastModifiedTime("lastModifiedTime").setMsgContext("msgContext").setRowIndex(1)
            .setVerNum(1)
            .build();
      TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(true, new DocumentId(2222L), newTransUnit, 3, 0, ContentState.NeedReview);
      expect(mockEvent.getUpdateInfo()).andReturn(updateInfo).anyTimes();

      replay(mockEvent);
      capturedTransUnitUpdatedEventHandler.getValue().onTransUnitUpdated(mockEvent);

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
      setupAndBindDocListPresenter();

      // simulate TU updated in second document
      TransUnitUpdatedEvent mockEvent = createMock(TransUnitUpdatedEvent.class);
      ArrayList<String> sources = new ArrayList<String>();
      sources.add("this is the source");
      boolean plural = false;

      ArrayList<String> targets = new ArrayList<String>();
      sources.add("this is the target");

      TransUnit newTransUnit = TransUnit.Builder.newTransUnitBuilder()
            .setId(12345L).setResId("resId").setLocaleId("es").setPlural(plural)
            .setSources(sources).setSourceComment("this is the source comment")
            .setTargets(targets).setStatus(ContentState.Approved).setLastModifiedBy("lastModifiedBy")
            .setLastModifiedTime("lastModifiedTime").setMsgContext("msgContext").setRowIndex(1)
            .setVerNum(1)
            .build();
      TransUnitUpdateInfo updateInfo = new TransUnitUpdateInfo(true, new DocumentId(2222L), newTransUnit, 3, 0, ContentState.NeedReview);
      expect(mockEvent.getUpdateInfo()).andReturn(updateInfo).anyTimes();
      replay(mockEvent);
      capturedTransUnitUpdatedEventHandler.getValue().onTransUnitUpdated(mockEvent);

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

   @Test
   public void filterTextUpdateGeneratesHistoryToken()
   {
      resetAllMocks();
      String filterText = "path/doc12";
      // these seem to persist beyond verify, so setting them up here is fine
      expect(mockFilterTextbox.getValue()).andReturn(filterText).anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();
      setupDefaultMockExpectations();
      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      capturedTextboxChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(filterText)
      { // overriding gives access to protected constructor
            });

      HistoryToken capturedHistoryToken = HistoryToken.fromTokenString(capturedHistoryTokenString.getValue());
      assertThat("generated history token filter text should match the filter textbox", capturedHistoryToken.getDocFilterText(), is(filterText));
      assertThat("generated history token filter exact flag should match the exact match checkbox", capturedHistoryToken.getDocFilterExact(), is(false));
   }

   @Test
   public void checkExactSearchCheckboxGeneratesHistoryToken()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();

      // simulate checking 'exact search' checkbox
      capturedCheckboxChangeHandler.getValue().onValueChange(new ValueChangeEvent<Boolean>(true)
      {
      });
      verify(mockHistory);

      HistoryToken exactSearchToken = new HistoryToken();
      exactSearchToken.setDocFilterExact(true);
      assertThat("checking the 'exact search' checkbox should be reflected in a new history token", capturedHistoryTokenString.getValue(), is(exactSearchToken.toTokenString()));
   }

   @Test
   public void uncheckExactSearchCheckboxGeneratesHistoryToken()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();

      // set up history to return 'checked' state for exact search box
      reset(mockHistory);
      HistoryToken exactSearchToken = new HistoryToken();
      exactSearchToken.setDocFilterExact(true);
      setupMockHistory(exactSearchToken.toTokenString());
      replay(mockHistory);

      // simulate unchecking 'exact search' checkbox
      capturedCheckboxChangeHandler.getValue().onValueChange(new ValueChangeEvent<Boolean>(false)
      {
      });
      verify(mockHistory);
      HistoryToken inexactSearchToken = new HistoryToken();
      inexactSearchToken.setDocFilterExact(false);
      assertThat("unchecking the 'exact search' checkbox should be reflected in a new history token", capturedHistoryTokenString.getValue(), is(inexactSearchToken.toTokenString()));
   }

   // TODO tests for check and uncheck case sensitive check

   @Test
   public void documentSelectUpdatesHistoryToken()
   {
      setupAndBindDocListPresenter();
      verifyAllMocks();

      // simulate document click on second document
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(2222L), "doc122", "second/path/", LocaleId.EN_US, new TranslationStats());
      capturedDocumentSelectionHandler.getValue().onSelection(new SelectionEvent<DocumentInfo>(docInfo)
      {
      });
      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("path of selected document should be set in history token", newToken.getDocumentPath(), is("second/path/doc122"));
      assertThat("view in history token should change to individual document view when a new document is selected", newToken.getView(), is(MainView.Editor));
   }

   @Test
   public void historyTokenFiltersDoclist()
   {
      resetAllMocks();
      setupDefaultMockExpectations();

      // should match 2 of the 3 sample documents
      String filterText = "match/exact/filter";
      expect(mockFilterTextbox.getValue()).andReturn(filterText).anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();
      expect(mockCaseSensitiveCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      // simulate firing history change event
      HistoryToken historyTokenWithFilter = new HistoryToken();
      historyTokenWithFilter.setDocFilterText(filterText);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(historyTokenWithFilter.toTokenString())
      {
      });

      ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
      expectedDocs.remove(0); // first doc does not match the filter
      ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
      for (DocumentNode node : dataProviderList)
      {
         assertThat("the data provider should have only documents that match the current filter", node.getDocInfo(), isIn(expectedDocs));
         actualDocInfos.add(node.getDocInfo());
      }
      assertThat("the data provider should have all documents that match the filter", actualDocInfos, hasItems(expectedDocs.get(0), expectedDocs.get(1)));
      assertThat("the data provider list should contain exactly the number of documents matching the filter", dataProviderList.size(), is(2));
   }

   @Test
   public void exactSearchMatchesExactOnly()
   {
      resetAllMocks();
      setupDefaultMockExpectations();

      // should match 1 of the 3 sample documents
      String filterText = "match/exact/filter";
      expect(mockFilterTextbox.getValue()).andReturn(filterText).anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(true).anyTimes();
      expect(mockCaseSensitiveCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      // simulate firing history change event
      HistoryToken historyTokenWithExactFilter = new HistoryToken();
      historyTokenWithExactFilter.setDocFilterText(filterText);
      historyTokenWithExactFilter.setDocFilterExact(true);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(historyTokenWithExactFilter.toTokenString())
      {
      });

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
      resetAllMocks();
      setupDefaultMockExpectations();

      // should match first and last of the 3 sample documents
      // multiple matching strings for third to check that there is no
      // duplication, also variable whitespace
      String filterText = " does/not, not/match ,no/filter ";
      expect(mockFilterTextbox.getValue()).andReturn(filterText).anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();
      expect(mockCaseSensitiveCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      verifyAllMocks();

      // simulate firing history change event
      HistoryToken historyTokenWithFilter = new HistoryToken();
      historyTokenWithFilter.setDocFilterText(filterText);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(historyTokenWithFilter.toTokenString())
      {
      });

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

   @SuppressWarnings("unchecked")
   @Test
   public void filterTextboxUpdatedFromHistory()
   {
      resetAllMocks();
      setupDefaultMockExpectations();

      String filterText = "some filter text";
      // must use fireEvents=true to prevent value being used as greyed-out
      // 'hint' text that automatically clears
      mockFilterTextbox.setValue(filterText, true);
      expectLastCall().once();
      // value should only be set if current value is different from history
      expect(mockFilterTextbox.getValue()).andReturn("different text").anyTimes();
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();
      expect(mockCaseSensitiveCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      // simulate firing history change event
      HistoryToken historyTokenWithFilter = new HistoryToken();
      historyTokenWithFilter.setDocFilterText(filterText);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(historyTokenWithFilter.toTokenString())
      {
      });
      verifyAllMocks();
   }

   @SuppressWarnings("unchecked")
   @Test
   public void filterCheckboxUpdatedFromHistory()
   {
      resetAllMocks();
      setupDefaultMockExpectations();

      mockExactSearchCheckbox.setValue(true);
      expectLastCall().once();
      expect(mockFilterTextbox.getValue()).andReturn("").anyTimes();
      // value should only be set if current value is different from history
      expect(mockExactSearchCheckbox.getValue()).andReturn(false).anyTimes();
      expect(mockCaseSensitiveCheckbox.getValue()).andReturn(false).anyTimes();

      replayAllMocks();
      dlp = newDocListPresenter();
      dlp.bind();
      // simulate firing history change event
      HistoryToken historyTokenWithExactFilter = new HistoryToken();
      historyTokenWithExactFilter.setDocFilterExact(true);
      capturedHistoryValueChangeHandler.getValue().onValueChange(new ValueChangeEvent<String>(historyTokenWithExactFilter.toTokenString())
      {
      });
      verifyAllMocks();
   }

   // TODO test case sensitive check updated from history

   // TODO test: update selected document when different from doc selection
   // event, as in DocumentListPresenter.setSelection()

   @Test
   public void getDocumentId()
   {
      setupAndBindDocListPresenter();

      // third document from buildSampleDocumentArray()
      DocumentId docId = dlp.getDocumentId("does/not/match/exact/filter");
      assertThat(docId.getId(), is(3333L));

      // second document from buildSampleDocumentArray()
      docId = dlp.getDocumentId("match/exact/filter");
      assertThat(docId.getId(), is(2222L));
   }

   @Test
   public void getDocumentInfo()
   {
      setupAndBindDocListPresenter();

      DocumentInfo docInfo = dlp.getDocumentInfo(new DocumentId(1111L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(1111L), "doc111", "first/path/", LocaleId.EN_US, new TranslationStats()))));

      docInfo = dlp.getDocumentInfo(new DocumentId(3333L));
      assertThat(docInfo, is(equalTo(new DocumentInfo(new DocumentId(3333L), "doc123", "third/path/", LocaleId.EN_US, new TranslationStats()))));
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

   private void setupDefaultMockExpectations()
   {
      setupDefaultMockExpectations(true, new DoclistSuccessAnswer(buildSampleDocumentArray()), Collections.<String, List<String>> emptyMap());
   }

   @SuppressWarnings("unchecked")
   private void setupDefaultMockExpectations(boolean dataProvider, IAnswer<? extends Object> docListRequestAnswer, Map<String, List<String>> windowLocationParameters)
   {
      if (dataProvider)
      {
         setupMockDataProvider();
      }

      setupMockDispatcher(docListRequestAnswer);
      setupMockDisplay();
      capturedDocumentSelectionHandler = new Capture<SelectionHandler<DocumentInfo>>();
      expect(mockDocList.addSelectionHandler(and(capture(capturedDocumentSelectionHandler), isA(SelectionHandler.class)))).andReturn(createMock(HandlerRegistration.class));
      setupMockEventBus(true);
      capturedCheckboxChangeHandler = new Capture<ValueChangeHandler<Boolean>>();
      expect(mockExactSearchCheckbox.addValueChangeHandler(and(capture(capturedCheckboxChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      capturedTextboxChangeHandler = new Capture<ValueChangeHandler<String>>();
      expect(mockCaseSensitiveCheckbox.addValueChangeHandler(and(capture(capturedCaseSensitiveCheckboxChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockFilterTextbox.addValueChangeHandler(and(capture(capturedTextboxChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      setupMockHistory("");
      expect(mockWorkspaceContext.getWorkspaceId()).andReturn(new WorkspaceId(new ProjectIterationId(testProjectSlug, testIterationSlug), new LocaleId(testLocaleId))).anyTimes();
      expect(mockWindowLocation.getParameterMap()).andReturn(windowLocationParameters).anyTimes();
      expect(mockWindowLocation.getQueryDocuments()).andReturn(windowLocationParameters.get("doc")).anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockHistory(String tokenToReturn)
   {
      capturedHistoryValueChangeHandler = new Capture<ValueChangeHandler<String>>();
      expect(mockHistory.addValueChangeHandler(and(capture(capturedHistoryValueChangeHandler), isA(ValueChangeHandler.class)))).andReturn(createMock(HandlerRegistration.class)).anyTimes();
      expect(mockHistory.getToken()).andReturn(tokenToReturn).anyTimes();
      expect(mockHistory.getHistoryToken()).andReturn(HistoryToken.fromTokenString(tokenToReturn)).anyTimes();
      mockHistory.fireCurrentHistoryState();
      expectLastCall().anyTimes();

      capturedHistoryTokenString = new Capture<String>();
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
      capturedTransUnitUpdatedEventHandler = new Capture<TransUnitUpdatedEventHandler>();
      expect(mockEventBus.addHandler(eq(TransUnitUpdatedEvent.getType()), and(capture(capturedTransUnitUpdatedEventHandler), isA(TransUnitUpdatedEventHandler.class)))).andReturn(createMock(HandlerRegistration.class)).once();
      expect(mockEventBus.addHandler((GwtEvent.Type<EventHandler>) notNull(), (EventHandler) notNull())).andReturn(createMock(HandlerRegistration.class)).anyTimes();

      capturedEventBusEvent = new Capture<GwtEvent>(CaptureType.ALL);
      mockEventBus.fireEvent(and(capture(capturedEventBusEvent), isA(GwtEvent.class)));
      expectLastCall().anyTimes();
   }

   @SuppressWarnings("unchecked")
   private void setupMockDispatcher(IAnswer<? extends Object> docListRequestAnswer)
   {
      capturedDocListRequest = new Capture<GetDocumentList>();
      capturedDocListRequestCallback = new Capture<AsyncCallback<GetDocumentListResult>>();
      mockDispatcher.execute(and(capture(capturedDocListRequest), isA(Action.class)), and(capture(capturedDocListRequestCallback), isA(AsyncCallback.class)));
      expectLastCall().andAnswer(docListRequestAnswer);
   }

   @SuppressWarnings("unchecked")
   private void setupMockDisplay()
   {
      capturedPageSize = new Capture<Integer>();
      mockDisplay.setPageSize(captureInt(capturedPageSize));

      expectLastCall().anyTimes();

      mockDisplay.renderTable();

      expect(mockDisplay.getDataProvider()).andReturn(mockDataProvider).anyTimes();
      expect(mockDisplay.getDocumentList()).andReturn(mockDocList).anyTimes();
      expect(mockDisplay.getFilterTextBox()).andReturn(mockFilterTextbox).anyTimes();
      expect(mockDisplay.getCaseSensitiveCheckbox()).andReturn(mockCaseSensitiveCheckbox).anyTimes();
      expect(mockDisplay.getExactSearchCheckbox()).andReturn(mockExactSearchCheckbox).anyTimes();
      expect(mockDisplay.getDocumentListTable()).andReturn(mockDocListTable).anyTimes();
   }

   private void resetAllMocks()
   {
      reset(mockCaseSensitiveCheckbox);
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
      replay(mockCaseSensitiveCheckbox);
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
      verify(mockCaseSensitiveCheckbox);
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
         GetDocumentListResult result = new GetDocumentListResult(new ProjectIterationId(testProjectSlug, testIterationSlug), docsToReturn);
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
