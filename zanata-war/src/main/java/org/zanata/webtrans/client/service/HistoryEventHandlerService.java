package org.zanata.webtrans.client.service;

import static com.google.common.base.Objects.equal;
import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.BookmarkableTextFlowEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class HistoryEventHandlerService implements ValueChangeHandler<String>
{

   private final EventBus eventBus;
   private final DocumentListPresenter documentListPresenter;
   private final AppPresenter appPresenter;
   private final SearchResultsPresenter searchResultsPresenter;
   // initial state
   private HistoryToken currentHistoryState = new HistoryToken();

   @Inject
   public HistoryEventHandlerService(EventBus eventBus, DocumentListPresenter documentListPresenter, AppPresenter appPresenter, SearchResultsPresenter searchResultsPresenter)
   {
      this.eventBus = eventBus;
      this.documentListPresenter = documentListPresenter;
      this.appPresenter = appPresenter;
      this.searchResultsPresenter = searchResultsPresenter;
   }

   @Override
   public void onValueChange(ValueChangeEvent<String> event)
   {
      HistoryToken newHistoryToken = HistoryToken.fromTokenString(event.getValue());
      Log.info("[gwt-history] Responding to history token: " + event.getValue());

      processForDocumentListPresenter(newHistoryToken);
      //AppPresenter process need to happen before transFilter. We want DocumentSelectionEvent to happen before FindMessageEvent.
      processForAppPresenter(newHistoryToken);
      processForTransFilter(newHistoryToken);
      processForProjectWideSearch(newHistoryToken);

      processForTransUnitSelection(newHistoryToken);

      currentHistoryState = newHistoryToken;
      appPresenter.showView(newHistoryToken.getView());
   }

   protected void processForDocumentListPresenter(HistoryToken token)
   {
      // @formatter:off
      if (!equal(token.getDocFilterExact(), currentHistoryState.getDocFilterExact())
            || !equal(token.getDocFilterText(), currentHistoryState.getDocFilterText())
            || !equal(token.isDocFilterCaseSensitive(), currentHistoryState.isDocFilterCaseSensitive()))
      // @formatter:on
      {
         Log.info("[gwt-history] document list filter has changed");
         documentListPresenter.updateFilterAndRun(token.getDocFilterText(), token.getDocFilterExact(), token.isDocFilterCaseSensitive());
      }
   }

   protected void processForAppPresenter(HistoryToken token)
   {
      DocumentId docId = documentListPresenter.getDocumentId(token.getDocumentPath());
      if (docId != null && !equal(appPresenter.getSelectedDocIdOrNull(), docId))
      {
         appPresenter.selectDocument(docId);
      }
      Log.info("[gwt-history] document id: " + docId);

      if (docId != null)
      {
         eventBus.fireEvent(new DocumentSelectionEvent(docId, token.getSearchText()));
      }
   }

   protected void processForTransFilter(HistoryToken newHistoryToken)
   {
      boolean findMessageChanged = !newHistoryToken.getSearchText().equals(currentHistoryState.getSearchText());
      if (findMessageChanged)
      {
         Log.info("[gwt-history] trans filter search has changed");
         eventBus.fireEvent(new FindMessageEvent(newHistoryToken.getSearchText()));
      }
   }

   protected void processForProjectWideSearch(HistoryToken token)
   {
      // @formatter:off
      if (!equal(token.getProjectSearchCaseSensitive(), currentHistoryState.getProjectSearchCaseSensitive())
            || !equal(token.getProjectSearchText(), currentHistoryState.getProjectSearchText())
            || !equal(token.isProjectSearchInSource(), currentHistoryState.isProjectSearchInSource())
            || !equal(token.isProjectSearchInTarget(), currentHistoryState.isProjectSearchInTarget()))
      // @formatter:on
      {
         Log.info("[gwt-history] project wide search condition has changed");

         searchResultsPresenter.updateViewAndRun(token.getProjectSearchText(), token.getProjectSearchCaseSensitive(), token.isProjectSearchInSource(), token.isProjectSearchInTarget());
      }

      boolean replacementTextChanged = !token.getProjectSearchReplacement().equals(currentHistoryState.getProjectSearchReplacement());
      if (replacementTextChanged)
      {
         Log.info("[gwt-history] project wide search replacement text has changed");
         searchResultsPresenter.updateReplacementText(token.getProjectSearchReplacement());
      }
   }

   protected void processForTransUnitSelection(HistoryToken token)
   {
      if (!equal(token.getTextFlowId(), currentHistoryState.getTextFlowId()) && token.getTextFlowId() != null)
      {
         Log.info("[gwt-history] bookmarkable text flow has changed");
         eventBus.fireEvent(new BookmarkableTextFlowEvent(new TransUnitId(token.getTextFlowId())));
      }
   }
}
