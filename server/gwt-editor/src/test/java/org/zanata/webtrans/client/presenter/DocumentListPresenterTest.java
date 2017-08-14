package org.zanata.webtrans.client.presenter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.webtrans.test.GWTTestData.extractFromEvents;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics.StatUnit;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEventHandler;
import org.zanata.webtrans.client.events.RefreshProjectStatsEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.view.DocumentListDisplay;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.ValidationAction.State;
import org.zanata.webtrans.shared.model.ValidationId;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetDocumentStats;
import org.zanata.webtrans.shared.rpc.GetDocumentStatsResult;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;
import org.zanata.webtrans.shared.rpc.ThemesOption;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class DocumentListPresenterTest {
    // field for document list presenter under test
    private DocumentListPresenter documentListPresenter;

    // mocks for interacting classes
    @Mock
    private DocumentListDisplay mockDisplay;
    @Mock
    private EventBus mockEventBus;
    @Mock
    private History mockHistory;
    @Mock
    private WebTransMessages mockMessages;
    @Mock
    private UserWorkspaceContext mockUserWorkspaceContext;
    @Mock
    private WorkspaceContext mockWorkspaceContext;
    @Mock
    private UserOptionsService mockUserOptionsService;
    @Mock
    private CachingDispatchAsync mockDispatcher;
    @Mock
    private HasPager mockPager;

    private UserConfigHolder configHolder;

    // captured events and handlers used in several tests
    @Captor
    private ArgumentCaptor<GwtEvent<DocumentStatsUpdatedEventHandler>>
            capturedEventBusEvent;
    @Captor
    private ArgumentCaptor<String> capturedHistoryTokenString;
    @Captor
    private ArgumentCaptor<HistoryToken> capturedHistoryToken;
    @Captor
    private ArgumentCaptor<AsyncCallback<GetDocumentStatsResult>> asyncCallbackArgumentCaptor;

    private static final String TEST_BY_WORDS_MESSAGE = "By Words";
    private static final String TEST_BY_MESSAGE_MESSAGE = "By Messages";

    private WorkspaceId workspaceId;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        configHolder = new UserConfigHolder();
        when(mockUserOptionsService.getConfigHolder()).thenReturn(configHolder);

        workspaceId =
                new WorkspaceId(new ProjectIterationId("projectSlug",
                        "iterationSlug", ProjectType.Podir), LocaleId.ES);

        when(mockUserWorkspaceContext.getWorkspaceContext()).thenReturn(
                mockWorkspaceContext);
        when(mockWorkspaceContext.getWorkspaceId()).thenReturn(workspaceId);

        when(mockMessages.projectTypeNotSet()).thenReturn("Project not set");
        when(mockMessages.downloadAllAsZipDescription()).thenReturn(
                "Download all translation file");

        when(mockDisplay.getPageNavigation()).thenReturn(mockPager);

        documentListPresenter =
                new DocumentListPresenter(mockDisplay, mockEventBus,
                        mockDispatcher, mockUserWorkspaceContext, mockMessages,
                        mockHistory, mockUserOptionsService);
    }

    @Test
    public void onBind() {
        when(mockMessages.byWords()).thenReturn(TEST_BY_WORDS_MESSAGE);
        when(mockMessages.byMessage()).thenReturn(TEST_BY_MESSAGE_MESSAGE);

        documentListPresenter.onBind();

        // verify(mockDisplay).renderTable(isA(NoSelectionModel.class));
        // verify(mockDisplay).setStatsFilter("Words");
        // verify(mockDisplay).updatePageSize(UserConfigHolder.DEFAULT_DOC_LIST_PAGE_SIZE);
        verify(mockDisplay).setListener(documentListPresenter);
        verify(mockEventBus).addHandler(DocumentSelectionEvent.getType(),
                documentListPresenter);
        verify(mockEventBus).addHandler(TransUnitUpdatedEvent.getType(),
                documentListPresenter);
        verify(mockEventBus).addHandler(UserConfigChangeEvent.TYPE,
                documentListPresenter);
        verify(mockEventBus).addHandler(WorkspaceContextUpdateEvent.getType(),
                documentListPresenter);

        verify(mockDisplay).setEnableDownloadZip(
                documentListPresenter.isZipFileDownloadAllowed(workspaceId
                        .getProjectIterationId().getProjectType()));
        verify(mockDisplay).setDownloadZipButtonTitle(isA(String.class));
    }

    @Test
    public void loadDocsIntoDataProvider() {
        documentListPresenter.bind();
        documentListPresenter.setDocuments(buildSampleDocumentArray());

        // right amount of docs
        assertThat(documentListPresenter.getFilteredNodes()).hasSize(3)
                .as("the data provider should have the same sized document list returned from the server");

        ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();

        ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
        for (DocumentNode node : documentListPresenter.getFilteredNodes()) {
            assertThat(node.getDocInfo()).isIn(expectedDocs)
                    .as("the data provider should have only documents that were returned from the server");
            actualDocInfos.add(node.getDocInfo());
        }
        assertThat(actualDocInfos)
                .contains(expectedDocs.get(0), expectedDocs.get(1),
                        expectedDocs.get(2))
                .as("the data provider should have all documents returned from the server");
    }

    @Test
    public void generatesDocumentStatsOnTuUpdate() {
        ArrayList<String> sources = new ArrayList<String>();
        sources.add("this is the source");
        boolean plural = false;

        ArrayList<String> targets = new ArrayList<String>();
        targets.add("this is the target");

        TransUnit newTransUnit =
                TransUnit.Builder.newTransUnitBuilder().setId(12345L)
                        .setResId("resId").setLocaleId("es").setPlural(plural)
                        .setSources(sources)
                        .setSourceComment("this is the source comment")
                        .setTargets(targets).setStatus(ContentState.Approved)
                        .setLastModifiedBy("lastModifiedBy")
                        .setLastModifiedTime(new Date())
                        .setMsgContext("msgContext")
                        .setSourceRefs("srcRef")
                        .setSourceFlags("srcFlag")
                        .setRowIndex(1)
                        .setVerNum(1).build();
        TransUnitUpdateInfo updateInfo =
                new TransUnitUpdateInfo(true, true, new DocumentId(2222L,
                        "match/exact/filter"), newTransUnit, 3, 0,
                        ContentState.NeedReview);
        TransUnitUpdatedEvent mockEvent = mock(TransUnitUpdatedEvent.class);

        when(mockEvent.getUpdateInfo()).thenReturn(updateInfo);

        documentListPresenter.bind();
        documentListPresenter.setDocuments(buildSampleDocumentArray());
        documentListPresenter.onTransUnitUpdated(mockEvent);

        verify(mockEventBus, times(2)).fireEvent(
                capturedEventBusEvent.capture());

        DocumentStatsUpdatedEvent docStatsEvent =
                extractFromEvents(capturedEventBusEvent.getAllValues(),
                        DocumentStatsUpdatedEvent.class);

        assertThat(docStatsEvent).isNotNull()
                .as("a document stats event should be fired when a TU update event occurs, not found");

        // document stats
        assertThat(docStatsEvent.getDocId())
                .isEqualTo(new DocumentId(2222L, ""))
                .as("document id in document stats event shoudl match updated TU document id");

        // check actual counts (approved/fuzzy/untranslated)
        // default TUs: 1/2/3
        // approving 1 fuzzy, expect 2/1/3
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.MESSAGE)
                .getApproved()).isEqualTo(new Long(2))
                .as("document Approved TU count should increase by 1 when a TU is updated from NeedsReview to Approved");
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.MESSAGE).getDraft())
                .isEqualTo(new Long(1));
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.MESSAGE)
                .getUntranslated()).isEqualTo(new Long(3))
                .as("document Untranslated TU count should remain the same when a TU is updated from NeedsReview to Approved");

        // default words: 4/5/6
        // approving 3 fuzzy so expect 7/2/6
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.WORD).getApproved())
                .isEqualTo(new Long(7))
                .as("document Approved words should increase when TU changes to Approved");
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.WORD).getDraft())
                .isEqualTo(new Long(2))
                .as("document NeedsReview words should decrease when a TU changes from NeedsReview");
        assertThat(docStatsEvent.getNewStats()
                .getStats(LocaleId.ES.toString(), StatUnit.WORD).getDraft())
                .isEqualTo(new Long(2))
                .as("document Untranslated words should not change when TU changes between NeedsReview and Approved");
    }

    @Test
    public void filterTextUpdateGeneratesHistoryToken() {
        String filterText = "path/doc12";
        // these seem to persist beyond verify, so setting them up here is fine

        documentListPresenter.bind();
        documentListPresenter.fireFilterToken(filterText);
        documentListPresenter.fireExactSearchToken(false);

        verify(mockHistory).newItem(capturedHistoryTokenString.capture());

        HistoryToken capturedHistoryToken =
                HistoryToken.fromTokenString(capturedHistoryTokenString
                        .getValue());
        assertThat(capturedHistoryToken.getDocFilterText())
                .isEqualTo(filterText)
                .as("generated history token filter text should match the filter textbox");
        assertThat(capturedHistoryToken.getDocFilterExact()).isFalse()
                .as("generated history token filter exact flag should match the exact match checkbox");
    }

    @Test
    public void checkExactSearchCheckboxGeneratesHistoryToken() {
        documentListPresenter.bind();

        // simulate checking 'exact search' checkbox
        documentListPresenter.fireExactSearchToken(true);

        verify(mockHistory).newItem(capturedHistoryTokenString.capture());

        HistoryToken exactSearchToken = new HistoryToken();
        exactSearchToken.setDocFilterExact(true);
        assertThat(capturedHistoryTokenString.getValue())
                .isEqualTo(exactSearchToken.toTokenString())
                .as("checking the 'exact search' checkbox should be reflected in a new history token");
    }

    @Test
    public void uncheckExactSearchCheckboxGeneratesHistoryToken() {
        // history reflects checkbox already checked
        HistoryToken exactSearchToken = new HistoryToken();
        exactSearchToken.setDocFilterExact(true);

        when(mockHistory.getToken()).thenReturn(
                exactSearchToken.toTokenString());

        documentListPresenter.bind();
        documentListPresenter.fireExactSearchToken(false);

        verify(mockHistory).newItem(capturedHistoryTokenString.capture());

        HistoryToken inexactSearchToken = new HistoryToken();
        inexactSearchToken.setDocFilterExact(false);
        assertThat(capturedHistoryTokenString.getValue())
                .isEqualTo(inexactSearchToken.toTokenString())
                .as("unchecking the 'exact search' checkbox should be reflected in a new history token");
    }

    // TODO tests for check and uncheck case sensitive check

    @Test
    public void documentSelectUpdatesHistoryToken() {
        HistoryToken documentPathToken = new HistoryToken();
        documentPathToken.setDocumentPath(null);
        when(mockHistory.getHistoryToken()).thenReturn(documentPathToken);

        documentListPresenter.bind();

        // simulate document click on second document
        DocumentInfo docInfo =
                new DocumentInfo(new DocumentId(2222L, ""), "doc122",
                        "second/path/", LocaleId.EN_US,
                        new ContainerTranslationStatistics(), new AuditInfo(
                                new Date(), "Translator"),
                        new HashMap<String, String>(), new AuditInfo(
                                new Date(), "last translator"));
        documentListPresenter.fireDocumentSelection(docInfo);

        verify(mockHistory).newItem(capturedHistoryToken.capture());
        verify(mockUserWorkspaceContext).setSelectedDoc(docInfo);

        HistoryToken newToken = capturedHistoryToken.getValue();
        assertThat(newToken.getDocumentPath()).isEqualTo("second/path/doc122")
                .as("path of selected document should be set in history token");
        assertThat(newToken.getView()).isEqualTo(MainView.Editor)
                .as("view in history token should change to individual document view when a new document is selected");
    }

    @Test
    public void exactSearchMatchesExactOnly() {
        // should match 1 of the 3 sample documents
        String filterText = "match/exact/filter";

        documentListPresenter.bind();
        documentListPresenter.setDocuments(buildSampleDocumentArray());
        documentListPresenter.updateFilterAndRun(filterText, true, false);

        verify(mockDisplay).updateFilter(false, true, filterText);

        // simulate firing history change event
        HistoryToken historyTokenWithExactFilter = new HistoryToken();
        historyTokenWithExactFilter.setDocFilterText(filterText);
        historyTokenWithExactFilter.setDocFilterExact(true);

        ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
        // third doc does not match the filter
        expectedDocs.remove(2);
        // first doc does not match the filter
        expectedDocs.remove(0);
        ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
        for (DocumentNode node : documentListPresenter.getFilteredNodes()) {
            assertThat(node.getDocInfo()).isIn(expectedDocs)
                    .as("the data provider should have only documents that exactly match the current filter");
            actualDocInfos.add(node.getDocInfo());
        }
        assertThat(actualDocInfos).contains(expectedDocs.get(0))
                .as("the data provider should have all documents that exactly match the filter");
        assertThat(documentListPresenter.getFilteredNodes()).hasSize(1)
                .as("the data provider list should contain exactly the number of documents matching the filter");
    }

    // TODO test case sensitivity option

    @Test
    public void commaSeparatedFilter() {
        // should match first and last of the 3 sample documents
        // multiple matching strings for third to check that there is no
        // duplication, also variable whitespace
        String filterText = " does/not, not/match ,no/filter ";

        documentListPresenter.bind();
        documentListPresenter.updateFilterAndRun(filterText, false, false);
        documentListPresenter.setDocuments(buildSampleDocumentArray());

        // simulate firing history change event
        HistoryToken historyTokenWithFilter = new HistoryToken();
        historyTokenWithFilter.setDocFilterText(filterText);

        verify(mockDisplay).updateFilter(false, false, filterText);

        ArrayList<DocumentInfo> expectedDocs = buildSampleDocumentArray();
        // second doc does not match any of the filter strings
        ArrayList<DocumentInfo> actualDocInfos = new ArrayList<DocumentInfo>();
        expectedDocs.remove(1);
        for (DocumentNode node : documentListPresenter.getFilteredNodes()) {
            assertThat(node.getDocInfo()).isIn(expectedDocs)
                    .as("the data provider should have only documents that match the current filter");
            actualDocInfos.add(node.getDocInfo());
        }
        assertThat(actualDocInfos)
                .contains(expectedDocs.get(0), expectedDocs.get(1))
                .as("the data provider should have all documents that match the filter");
        assertThat(documentListPresenter.getFilteredNodes()).hasSize(2)
                .as("the data provider list should contain exactly the number of documents matching the filter");
    }

    // TODO test case sensitive check updated from history

    // TODO test: update selected document when different from doc selection
    // event, as in DocumentListPresenter.setSelection()

    @Test
    public void getDocumentId() {
        documentListPresenter.bind();
        documentListPresenter.setDocuments(buildSampleDocumentArray());

        // third document from buildSampleDocumentArray()
        DocumentId docId =
                documentListPresenter
                        .getDocumentId("does/not/match/exact/filter");
        assertThat(docId.getId()).isEqualTo(3333L);

        // second document from buildSampleDocumentArray()
        docId = documentListPresenter.getDocumentId("match/exact/filter");
        assertThat(docId.getId()).isEqualTo(2222L);
    }

    @Test
    public void getDocumentInfo() {
        documentListPresenter.bind();
        documentListPresenter.setDocuments(buildSampleDocumentArray());

        DocumentId doc1 = new DocumentId(1111L, "no/filter/matches");
        DocumentInfo docInfo = documentListPresenter.getDocumentInfo(doc1);

        assertThat(docInfo).isEqualTo(new DocumentInfo(doc1, "doc111",
                "first/path/", LocaleId.EN_US,
                new ContainerTranslationStatistics(), new AuditInfo(new Date(),
                        "Translator"), new HashMap<String, String>(),
                new AuditInfo(new Date(), "last translator")));

        DocumentId doc2 = new DocumentId(3333L, "does/not/match/exact/filter");
        docInfo = documentListPresenter.getDocumentInfo(doc2);
        assertThat(docInfo).isEqualTo(new DocumentInfo(doc2, "doc123",
                "third/path/", LocaleId.EN_US,
                new ContainerTranslationStatistics(), new AuditInfo(new Date(),
                        "Translator"), new HashMap<String, String>(),
                new AuditInfo(new Date(), "last translator")));
    }

    @Test
    public void onUserConfigChangedDocument() {
        UserConfigChangeEvent mockEvent = mock(UserConfigChangeEvent.class);
        when(mockEvent.getView()).thenReturn(MainView.Documents);

        documentListPresenter.onUserConfigChanged(mockEvent);

        // verify(mockDisplay).updatePageSize(UserConfigHolder.DEFAULT_DOC_LIST_PAGE_SIZE);
    }

    @Test
    public void onUserConfigChangedEditor() {
        UserConfigChangeEvent mockEvent = mock(UserConfigChangeEvent.class);
        when(mockEvent.getView()).thenReturn(MainView.Editor);

        documentListPresenter.onUserConfigChanged(mockEvent);

        verify(mockDisplay).setLayout(ThemesOption.THEMES_DEFAULT.name());

        verifyZeroInteractions(mockDisplay);
    }

    @Test
    public void onWorkspaceContextUpdated() {
        WorkspaceContextUpdateEvent event =
                new WorkspaceContextUpdateEvent(workplaceContextData(true,
                        ProjectType.Gettext));
        documentListPresenter.onWorkspaceContextUpdated(event);

        verify(mockDisplay).setEnableDownloadZip(
                documentListPresenter.isZipFileDownloadAllowed(event
                        .getProjectType()));
        verify(mockDisplay).setDownloadZipButtonTitle(isA(String.class));
    }

    @Test
    public void queryStats() {
        ArrayList<DocumentInfo> documentInfoList = buildSampleDocumentArray();
        ArrayList<DocumentNode> sortedNodes = new ArrayList<DocumentNode>();
        TreeMap<DocumentId, DocumentNode> nodes =
                new TreeMap<DocumentId, DocumentNode>();
        HashMap<DocumentId, ContainerTranslationStatistics> statMap =
                new HashMap<DocumentId, ContainerTranslationStatistics>();
        HashMap<DocumentId, AuditInfo> lastTranslatedMap =
                new HashMap<DocumentId, AuditInfo>();

        for (DocumentInfo docInfo : documentInfoList) {
            DocumentNode node = new DocumentNode(docInfo);
            nodes.put(docInfo.getId(), node);
            sortedNodes.add(node);
            statMap.put(docInfo.getId(), new ContainerTranslationStatistics());
        }
        documentListPresenter.setStatesForTest(sortedNodes, nodes);

        GetDocumentStatsResult result =
                new GetDocumentStatsResult(statMap, lastTranslatedMap);

        documentListPresenter.queryStats();

        ArgumentCaptor<GetDocumentStats> actionCaptor =
                ArgumentCaptor.forClass(GetDocumentStats.class);

        verify(mockDispatcher).execute(actionCaptor.capture(),
                asyncCallbackArgumentCaptor.capture());

        AsyncCallback<GetDocumentStatsResult> callback = asyncCallbackArgumentCaptor.getValue();
        callback.onSuccess(result);

        verify(mockEventBus, times(3)).fireEvent(
                isA(DocumentStatsUpdatedEvent.class));
        verify(mockEventBus, times(1)).fireEvent(
                isA(RefreshProjectStatsEvent.class));
    }

    private static HasWorkspaceContextUpdateData workplaceContextData(
            final boolean projectActive, final ProjectType projectType) {
        return new HasWorkspaceContextUpdateData() {
            @Override
            public boolean isProjectActive() {
                return projectActive;
            }

            @Override
            public ProjectType getProjectType() {
                return projectType;
            }

            @Override
            public Map<ValidationId, State> getValidationStates() {
                return null;
            }

            @Override
            public String getOldProjectSlug() {
                return null;
            }

            @Override
            public String getNewProjectSlug() {
                return null;
            }

            @Override
            public String getOldIterationSlug() {
                return null;
            }

            @Override
            public String getNewIterationSlug() {
                return null;
            }
        };
    }

    private ArrayList<DocumentInfo> buildSampleDocumentArray() {
        ArrayList<DocumentInfo> docList = new ArrayList<DocumentInfo>();

        TransUnitCount unitCount = new TransUnitCount(1, 2, 3);
        TransUnitWords wordCount = new TransUnitWords(4, 5, 6);

        ContainerTranslationStatistics stats =
                new ContainerTranslationStatistics();
        stats.addStats(new TranslationStatistics(unitCount, LocaleId.ES
                .toString()));
        stats.addStats(new TranslationStatistics(wordCount, LocaleId.ES
                .toString()));

        DocumentInfo docInfo =
                new DocumentInfo(new DocumentId(1111L, "no/filter/matches"),
                        "matches", "no/filter", LocaleId.EN_US, stats,
                        new AuditInfo(new Date(), "Translator"),
                        new HashMap<String, String>(), new AuditInfo(
                                new Date(), "last translator"));
        docList.add(docInfo);

        docInfo =
                new DocumentInfo(new DocumentId(2222L, "match/exact/filter"),
                        "filter", "match/exact/", LocaleId.EN_US, stats,
                        new AuditInfo(new Date(), "Translator"),
                        new HashMap<String, String>(), new AuditInfo(
                                new Date(), "last translator"));
        docList.add(docInfo);

        docInfo =
                new DocumentInfo(new DocumentId(3333L,
                        "does/not/match/exact/filter"), "filter",
                        "does/not/match/exact/", LocaleId.EN_US, stats,
                        new AuditInfo(new Date(), "Translator"),
                        new HashMap<String, String>(), new AuditInfo(
                                new Date(), "last translator"));
        docList.add(docInfo);

        return docList;
    }
}
