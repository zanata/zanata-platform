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

import static org.easymock.EasyMock.and;
import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.Capture;
import org.easymock.IAnswer;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

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
public class SearchResultsPresenterTest extends PresenterTest
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

   CachingDispatchAsync mockDispatcher;
   Display mockDisplay;
   EventBus mockEventBus;
   History mockHistory;
   KeyShortcutPresenter mockKeyShortcutPresenter;
   WebTransMessages mockMessages;
   Location mockWindowLocation;
   UserWorkspaceContext mockUserWorkspaceContext;
   Provider<UndoLink> mockUndoLinkProvider;

   HasValue<Boolean> mockCaseSensitiveChk;
   HasValue<String> mockFilterTextBox;
   HasClickHandlers mockReplaceAllButton;
   HasValue<String> mockReplacementTextBox;
   HasValue<Boolean> mockRequirePreviewChk;
   HasValue<Boolean> mockSelectAllHeader;
   HasClickHandlers mockSearchButton;
   HasText mockSearchResponseLabel;
   HasValue<Boolean> mockSelectAllChk;

   Capture<ClickHandler> capturedReplaceAllButtonClickHandler;
   Capture<ValueChangeHandler<String>> capturedReplacementTextBoxValueChangeHandler;
   Capture<ValueChangeHandler<Boolean>> capturedRequirePreviewChkValueChangeHandler;
   Capture<ClickHandler> capturedSearchButtonClickHandler;
   Capture<ClickHandler> capturedSelectAllButtonClickHandler;
   Capture<ValueChangeHandler<Boolean>> capturedSelectAllChkValueChangeHandler;

   Capture<TransUnitUpdatedEventHandler> capturedTransUnitUpdatedEventHandler;
   Capture<WorkspaceContextUpdateEventHandler> capturedWorkspaceContextUpdatedEventHandler;
   Capture<KeyShortcut> capturedKeyShortcuts;
   Capture<HistoryToken> capturedHistoryToken;

   Capture<GetProjectTransUnitLists> capturedDispatchedSearch;
   Capture<AsyncCallback<GetProjectTransUnitListsResult>> capturedDispatchedSearchCallback;

   // arguments to Display.addDocument method
   Capture<ClickHandler> capturedViewDocClickHandlers;
   Capture<ClickHandler> capturedSearchDocClickHandlers;
   Capture<MultiSelectionModel<TransUnitReplaceInfo>> capturedSelectionModels;
   Capture<ValueChangeHandler<Boolean>> capturedSelectDocChangeHandlers;

   // first test document
   ListDataProvider<TransUnitReplaceInfo> mockDataProviderDoc1;
   List<TransUnitReplaceInfo> dataProviderDoc1List;
   MultiSelectionModel<TransUnitReplaceInfo> mockSelectionModelDoc1;
   Capture<SelectionChangeEvent.Handler> capturedSelectionChangeHandlerDoc1;
   Capture<SelectionChangeEvent.Handler> capturedSelectionChangeDeselectHandlerDoc1;

   @BeforeClass
   public void createMocksAndCaptures()
   {
      createAllMocks();
      createAllCaptures();
   }

   @SuppressWarnings("unchecked")
   private void createAllMocks()
   {
      mockDispatcher = createAndAddMock(CachingDispatchAsync.class);
      mockDisplay = createAndAddMock(Display.class);
      mockEventBus = createAndAddMock(EventBus.class);
      mockUserWorkspaceContext = createAndAddMock(UserWorkspaceContext.class);
      mockHistory = createAndAddMock(History.class);
      mockKeyShortcutPresenter = createAndAddMock(KeyShortcutPresenter.class);
      mockMessages = createAndAddMock(WebTransMessages.class);
      mockWindowLocation = createAndAddMock(Window.Location.class);

      mockCaseSensitiveChk = createAndAddMock(HasValue.class);
      mockFilterTextBox = createAndAddMock(HasValue.class);
      mockReplaceAllButton = createAndAddMock(HasClickHandlers.class);
      mockReplacementTextBox = createAndAddMock(HasValue.class);
      mockRequirePreviewChk = createAndAddMock(HasValue.class);
      mockSelectAllHeader = createAndAddMock(HasValue.class);
      mockSearchButton = createAndAddMock(HasClickHandlers.class);
      mockSearchResponseLabel = createAndAddMock(HasText.class);
      mockSelectAllChk = createAndAddMock(HasValue.class);

      mockDataProviderDoc1 = createAndAddMock(ListDataProvider.class);
      mockSelectionModelDoc1 = createAndAddMock(MultiSelectionModel.class);
   }

   private void createAllCaptures()
   {
      capturedReplaceAllButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedReplacementTextBoxValueChangeHandler = addCapture(new Capture<ValueChangeHandler<String>>());
      capturedRequirePreviewChkValueChangeHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());
      capturedSearchButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedSelectAllButtonClickHandler = addCapture(new Capture<ClickHandler>());
      capturedSelectAllChkValueChangeHandler = addCapture(new Capture<ValueChangeHandler<Boolean>>());

      capturedTransUnitUpdatedEventHandler = addCapture(new Capture<TransUnitUpdatedEventHandler>());
      capturedWorkspaceContextUpdatedEventHandler = addCapture(new Capture<WorkspaceContextUpdateEventHandler>());

      capturedKeyShortcuts = addCapture(new Capture<KeyShortcut>());
      capturedHistoryToken = addCapture(new Capture<HistoryToken>());

      capturedDispatchedSearch = addCapture(new Capture<GetProjectTransUnitLists>());
      capturedDispatchedSearchCallback = addCapture(new Capture<AsyncCallback<GetProjectTransUnitListsResult>>());

      capturedViewDocClickHandlers = addCapture(new Capture<ClickHandler>());
      capturedSearchDocClickHandlers = addCapture(new Capture<ClickHandler>());
      capturedSelectionModels = addCapture(new Capture<MultiSelectionModel<TransUnitReplaceInfo>>());
      capturedSelectDocChangeHandlers = addCapture(new Capture<ValueChangeHandler<Boolean>>());

      capturedSelectionChangeHandlerDoc1 = addCapture(new Capture<SelectionChangeEvent.Handler>());
      capturedSelectionChangeDeselectHandlerDoc1 = addCapture(new Capture<SelectionChangeEvent.Handler>());
   }

   @BeforeMethod
   public void beforeMethod()
   {
      resetAll();

      searchResultsPresenter = new SearchResultsPresenter(mockDisplay, mockEventBus, mockDispatcher, mockHistory, mockMessages, mockUserWorkspaceContext, mockKeyShortcutPresenter, mockUndoLinkProvider, mockWindowLocation);

   }

   private void resetDataProviderLists()
   {
      dataProviderDoc1List = new ArrayList<TransUnitReplaceInfo>();
   }

   public void testExpectedActionsOnBind()
   {
      replayAllMocks();
      searchResultsPresenter.bind();
      verifyAllMocks();
   }

   public void searchButtonClickUpdatesHistory()
   {
      boolean caseSensitiveFalseValue = false;
      expectRunSearch(searchPageHistoryToken(), caseSensitiveFalseValue, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_TARGET);
      replayAllMocks();
      searchResultsPresenter.bind();
      simulateClick(capturedSearchButtonClickHandler);
      verifyAllMocks();

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should be updated with current search phrase in search text box", newToken.getProjectSearchText(), is(TEST_SEARCH_PHRASE));
      assertThat("new history token project search case sensitivity should match checkbox value", newToken.getProjectSearchCaseSensitive(), is(caseSensitiveFalseValue));
      assertThat("new history token should reflect search in target when selected search field is target", newToken.isProjectSearchInTarget(), is(true));
      assertThat("new history token should reflect not to search in source when selected search field is target", newToken.isProjectSearchInSource(), is(false));
   }

   public void searchWithCaseSensitive()
   {
      boolean caseSensitiveTrueValue = true;
      expectRunSearch(searchPageHistoryToken(), caseSensitiveTrueValue, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_TARGET);
      replayAllMocks();
      searchResultsPresenter.bind();
      simulateClick(capturedSearchButtonClickHandler);
      verifyAllMocks();

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token project search case sensitivity should match checkbox value", newToken.getProjectSearchCaseSensitive(), is(caseSensitiveTrueValue));
   }

   public void searchInSource()
   {
      expectRunSearch(searchPageHistoryToken(), false, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_SOURCE);
      replayAllMocks();
      searchResultsPresenter.bind();
      simulateClick(capturedSearchButtonClickHandler);
      verifyAllMocks();

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should reflect search in source when selected search field is source", newToken.isProjectSearchInSource(), is(true));
      assertThat("new history token should reflect not to search in target when selected search field is source", newToken.isProjectSearchInTarget(), is(false));
   }

   public void searchInSourceAndTarget()
   {
      expectRunSearch(searchPageHistoryToken(), false, TEST_SEARCH_PHRASE, SearchResultsPresenter.Display.SEARCH_FIELD_BOTH);
      replayAllMocks();
      searchResultsPresenter.bind();
      simulateClick(capturedSearchButtonClickHandler);
      verifyAllMocks();

      HistoryToken newToken = capturedHistoryToken.getValue();
      assertThat("new history token should reflect search in source when selected search field is both", newToken.isProjectSearchInSource(), is(true));
      assertThat("new history token should reflect search in target when selected search field is both", newToken.isProjectSearchInTarget(), is(true));
   }

   public void replacementValueChanged()
   {
      expect(mockHistory.getHistoryToken()).andReturn(searchPageHistoryToken()).once();
      expectNewHistoryItem();
      replayAllMocks();
      searchResultsPresenter.bind();
      valueChangeEvent(capturedReplacementTextBoxValueChangeHandler, TEST_REPLACE_PHRASE);
      verifyAllMocks();

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

      expect(mockMessages.searchForPhraseReturnedNoResults(TEST_SEARCH_PHRASE)).andReturn(TEST_MESSAGE_NO_RESULTS_FOR_SEARCH).once();
      mockSearchResponseLabel.setText(TEST_MESSAGE_NO_RESULTS_FOR_SEARCH);
      mockDisplay.setSearching(false);

      replayAllMocks();
      searchResultsPresenter.bind();

      simulateSearch();

      verifyAllMocks();
   }

   @Test(enabled = false, description = "need fix")
   public void firesSearchFromHistoryOneResult()
   {
      expectSearchAndDisplaySingleResult();
      replayAllMocks();
      searchResultsPresenter.bind();
      simulateSearch();
      verifyAllMocks();
      assertThat("text flows for document should be added to data provider", dataProviderDoc1List.size(), is(1));
   }

   // TODO use 4 results in 2 documents
   @Test(enabled = false, description = "need fix")
   public void selectAllChkCheckedSingleResult()
   {
      expectSearchAndDisplaySingleResult();
      mockSelectAllHeader.setValue(Boolean.TRUE, true);
      expectLastCall().anyTimes();

      replayAllMocks();
      searchResultsPresenter.bind();
      simulateSearch();
      valueChangeEvent(capturedSelectAllChkValueChangeHandler, true);

      verifyAllMocks();
   }

   @Test(enabled = false, description = "need fix")
   public void selectAllChkUnchecked()
   {
      expectSearchAndDisplaySingleResult();
      mockSelectAllHeader.setValue(Boolean.FALSE, true);
      expectLastCall().anyTimes();

      replayAllMocks();
      searchResultsPresenter.bind();
      simulateSearch();
      valueChangeEvent(capturedSelectAllChkValueChangeHandler, false);

      verifyAllMocks();
   }

   private void expectSearchAndDisplaySingleResult()
   {
      // set up some search results
      expectPrepareToDispatchSearch(TEST_SEARCH_PHRASE, false, null);
      expectDispatchSearch(buildSingleTextFlowResponse());
      expect(mockMessages.showingResultsForProjectWideSearch(TEST_SEARCH_PHRASE, 1, 1)).andReturn(TEST_MESSAGE_SHOWING_RESULTS_FOR_SEARCH).once();
      expectSearchReturned(TEST_MESSAGE_SHOWING_RESULTS_FOR_SEARCH);

      // display single document
      expect(mockDisplay.createMultiSelectionModel()).andReturn(mockSelectionModelDoc1).once();
      expect(mockSelectionModelDoc1.addSelectionChangeHandler(capture(capturedSelectionChangeHandlerDoc1))).andReturn(mockHandlerRegistration());
      expect(mockSelectionModelDoc1.addSelectionChangeHandler(capture(capturedSelectionChangeDeselectHandlerDoc1))).andReturn(mockHandlerRegistration());
      
      expect(mockDisplay.addDocument(eq(TEST_DOC_PATH_1), capture(capturedViewDocClickHandlers), capture(capturedSearchDocClickHandlers), capture(capturedSelectionModels), capture(capturedSelectDocChangeHandlers))).andReturn(mockDataProviderDoc1).once();

      expect(mockDisplay.getSelectAllCheckbox()).andReturn(mockSelectAllHeader).once();
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

   private IAnswer<GetProjectTransUnitListsResult> buildNoSearchResultsResponse()
   {
      return buildSuccessSearchResponse(new HashMap<Long, String>(), new HashMap<Long, List<TransUnit>>());
   }

   private IAnswer<GetProjectTransUnitListsResult> buildSingleTextFlowResponse()
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

   private IAnswer<GetProjectTransUnitListsResult> buildSuccessSearchResponse(final Map<Long, String> docPaths, final Map<Long, List<TransUnit>> documents)
   {
      IAnswer<GetProjectTransUnitListsResult> searchResponse = new IAnswer<GetProjectTransUnitListsResult>()
      {
         @Override
         public GetProjectTransUnitListsResult answer() throws Throwable
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

      expect(mockWindowLocation.getQueryDocuments()).andReturn(queryStringDocuments).once();
   }

   @SuppressWarnings("unchecked")
   private void expectDispatchSearch(IAnswer<GetProjectTransUnitListsResult> searchResponse)
   {
      mockDispatcher.execute(and(capture(capturedDispatchedSearch), isA(Action.class)), and(capture(capturedDispatchedSearchCallback), isA(AsyncCallback.class)));
      expectLastCall().andAnswer(searchResponse);
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

   @Override
   protected void setDefaultBindExpectations()
   {
      boolean workspaceIsReadOnly = false;

      expectUiMessages();
      expectDisplayComponentGetters();
      expectHandlerRegistrations();

      resetDataProviderLists();
      expect(mockDataProviderDoc1.getList()).andReturn(dataProviderDoc1List).anyTimes();

      expect(mockUserWorkspaceContext.hasReadOnlyAccess()).andReturn(workspaceIsReadOnly).anyTimes();
      mockDisplay.setReplaceAllButtonVisible(!workspaceIsReadOnly);
      mockDisplay.setReplaceAllButtonEnabled(false);
      
      mockDisplay.addSearchFieldsSelect("search target", "target");
      mockDisplay.addSearchFieldsSelect("search source", "source");
      mockDisplay.addSearchFieldsSelect("search both", "both");
      
      expect(mockKeyShortcutPresenter.register(capture(capturedKeyShortcuts))).andReturn(mockHandlerRegistration()).times(TOTAL_KEY_SHORTCUTS);
   }

   private void expectUiMessages()
   {
      expect(mockMessages.selectAllTextFlowsKeyShortcut()).andReturn(TEST_MESSAGE_SELECT_ALL_TEXT_FLOWS_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.focusSearchPhraseKeyShortcut()).andReturn(TEST_MESSAGE_FOCUS_SEARCH_PHRASE_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.focusReplacementPhraseKeyShortcut()).andReturn(TEST_MESSAGE_FOCUS_REPLACEMENT_PHRASE_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.replaceSelectedKeyShortcut()).andReturn(TEST_MESSAGE_REPLACE_SELECTED_KEY_SHORTCUT).anyTimes();
      expect(mockMessages.toggleRowActionButtons()).andReturn(TEST_MESSAGE_TOGGLE_ROW_ACTION_BUTTONS).anyTimes();
   }

   private void expectDisplayComponentGetters()
   {
      // getters used during bind
      expect(mockDisplay.getSearchButton()).andReturn(mockSearchButton).anyTimes();
      expect(mockDisplay.getReplacementTextBox()).andReturn(mockReplacementTextBox).anyTimes();
      expect(mockDisplay.getSelectAllChk()).andReturn(mockSelectAllChk).anyTimes();
      expect(mockDisplay.getRequirePreviewChk()).andReturn(mockRequirePreviewChk).anyTimes();
      expect(mockDisplay.getReplaceAllButton()).andReturn(mockReplaceAllButton).anyTimes();

      // getters used after bind
      expect(mockDisplay.getCaseSensitiveChk()).andReturn(mockCaseSensitiveChk).anyTimes();
      expect(mockDisplay.getFilterTextBox()).andReturn(mockFilterTextBox).anyTimes();
      expect(mockDisplay.getSearchResponseLabel()).andReturn(mockSearchResponseLabel).anyTimes();
   }

   private void expectHandlerRegistrations()
   {
      expectClickHandlerRegistration(mockSearchButton, capturedSearchButtonClickHandler);
      expectValueChangeHandlerRegistration(mockReplacementTextBox, capturedReplacementTextBoxValueChangeHandler);
      expectValueChangeHandlerRegistration(mockSelectAllChk, capturedSelectAllChkValueChangeHandler);
      expectValueChangeHandlerRegistration(mockRequirePreviewChk, capturedRequirePreviewChkValueChangeHandler);
      expectClickHandlerRegistration(mockReplaceAllButton, capturedReplaceAllButtonClickHandler);

      expectEventHandlerRegistration(mockEventBus, TransUnitUpdatedEvent.getType(), TransUnitUpdatedEventHandler.class, capturedTransUnitUpdatedEventHandler);
      expectEventHandlerRegistration(mockEventBus, WorkspaceContextUpdateEvent.getType(), WorkspaceContextUpdateEventHandler.class, capturedWorkspaceContextUpdatedEventHandler);
   }

   private void expectRunSearch(HistoryToken previousHistoryToken, boolean caseSensitive, String searchPhrase, String searchInFields)
   {
      expect(mockHistory.getHistoryToken()).andReturn(previousHistoryToken).once();
      expect(mockCaseSensitiveChk.getValue()).andReturn(caseSensitive).once();
      expect(mockFilterTextBox.getValue()).andReturn(searchPhrase).once();
      expect(mockDisplay.getSelectedSearchField()).andReturn(searchInFields).once();

      expectNewHistoryItem();
   }

   private void expectNewHistoryItem()
   {
      mockHistory.newItem(capture(capturedHistoryToken));
   }

}
