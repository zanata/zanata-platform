package org.zanata.webtrans.client.service;

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
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.shared.model.DocumentId;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.shared.GwtEvent;

import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
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

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      service = new HistoryEventHandlerService(eventBus, documentListPresenter, appPresenter, searchResultsPresenter);
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
      HistoryToken token = new HistoryToken();

      service.processForAppPresenter(token);

      verifyZeroInteractions(appPresenter);
   }

   @Test
   public void onProcessForAppPresenter()
   {
      HistoryToken token = new HistoryToken();
      token.setDocumentPath("doc/a.po");
      DocumentId documentId = new DocumentId(1);
      when(documentListPresenter.getDocumentId("doc/a.po")).thenReturn(documentId);

      service.processForAppPresenter(token);

      verify(appPresenter).selectDocument(documentId);
      verify(eventBus).fireEvent(eventCaptor.capture());
      DocumentSelectionEvent documentSelectionEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), DocumentSelectionEvent.class);
      assertThat(documentSelectionEvent.getDocumentId(), Matchers.equalTo(documentId));

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
   public void processHistoryToken()
   {
      // Given: history token contains everything
      HistoryToken token = new HistoryToken();
      token.setDocFilterText("doc filter test");
      token.setSearchText("search text");
      token.setProjectSearchText("project search text");
      token.setDocumentPath("doc/path");
      token.setProjectSearchReplacement("replacement");
      when(historyChangeEvent.getValue()).thenReturn(token.toTokenString());
      DocumentId documentId = new DocumentId(1);
      when(documentListPresenter.getDocumentId("doc/path")).thenReturn(documentId);
      when(appPresenter.getSelectedDocIdOrNull()).thenReturn(new DocumentId(99));

      // When:
      service.onValueChange(historyChangeEvent);

      // Then:
      InOrder inOrder = Mockito.inOrder(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
      inOrder.verify(documentListPresenter).updateFilterAndRun(token.getDocFilterText(), token.getDocFilterExact(), token.isDocFilterCaseSensitive());
      inOrder.verify(documentListPresenter).getDocumentId(token.getDocumentPath());
      inOrder.verify(appPresenter).getSelectedDocIdOrNull();
      inOrder.verify(appPresenter).selectDocument(documentId);
      inOrder.verify(eventBus).fireEvent(Mockito.isA(DocumentSelectionEvent.class));
      inOrder.verify(eventBus).fireEvent(Mockito.isA(FindMessageEvent.class));
      inOrder.verify(searchResultsPresenter).updateViewAndRun(token.getProjectSearchText(), token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      inOrder.verify(searchResultsPresenter).updateReplacementText(token.getProjectSearchReplacement());
      inOrder.verify(appPresenter).showView(token.getView());

      verifyNoMoreInteractions(documentListPresenter, appPresenter, eventBus, searchResultsPresenter);
   }

}
