package org.zanata.webtrans.client.service;

import org.zanata.webtrans.client.events.BookmarkedTextFlowEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.InitEditorEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.AppPresenter;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter;
import org.zanata.webtrans.client.presenter.TransFilterPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import net.customware.gwt.presenter.client.EventBus;
import static com.google.common.base.Objects.*;

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
   private final GetTransUnitActionContextHolder getTransUnitActionContextHolder;
   private final ModalNavigationStateHolder modalStateHolder;
   private final UserConfigHolder configHolder;
   // initial state
   private HistoryToken currentHistoryState = new HistoryToken();

   @Inject
   // @formatter:off
   public HistoryEventHandlerService(EventBus eventBus, DocumentListPresenter documentListPresenter,
                                     AppPresenter appPresenter, SearchResultsPresenter searchResultsPresenter,
                                     GetTransUnitActionContextHolder getTransUnitActionContextHolder,
                                     ModalNavigationStateHolder modalStateHolder, UserConfigHolder configHolder)
   // @formatter:on
   {
      this.eventBus = eventBus;
      this.documentListPresenter = documentListPresenter;
      this.appPresenter = appPresenter;
      this.searchResultsPresenter = searchResultsPresenter;
      this.getTransUnitActionContextHolder = getTransUnitActionContextHolder;
      this.modalStateHolder = modalStateHolder;
      this.configHolder = configHolder;
   }

   @Override
   public void onValueChange(ValueChangeEvent<String> event)
   {
      HistoryToken newHistoryToken = HistoryToken.fromTokenString(event.getValue());
      Log.info("[gwt-history] Responding to history token: " + event.getValue());

      processForDocumentListPresenter(newHistoryToken);
      processForProjectWideSearch(newHistoryToken);

      configHolder.setFilterByUntranslated(newHistoryToken.isFilterUntranslated());
      configHolder.setFilterByNeedReview(newHistoryToken.isFilterFuzzy());
      configHolder.setFilterByTranslated(newHistoryToken.isFilterTranslated());

      DocumentId documentId = documentListPresenter.getDocumentId(newHistoryToken.getDocumentPath());
      if (!getTransUnitActionContextHolder.isContextInitialized() && documentId != null)
      {
         // if editor is not yet initialized, we want to load document with search and target trans unit all at once
         Long textFlowId = newHistoryToken.getTextFlowId();
         TransUnitId transUnitId = textFlowId == null ? null : new TransUnitId(textFlowId);
         getTransUnitActionContextHolder.initContext(documentId, newHistoryToken.getSearchText(), transUnitId);
         eventBus.fireEvent(new InitEditorEvent());
      }

      processForAppPresenter(documentId);
      processForTransFilter(newHistoryToken);
      processForBookmarkedTextFlow(newHistoryToken);
      processEditorOptions(newHistoryToken);

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

   protected void processForAppPresenter(DocumentId docId)
   {
      if (docId != null && !equal(appPresenter.getSelectedDocIdOrNull(), docId))
      {
         appPresenter.selectDocument(docId);
      }
      Log.info("[gwt-history] document id: " + docId);

      if (docId != null)
      {
         eventBus.fireEvent(new DocumentSelectionEvent(docId));
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

   protected void processEditorOptions(HistoryToken token)
   {
      if (!equal(token.isFilterUntranslated(), currentHistoryState.isFilterUntranslated())
            || !equal(token.isFilterFuzzy(), currentHistoryState.isFilterFuzzy())
            || !equal(token.isFilterTranslated(), currentHistoryState.isFilterTranslated()))
      {
         Log.info("[gwt-history] message filter option has changed");

         eventBus.fireEvent(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);
      }
   }

   protected void processForBookmarkedTextFlow(HistoryToken token)
   {
      if (equal(token.getTextFlowId(), currentHistoryState.getTextFlowId()) || token.getTextFlowId() == null || modalStateHolder.getPageCount() == 0)
      {
         // target text flow id hasn't changed,
         // or no text flow id in history,
         // or modal navigation state holder is not yet initialized (which means InitEditorEvent will be handling this.
         // See onValueChange(ValueChangeEvent<String>))
         return;
      }

      TransUnitId transUnitId = new TransUnitId(token.getTextFlowId());
      int targetPage = modalStateHolder.getTargetPage(transUnitId);
      if (targetPage != NavigationService.UNDEFINED)
      {
         Log.info("[gwt-history] bookmarked text flow. Target page: " + targetPage + ", target TU id: " + transUnitId);
         int offset = targetPage * getTransUnitActionContextHolder.getContext().getCount();
         eventBus.fireEvent(new BookmarkedTextFlowEvent(offset, transUnitId));
      }
   }
}
