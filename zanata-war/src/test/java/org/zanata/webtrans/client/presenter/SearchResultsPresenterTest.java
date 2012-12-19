/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window.Location;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter.Display;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitLists;
import org.zanata.webtrans.shared.rpc.GetProjectTransUnitListsResult;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Provider;

/**
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 * 
 */
@Test(groups = { "unit-tests" })
public class SearchResultsPresenterTest
{

   private static final String TEST_LOCALE_ID = "de";

   private static final long TEST_DOC_ID_1 = 5L;
   private static final String TEST_DOC_PATH_1 = "doc1";

   private static final String TEST_TARGET_STRING_1 = "target 1";
   private static final String TEST_SOURCE_STRING_1 = "source";
   private static final int TEST_VERNUM_1 = 0;
   private static final long TEST_TU_ID_1 = 7L;
   private static final String TEST_RES_ID_1 = "resId1";

   private static final int TOTAL_KEY_SHORTCUTS = 5;

   private static final String TEST_MESSAGE_FOCUS_SEARCH_PHRASE_KEY_SHORTCUT = "Focus search phrase";
   private static final String TEST_MESSAGE_FOCUS_REPLACEMENT_PHRASE_KEY_SHORTCUT = "Focus replacement phrase";
   private static final String TEST_MESSAGE_REPLACE_SELECTED_KEY_SHORTCUT = "Replace selected text flows";
   private static final String TEST_MESSAGE_SELECT_ALL_TEXT_FLOWS_KEY_SHORTCUT = "Select all text flows";
   private static final String TEST_MESSAGE_SHOWING_RESULTS_FOR_SEARCH = "Results for search XXX (X text flows in X documents)";
   private static final String TEST_MESSAGE_TOGGLE_ROW_ACTION_BUTTONS = "Toggle row action buttons";

   private static final String TEST_SEARCH_PHRASE = "search phrase";
   private static final String TEST_REPLACE_PHRASE = "replace phrase";

   private static final String TEST_MESSAGE_NO_RESULTS_FOR_SEARCH = "No results for search XXX";

   // object under test
   SearchResultsPresenter searchResultsPresenter;

   @Mock
   CachingDispatchAsync mockDispatcher;
   @Mock
   Display mockDisplay;
   @Mock
   EventBus mockEventBus;
   @Mock
   History mockHistory;
   @Mock
   KeyShortcutPresenter mockKeyShortcutPresenter;
   @Mock
   WebTransMessages mockMessages;
   @Mock
   Location mockWindowLocation;
   @Mock
   UserWorkspaceContext mockUserWorkspaceContext;
   @Mock
   Provider<UndoLink> mockUndoLinkProvider;

   @Mock
   HasValue<Boolean> mockCaseSensitiveChk;
   @Mock
   HasValue<String> mockFilterTextBox;
   @Mock
   HasClickHandlers mockReplaceAllButton;
   @Mock
   HasValue<String> mockReplacementTextBox;
   @Mock
   HasValue<Boolean> mockRequirePreviewChk;
   @Mock
   HasValue<Boolean> mockSelectAllHeader;
   @Mock
   HasClickHandlers mockSearchButton;
   @Mock
   HasText mockSearchResponseLabel;
   @Mock
   HasValue<Boolean> mockSelectAllChk;

   @Captor
   ArgumentCaptor<ClickHandler> capturedReplaceAllButtonClickHandler;
   @Captor
   ArgumentCaptor<ValueChangeHandler<String>> capturedReplacementTextBoxValueChangeHandler;
   @Captor
   ArgumentCaptor<ValueChangeHandler<Boolean>> capturedRequirePreviewChkValueChangeHandler;
   @Captor
   ArgumentCaptor<ClickHandler> capturedSearchButtonClickHandler;
   @Captor
   ArgumentCaptor<ClickHandler> capturedSelectAllButtonClickHandler;
   @Captor
   ArgumentCaptor<ValueChangeHandler<Boolean>> capturedSelectAllChkValueChangeHandler;

   @Captor
   ArgumentCaptor<TransUnitUpdatedEventHandler> capturedTransUnitUpdatedEventHandler;
   @Captor
   ArgumentCaptor<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdatedEventHandler;
   @Captor
   ArgumentCaptor<KeyShortcut> capturedKeyShortcuts;
   @Captor
   ArgumentCaptor<HistoryToken> capturedHistoryToken;

   @Captor
   ArgumentCaptor<GetProjectTransUnitLists> capturedDispatchedSearch;
   @Captor
   ArgumentCaptor<AsyncCallback<GetProjectTransUnitListsResult>> capturedDispatchedSearchCallback;

   // arguments to Display.addDocument method
   @Captor
   ArgumentCaptor<ClickHandler> capturedViewDocClickHandlers;
   @Captor
   ArgumentCaptor<ClickHandler> capturedSearchDocClickHandlers;
   @Captor
   ArgumentCaptor<ClickHandler> capturedInfoClickHandlers;
   @Captor
   ArgumentCaptor<MultiSelectionModel<TransUnitReplaceInfo>> capturedSelectionModels;
   @Captor
   ArgumentCaptor<ValueChangeHandler<Boolean>> capturedSelectDocChangeHandlers;

   // first test document
   @Mock
   ListDataProvider<TransUnitReplaceInfo> mockDataProviderDoc1;
   List<TransUnitReplaceInfo> dataProviderDoc1List;
   @Mock 
   MultiSelectionModel<TransUnitReplaceInfo> mockSelectionModelDoc1;
   @Captor
   ArgumentCaptor<SelectionChangeEvent.Handler> capturedSelectionChangeHandlerDoc1;
   @Captor
   ArgumentCaptor<SelectionChangeEvent.Handler> capturedSelectionChangeDeselectHandlerDoc1;
   @Mock 
   private ClickEvent clickEvent;
   @Mock 
   private HandlerRegistration handlerRegistration;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);

      searchResultsPresenter = new SearchResultsPresenter(mockDisplay, mockEventBus, mockDispatcher, mockHistory, mockMessages, mockUserWorkspaceContext, mockKeyShortcutPresenter, mockUndoLinkProvider, mockWindowLocation);

      when(mockMessages.selectAllTextFlowsKeyShortcut()).thenReturn(TEST_MESSAGE_SELECT_ALL_TEXT_FLOWS_KEY_SHORTCUT);
      when(mockMessages.focusSearchPhraseKeyShortcut()).thenReturn(TEST_MESSAGE_FOCUS_SEARCH_PHRASE_KEY_SHORTCUT);
      when(mockMessages.focusReplacementPhraseKeyShortcut()).thenReturn(TEST_MESSAGE_FOCUS_REPLACEMENT_PHRASE_KEY_SHORTCUT);
      when(mockMessages.replaceSelectedKeyShortcut()).thenReturn(TEST_MESSAGE_REPLACE_SELECTED_KEY_SHORTCUT);
      when(mockMessages.toggleRowActionButtons()).thenReturn(TEST_MESSAGE_TOGGLE_ROW_ACTION_BUTTONS);

      // getters used during bind
      when(mockDisplay.getSearchButton()).thenReturn(mockSearchButton);
      when(mockDisplay.getReplacementTextBox()).thenReturn(mockReplacementTextBox);
      when(mockDisplay.getSelectAllChk()).thenReturn(mockSelectAllChk);
      when(mockDisplay.getRequirePreviewChk()).thenReturn(mockRequirePreviewChk);
      when(mockDisplay.getReplaceAllButton()).thenReturn(mockReplaceAllButton);

      // getters used after bind
      when(mockDisplay.getCaseSensitiveChk()).thenReturn(mockCaseSensitiveChk);
      when(mockDisplay.getFilterTextBox()).thenReturn(mockFilterTextBox);
      when(mockDisplay.getSearchResponseLabel()).thenReturn(mockSearchResponseLabel);


   }

   @Test
   public void testExpectedActionsOnBind()
   {
      when(mockKeyShortcutPresenter.register(capturedKeyShortcuts.capture())).thenReturn(handlerRegistration);
      when(mockDataProviderDoc1.getList()).thenReturn(dataProviderDoc1List);

      boolean workspaceIsReadOnly = false;
      when(mockUserWorkspaceContext.hasReadOnlyAccess()).thenReturn(workspaceIsReadOnly);

      searchResultsPresenter.bind();

      verify(mockSearchButton).addClickHandler(capturedSearchButtonClickHandler.capture());
      verify(mockReplacementTextBox).addValueChangeHandler(capturedReplacementTextBoxValueChangeHandler.capture());
      verify(mockSelectAllChk).addValueChangeHandler(capturedSelectAllChkValueChangeHandler.capture());
      verify(mockRequirePreviewChk).addValueChangeHandler(capturedRequirePreviewChkValueChangeHandler.capture());
      verify(mockReplaceAllButton).addClickHandler(capturedReplaceAllButtonClickHandler.capture());

      verify(mockEventBus).addHandler(eq(TransUnitUpdatedEvent.getType()), capturedTransUnitUpdatedEventHandler.capture());
      verify(mockEventBus).addHandler(eq(WorkspaceContextUpdateEvent.getType()), capturedWorkspaceContextUpdatedEventHandler.capture());


      dataProviderDoc1List = new ArrayList<TransUnitReplaceInfo>();
      mockDisplay.setReplaceAllButtonVisible(!workspaceIsReadOnly);
      mockDisplay.setReplaceAllButtonEnabled(false);

      mockDisplay.addSearchFieldsSelect("search target", "target");
      mockDisplay.addSearchFieldsSelect("search source", "source");
      mockDisplay.addSearchFieldsSelect("search both", "both");

   }

   @Test
   public void searchButtonClickUpdatesHistory()
   {
      boolean caseSensitiveFalseValue = false;
      expectRunSearch(searchPageHistoryToken(), caseSensitiveFalseValue, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_TARGET);

      searchResultsPresenter.bind();

      verify(mockSearchButton).addClickHandler(capturedSearchButtonClickHandler.capture());

      capturedSearchButtonClickHandler.getValue().onClick(clickEvent);

      verify(mockHistory).newItem(capturedHistoryToken.capture());

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should be updated with current search phrase in search text box", newToken.getProjectSearchText(), is(TEST_SEARCH_PHRASE));
      assertThat("new history token project search case sensitivity should match checkbox value", newToken.getProjectSearchCaseSensitive(), is(caseSensitiveFalseValue));
      assertThat("new history token should reflect search in target when selected search field is target", newToken.isProjectSearchInTarget(), is(true));
      assertThat("new history token should reflect not to search in source when selected search field is target", newToken.isProjectSearchInSource(), is(false));
   }

   @Test
   public void searchWithCaseSensitive()
   {
      boolean caseSensitiveTrueValue = true;
      expectRunSearch(searchPageHistoryToken(), caseSensitiveTrueValue, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_TARGET);
      
      searchResultsPresenter.bind();

      verify(mockSearchButton).addClickHandler(capturedSearchButtonClickHandler.capture());

      capturedSearchButtonClickHandler.getValue().onClick(clickEvent);

      verify(mockHistory).newItem(capturedHistoryToken.capture());
      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token project search case sensitivity should match checkbox value", newToken.getProjectSearchCaseSensitive(), is(caseSensitiveTrueValue));
   }

   @Test
   public void searchInSource()
   {
      expectRunSearch(searchPageHistoryToken(), false, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_SOURCE);
      
      searchResultsPresenter.bind();
      verify(mockSearchButton).addClickHandler(capturedSearchButtonClickHandler.capture());
      capturedSearchButtonClickHandler.getValue().onClick(clickEvent);

      verify(mockHistory).newItem(capturedHistoryToken.capture());

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should reflect search in source when selected search field is source", newToken.isProjectSearchInSource(), is(true));
      assertThat("new history token should reflect not to search in target when selected search field is source", newToken.isProjectSearchInTarget(), is(false));
   }

   @Test
   public void searchInSourceAndTarget()
   {
      expectRunSearch(searchPageHistoryToken(), false, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_BOTH);
      
      searchResultsPresenter.bind();
      verify(mockSearchButton).addClickHandler(capturedSearchButtonClickHandler.capture());

      capturedSearchButtonClickHandler.getValue().onClick(clickEvent);

      verify(mockHistory).newItem(capturedHistoryToken.capture());

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should reflect search in source when selected search field is both", newToken.isProjectSearchInSource(), is(true));
      assertThat("new history token should reflect search in target when selected search field is both", newToken.isProjectSearchInTarget(), is(true));
   }

   @Test
   public void replacementValueChanged()
   {
      when(mockHistory.getHistoryToken()).thenReturn(searchPageHistoryToken());

      searchResultsPresenter.bind();
      verify(mockReplacementTextBox).addValueChangeHandler(capturedReplacementTextBoxValueChangeHandler.capture());

      ValueChangeEvent<String> valueChangeEvent = mock(ValueChangeEvent.class);
      when(valueChangeEvent.getValue()).thenReturn(TEST_REPLACE_PHRASE);
      capturedReplacementTextBoxValueChangeHandler.getValue().onValueChange(valueChangeEvent);

      verify(mockHistory).newItem(capturedHistoryToken.capture());

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should be updated with current replacement phrase", newToken.getProjectSearchReplacement(), is(TEST_REPLACE_PHRASE));
   }

   @Test(enabled = false, description = "need fix")
   public void firesSearchFromHistoryNoResults()
   {
      expectPrepareToDispatchSearch(TEST_SEARCH_PHRASE, false, null);
      expectDispatchSearch(buildNoSearchResultsResponse());

      // display search results
      mockDisplay.clearAll();
      mockDisplay.setReplaceAllButtonEnabled(false);

      when(mockMessages.searchForPhraseReturnedNoResults(TEST_SEARCH_PHRASE)).thenReturn(TEST_MESSAGE_NO_RESULTS_FOR_SEARCH);
      mockSearchResponseLabel.setText(TEST_MESSAGE_NO_RESULTS_FOR_SEARCH);
      mockDisplay.setSearching(false);

      
      searchResultsPresenter.bind();

      simulateSearch();

      
   }

   @Test(enabled = false, description = "need fix")
   public void firesSearchFromHistoryOneResult()
   {
      expectSearchAndDisplaySingleResult();
      
      searchResultsPresenter.bind();
      simulateSearch();
      
      assertThat("text flows for document should be added to data provider", dataProviderDoc1List.size(), is(1));
   }

   // TODO use 4 results in 2 documents
   @Test(enabled = false, description = "need fix")
   public void selectAllChkCheckedSingleResult()
   {
      expectSearchAndDisplaySingleResult();
      mockSelectAllHeader.setValue(Boolean.TRUE, true);
     

      
      searchResultsPresenter.bind();
      simulateSearch();
//      valueChangeEvent(capturedSelectAllChkValueChangeHandler, true);

      
   }

   @Test(enabled = false, description = "need fix")
   public void selectAllChkUnchecked()
   {
      expectSearchAndDisplaySingleResult();
      mockSelectAllHeader.setValue(Boolean.FALSE, true);
     

      
      searchResultsPresenter.bind();
      simulateSearch();
//      valueChangeEvent(capturedSelectAllChkValueChangeHandler, false);

      
   }

   private void expectSearchAndDisplaySingleResult()
   {
      // set up some search results
      expectPrepareToDispatchSearch(TEST_SEARCH_PHRASE, false, null);
      expectDispatchSearch(buildSingleTextFlowResponse());
      when(mockMessages.showingResultsForProjectWideSearch(TEST_SEARCH_PHRASE, 1, 1)).thenReturn(TEST_MESSAGE_SHOWING_RESULTS_FOR_SEARCH);
      expectSearchReturned(TEST_MESSAGE_SHOWING_RESULTS_FOR_SEARCH);

      // display single document
      when(mockDisplay.createMultiSelectionModel()).thenReturn(mockSelectionModelDoc1);
      when(mockSelectionModelDoc1.addSelectionChangeHandler(capturedSelectionChangeHandlerDoc1.capture())).thenReturn(handlerRegistration);
      when(mockSelectionModelDoc1.addSelectionChangeHandler(capturedSelectionChangeDeselectHandlerDoc1.capture())).thenReturn(handlerRegistration);
      
      when(mockDisplay.addDocument(eq(TEST_DOC_PATH_1), capturedViewDocClickHandlers.capture(), capturedSearchDocClickHandlers.capture(), capturedInfoClickHandlers.capture(), capturedSelectionModels.capture(), capturedSelectDocChangeHandlers.capture())).thenReturn(mockDataProviderDoc1);

      when(mockDisplay.getSelectAllCheckbox()).thenReturn(mockSelectAllHeader);
   }

   private void expectSearchReturned(String searchResponseInfoMessage)
   {
      mockDisplay.clearAll();
      mockDisplay.setReplaceAllButtonEnabled(false);
      mockDisplay.setSearching(false);
      mockSearchResponseLabel.setText(searchResponseInfoMessage);
   }

   private void simulateSearch()
   {
      HistoryToken token = searchPageHistoryToken();
      token.setProjectSearchText(TEST_SEARCH_PHRASE);
   }

   private Answer<GetProjectTransUnitListsResult> buildNoSearchResultsResponse()
   {
      return buildSuccessSearchResponse(new HashMap<Long, String>(), new HashMap<Long, List<TransUnit>>());
   }

   private Answer<GetProjectTransUnitListsResult> buildSingleTextFlowResponse()
   {
      final Map<Long, String> docPaths = new HashMap<Long, String>();
      docPaths.put(TEST_DOC_ID_1, TEST_DOC_PATH_1);

      final Map<Long, List<TransUnit>> documents = new HashMap<Long, List<TransUnit>>();
      List<TransUnit> docs = new ArrayList<TransUnit>();
      docs.add(TransUnit.Builder.newTransUnitBuilder()
            .setId(TEST_TU_ID_1)
            .setResId(TEST_RES_ID_1)
            .setLocaleId(TEST_LOCALE_ID)
            .setRowIndex(0)
            .setVerNum(TEST_VERNUM_1)
            .addSource(TEST_SOURCE_STRING_1)
            .addTargets(TEST_TARGET_STRING_1)
            .build());
      documents.put(TEST_DOC_ID_1, docs);

      return buildSuccessSearchResponse(docPaths, documents);
   }

   private Answer<GetProjectTransUnitListsResult> buildSuccessSearchResponse(final Map<Long, String> docPaths, final Map<Long, List<TransUnit>> documents)
   {
      Answer<GetProjectTransUnitListsResult> searchResponse = new Answer<GetProjectTransUnitListsResult>()
      {

         @Override
         public GetProjectTransUnitListsResult answer(InvocationOnMock invocation) throws Throwable
         {
            GetProjectTransUnitListsResult result = new GetProjectTransUnitListsResult(capturedDispatchedSearch.getValue(), docPaths, documents);
            capturedDispatchedSearchCallback.getValue().onSuccess(result);
            return null;
         }
      };
      return searchResponse;
   }

   private void expectPrepareToDispatchSearch(String searchPhrase, boolean caseSensitiveSearch, List<String> queryStringDocuments)
   {
      // respond to search in history token
      mockFilterTextBox.setValue(searchPhrase, false);
      mockCaseSensitiveChk.setValue(caseSensitiveSearch, false);
      mockDisplay.clearAll();
      mockDisplay.setReplaceAllButtonEnabled(false);
      mockDisplay.setSearching(true);

      // this would probably be more correct to do when
      // the search returns, rather than when it begins
      mockDisplay.setHighlightString(searchPhrase);

      when(mockWindowLocation.getQueryDocuments()).thenReturn(queryStringDocuments);
   }

   @SuppressWarnings("unchecked")
   private void expectDispatchSearch(Answer<GetProjectTransUnitListsResult> searchResponse)
   {
      doAnswer(searchResponse).when(mockDispatcher).execute(capturedDispatchedSearch.capture(), capturedDispatchedSearchCallback.capture());
   }

   /**
    * @return a history token with defaults except current page is search page
    */
   private HistoryToken searchPageHistoryToken()
   {
      HistoryToken historyToken = new HistoryToken();
      historyToken.setView(MainView.Search);
      return historyToken;
   }

   private void expectRunSearch(HistoryToken previousHistoryToken, boolean caseSensitive, String searchPhrase, String searchInFields)
   {
      when(mockHistory.getHistoryToken()).thenReturn(previousHistoryToken);
      when(mockCaseSensitiveChk.getValue()).thenReturn(caseSensitive);
      when(mockFilterTextBox.getValue()).thenReturn(searchPhrase);
      when(mockDisplay.getSelectedSearchField()).thenReturn(searchInFields);
   }

}
