package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.zanata.model.TestFixture.*;
import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.BookmarkedTextFlowEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.InitEditorEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class HistoryEventHandlerServiceTest
{
   private HistoryEventHandlerService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private DocumentListPresenter documentListPresenter;
   @Mock
   private AppPresenter appPresenter;
   @Mock
   private SearchResultsPresenter searchResultsPresenter;
   @Captor
   private ArgumentCaptor<GwtEvent> eventCaptor;
   @Mock
   private ValueChangeEvent<String> historyChangeEvent;
   @Mock
   private ModalNavigationStateHolder stateHolder;
   private GetTransUnitActionContextHolder contextHolder;
   private UserConfigHolder configHolder;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      contextHolder = new GetTransUnitActionContextHolder(new UserConfigHolder());
      configHolder = new UserConfigHolder();
      service = new HistoryEventHandlerService(eventBus, documentListPresenter, appPresenter, searchResultsPresenter, contextHolder, stateHolder, configHolder);
   }

   @Test
   public void onProcessForDocumentListPresenterWillDoNothingIfHistoryNotChanged()
   {
      HistoryToken token = new HistoryToken();

      service.processForDocumentListPresenter(token);

      verifyZeroInteractions(documentListPresenter);
   }

   @Test
   public void onProcessForDocumentListPresenter()
   {
      HistoryToken token = new HistoryToken();
      token.setDocFilterCaseSensitive(false);
      token.setDocFilterExact(true);
      token.setDocFilterText("something");

      service.processForDocumentListPresenter(token);

      verify(documentListPresenter).updateFilterAndRun(token.getDocFilterText(), token.getDocFilterExact(), token.isDocFilterCaseSensitive());
   }

   @Test
   public void onProcessForAppPresenterWillDoNothingIfHistoryNotChanged()
   {
      service.processForAppPresenter(null);

      verifyZeroInteractions(appPresenter);
   }

   @Test
   public void onProcessForAppPresenter()
   {
      HistoryToken token = new HistoryToken();
      token.setDocumentPath("doc/a.po");
      DocumentInfo document = TestFixture.documentInfo(99, "doc/a.po");
      when(documentListPresenter.getDocumentId("doc/a.po")).thenReturn(document.getId());
      when(documentListPresenter.getDocumentInfo(document.getId())).thenReturn(document);

      service.processForAppPresenter(document.getId());

      verify(appPresenter).selectDocument(document.getId());
      verify(eventBus).fireEvent(eventCaptor.capture());
      DocumentSelectionEvent documentSelectionEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), DocumentSelectionEvent.class);
      assertThat(documentSelectionEvent.getDocumentId(), Matchers.equalTo(document.getId()));

   }

   @Test
   public void onProcessTransFilterWillDoNothingIfHistoryNotChanged()
   {
      HistoryToken token = new HistoryToken();

      service.processForTransFilter(token);

      verifyZeroInteractions(eventBus);
   }

   @Test
   public void onProcessTransFilter()
   {
      HistoryToken token = new HistoryToken();
      token.setSearchText("something");

      service.processForTransFilter(token);

      verify(eventBus).fireEvent(eventCaptor.capture());
      FindMessageEvent findMessageEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), FindMessageEvent.class);
      assertThat(findMessageEvent.getMessage(), Matchers.equalTo("something"));

   }

   @Test
   public void onProcessForProjectWideSearchWillDoNothingIfHistoryNotChanged()
   {
      HistoryToken token = new HistoryToken();

      service.processForProjectWideSearch(token);

      verifyZeroInteractions(searchResultsPresenter);
   }

   @Test
   public void onProcessForProjectWideSearchWithoutReplacementTextChange()
   {
      HistoryToken token = new HistoryToken();
      token.setProjectSearchCaseSensitive(true);
      token.setProjectSearchInSource(false);
      token.setProjectSearchInTarget(true);
      token.setProjectSearchText("something");

      service.processForProjectWideSearch(token);

      verify(searchResultsPresenter).updateViewAndRun("something", token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      verifyNoMoreInteractions(searchResultsPresenter);
   }

   @Test
   public void onProcessForProjectWideSearchWithReplacementTextChange()
   {
      HistoryToken token = new HistoryToken();
      token.setProjectSearchCaseSensitive(true);
      token.setProjectSearchInSource(false);
      token.setProjectSearchInTarget(true);
      token.setProjectSearchText("something");
      token.setProjectSearchReplacement("something else");

      service.processForProjectWideSearch(token);

      verify(searchResultsPresenter).updateViewAndRun("something", token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      verify(searchResultsPresenter).updateReplacementText("something else");
   }

   @Test
   public void processHistoryTokenWithInitializedContext()
   {
      // Given: history token contains everything and editor context is loaded
      HistoryToken token = new HistoryToken();
      token.setDocFilterText("doc filter test");
      token.setSearchText("search text");
      token.setProjectSearchText("project search text");
      token.setDocumentPath("doc/path");
      token.setProjectSearchReplacement("replacement");
      when(historyChangeEvent.getValue()).thenReturn(token.toTokenString());
      DocumentInfo documentInfo = TestFixture.documentInfo(1, "doc/path");
      DocumentId documentId = documentInfo.getId();
      when(documentListPresenter.getDocumentId("doc/path")).thenReturn(documentId);
      when(documentListPresenter.getDocumentInfo(documentId)).thenReturn(documentInfo);
      when(appPresenter.getSelectedDocIdOrNull()).thenReturn(new DocumentId(new Long(99), ""));
      contextHolder.updateContext(new GetTransUnitActionContext(documentInfo(99, "")));

      // When:
      service.onValueChange(historyChangeEvent);

      // Then:
      InOrder inOrder = Mockito.inOrder(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
      inOrder.verify(documentListPresenter).updateFilterAndRun(token.getDocFilterText(), token.getDocFilterExact(), token.isDocFilterCaseSensitive());
      inOrder.verify(searchResultsPresenter).updateViewAndRun(token.getProjectSearchText(), token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      inOrder.verify(searchResultsPresenter).updateReplacementText(token.getProjectSearchReplacement());
      inOrder.verify(documentListPresenter).getDocumentId(token.getDocumentPath());
      inOrder.verify(appPresenter).getSelectedDocIdOrNull();
      inOrder.verify(appPresenter).selectDocument(documentId);
      inOrder.verify(documentListPresenter).getDocumentInfo(documentId);
      inOrder.verify(eventBus).fireEvent(Mockito.isA(DocumentSelectionEvent.class));
      inOrder.verify(eventBus).fireEvent(Mockito.isA(FindMessageEvent.class));
      inOrder.verify(appPresenter).showView(token.getView());

      verifyNoMoreInteractions(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
   }

   @Test
   public void processHistoryTokenForUninitializedContext()
   {
      // Given: history token contains everything but editor context is not initialized
      HistoryToken token = new HistoryToken();
      token.setDocFilterText("doc filter test");
      token.setSearchText("search text");
      token.setProjectSearchText("project search text");
      token.setDocumentPath("doc/path");
      token.setProjectSearchReplacement("replacement");
      token.setTextFlowId("1");
      when(historyChangeEvent.getValue()).thenReturn(token.toTokenString());
      DocumentInfo documentInfo = TestFixture.documentInfo(1, "doc/path");
      DocumentId documentId = documentInfo.getId();
      when(documentListPresenter.getDocumentId("doc/path")).thenReturn(documentId);
      when(documentListPresenter.getDocumentInfo(documentId)).thenReturn(documentInfo);
      when(appPresenter.getSelectedDocIdOrNull()).thenReturn(new DocumentId(new Long(99), ""));
      contextHolder.updateContext(null);

      // When:
      service.onValueChange(historyChangeEvent);

      // Then:
      InOrder inOrder = Mockito.inOrder(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
      inOrder.verify(documentListPresenter).updateFilterAndRun(token.getDocFilterText(), token.getDocFilterExact(), token.isDocFilterCaseSensitive());
      inOrder.verify(searchResultsPresenter).updateViewAndRun(token.getProjectSearchText(), token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      inOrder.verify(searchResultsPresenter).updateReplacementText(token.getProjectSearchReplacement());

      inOrder.verify(documentListPresenter).getDocumentId(token.getDocumentPath());
      inOrder.verify(documentListPresenter).getDocumentInfo(documentId);
      inOrder.verify(eventBus).fireEvent(Mockito.isA(InitEditorEvent.class));
      inOrder.verify(appPresenter).getSelectedDocIdOrNull();
      inOrder.verify(appPresenter).selectDocument(documentId);
      inOrder.verify(documentListPresenter).getDocumentInfo(documentId);
      inOrder.verify(eventBus).fireEvent(Mockito.isA(DocumentSelectionEvent.class));
      inOrder.verify(eventBus).fireEvent(Mockito.isA(FindMessageEvent.class));
      inOrder.verify(appPresenter).showView(token.getView());

      verifyNoMoreInteractions(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
   }

   @Test
   public void processBookmarkedTextFlowWhenEditorIsNotInitialized()
   {
      // Given: editor is not initialized yet
      HistoryToken token = new HistoryToken();
      token.setTextFlowId("1");

      // When:
      service.processForBookmarkedTextFlow(token);

      // Then:
      verifyZeroInteractions(eventBus);
   }

   @Test
   public void processBookmarkedTextFlowWhenThereIsNoTextFlowInHistoryUrl()
   {
      // Given: no text flow
      HistoryToken token = new HistoryToken();

      // When:
      service.processForBookmarkedTextFlow(token);

      // Then:
      verifyZeroInteractions(eventBus);

   }

   @Test
   public void processBookmarkedTextFlowWithInvalidTextFlowId()
   {
      // Given: text flow not in current document
      HistoryToken token = new HistoryToken();
      token.setTextFlowId("111");
      when(stateHolder.getPageCount()).thenReturn(10);
      when(stateHolder.getTargetPage(new TransUnitId(111))).thenReturn(NavigationService.UNDEFINED);

      // When:
      service.processForBookmarkedTextFlow(token);

      // Then:
      verifyZeroInteractions(eventBus);
   }

   @Test
   public void processBookmarkedTextFlow()
   {
      // Given: everything works
      contextHolder.updateContext(new GetTransUnitActionContext(TestFixture.documentInfo(99, "")));
      HistoryToken token = new HistoryToken();
      token.setTextFlowId("111");
      when(stateHolder.getPageCount()).thenReturn(10);
      when(stateHolder.getTargetPage(new TransUnitId(111))).thenReturn(2);

      // When:
      service.processForBookmarkedTextFlow(token);

      // Then:
      verify(eventBus).fireEvent(Mockito.isA(BookmarkedTextFlowEvent.class));
   }

   @Test
   public void processMessageFilterOptions()
   {
      HistoryToken token = new HistoryToken();
      token.setFilterUntranslated(true);

      service.processMessageFilterOptions(token);

      verify(eventBus).fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      verify(eventBus).fireEvent(Mockito.isA(FilterViewEvent.class));
   }

}
