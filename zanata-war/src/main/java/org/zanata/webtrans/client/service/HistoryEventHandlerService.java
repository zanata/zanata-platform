package org.zanata.webtrans.client.service;

import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.shared.model.DocumentId;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import static com.google.common.base.Objects.equal;

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

      currentHistoryState = newHistoryToken;
      appPresenter.showView(newHistoryToken.getView());
   }

   private void processForDocumentListPresenter(HistoryToken token)
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

   private void processForAppPresenter(HistoryToken token)
   {
      DocumentId docId = documentListPresenter.getDocumentId(token.getDocumentPath());
      if (!equal(appPresenter.getSelectedDocIdOrNull(), docId))
      {
         appPresenter.selectDocument(docId);
      }
      Log.info("[gwt-history] document id: " + docId);

      // if there is no valid document, don't show the editor
      // default to document list instead
      if (docId == null && token.getView() == MainView.Editor)
      {
         Log.warn("[gwt-history] access editor view with invalid document id. Showing document list view instead");
         token.setView(MainView.Documents);
      }

      if (docId != null)
      {
         eventBus.fireEvent(new DocumentSelectionEvent(docId, token.getSearchText()));
      }
   }

   private void processForTransFilter(HistoryToken newHistoryToken)
   {
      boolean findMessageChanged = !newHistoryToken.getSearchText().equals(currentHistoryState.getSearchText());
      if (findMessageChanged)
      {
         Log.info("[gwt-history] trans filter search has changed");
         eventBus.fireEvent(new FindMessageEvent(newHistoryToken.getSearchText()));
      }
   }

   private void processForProjectWideSearch(HistoryToken token)
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


}
