package org.zanata.webtrans.client.presenter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.PresenterRevealedEvent;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.TransUnitCount;
import org.zanata.common.TransUnitWords;
import org.zanata.common.TranslationStats;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.DocumentStatsUpdatedEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.ProjectStatsUpdatedEvent;
import org.zanata.webtrans.client.events.ShowSideMenuEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.history.Window;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.view.AppDisplay;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.HasWorkspaceContextUpdateData;

@Test(groups = { "unit-tests" })
public class AppPresenterTest
{
   private AppPresenter presenter;
   @Mock
   private AppDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private SideMenuPresenter sideMenuPresenter;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;
   @Mock
   private TranslationPresenter translationPresenter;
   @Mock
   private DocumentListPresenter documentListPresenter;
   @Mock
   private SearchResultsPresenter searchResultPresenter;
   private UserWorkspaceContext userWorkspace = TestFixture.userWorkspaceContext();
   @Mock
   private WebTransMessages messages;
   @Mock
   private History history;
   @Mock
   private Window window;
   @Mock
   private Window.Location location;
   @Captor
   private ArgumentCaptor<TranslationStats> statsCaptor;
   private TranslationStats projectStats;
   private TranslationStats selectedDocumentStats;
   @Captor
   private ArgumentCaptor<KeyShortcut> keyShortcutCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      selectedDocumentStats = new TranslationStats(new TransUnitCount(1, 2, 3), new TransUnitWords(4, 5, 6));
      projectStats = new TranslationStats(new TransUnitCount(7, 8, 9), new TransUnitWords(10, 11, 12));

      MockitoAnnotations.initMocks(this);
      presenter = new AppPresenter(display, eventBus, sideMenuPresenter, keyShortcutPresenter, translationPresenter, documentListPresenter, searchResultPresenter, userWorkspace, messages, history, window, location);

      verify(display).setListener(presenter);
   }

   @Test
   public void onBind()
   {
      when(location.getParameter("title")).thenReturn("blah");
      when(messages.windowTitle2(userWorkspace.getWorkspaceContext().getWorkspaceName(), userWorkspace.getWorkspaceContext().getLocaleName(), "blah")).thenReturn("new title");
      
      presenter.onBind();

      verify(keyShortcutPresenter).bind();
      verify(documentListPresenter).bind();
      verify(translationPresenter).bind();
      verify(searchResultPresenter).bind();
      verify(sideMenuPresenter).bind();

      verify(eventBus).addHandler(ShowSideMenuEvent.getType(), presenter);
      verify(eventBus).addHandler(WorkspaceContextUpdateEvent.getType(), presenter);
      verify(eventBus).addHandler(DocumentStatsUpdatedEvent.getType(), presenter);
      verify(eventBus).addHandler(ProjectStatsUpdatedEvent.getType(), presenter);
      verify(eventBus).addHandler(PresenterRevealedEvent.getType(), presenter);

      WorkspaceId workspaceId = userWorkspace.getWorkspaceContext().getWorkspaceId();
      String localeId = workspaceId.getLocaleId().getId();
      
      verify(display).setProjectLinkLabel(workspaceId.getProjectIterationId().getProjectSlug());
      verify(display).setVersionLinkLabel(workspaceId.getProjectIterationId().getIterationSlug());
      verify(display).setFilesLinkLabel("Documents (" + localeId + ")");
      
      
      verify(display).setReadOnlyVisible(userWorkspace.hasReadOnlyAccess());
      verify(window).setTitle("new title");
      verify(keyShortcutPresenter, times(3)).register(keyShortcutCaptor.capture());
   }

   @Test
   public void testKeyShortcutsKeys()
   {
      when(location.getParameter("title")).thenReturn(null);
      when(messages.windowTitle(userWorkspace.getWorkspaceContext().getWorkspaceName(), userWorkspace.getWorkspaceContext().getLocaleName())).thenReturn("title");
      when(messages.showDocumentListKeyShortcut()).thenReturn("doc list");
      when(messages.showEditorKeyShortcut()).thenReturn("editor");
      when(messages.showProjectWideSearch()).thenReturn("project wide search");
      presenter.onBind();
      verify(window).setTitle("title");
      verify(keyShortcutPresenter, times(3)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> shortcuts = keyShortcutCaptor.getAllValues();

      // testing keys
      KeyShortcut docListKey = shortcuts.get(0);
      assertThat(docListKey.getAllKeys(), Matchers.contains(new Keys(Keys.ALT_KEY, 'L')));
      assertThat(docListKey.getDescription(), Matchers.equalTo("doc list"));
      assertThat(docListKey.getContext(), Matchers.equalTo(ShortcutContext.Application));

      KeyShortcut editorKey = shortcuts.get(1);
      assertThat(editorKey.getAllKeys(), Matchers.contains(new Keys(Keys.ALT_KEY, 'O')));
      assertThat(editorKey.getDescription(), Matchers.equalTo("editor"));
      assertThat(editorKey.getContext(), Matchers.equalTo(ShortcutContext.Application));

      KeyShortcut searchKey = shortcuts.get(2);
      assertThat(searchKey.getAllKeys(), Matchers.contains(new Keys(Keys.ALT_KEY, 'P')));
      assertThat(searchKey.getDescription(), Matchers.equalTo("project wide search"));
      assertThat(searchKey.getContext(), Matchers.equalTo(ShortcutContext.Application));
   }

   @Test
   public void testKeyShortcutHandlers()
   {
      when(messages.showDocumentListKeyShortcut()).thenReturn("doc list");
      when(messages.showEditorKeyShortcut()).thenReturn("editor");
      when(messages.showProjectWideSearch()).thenReturn("project wide search");
      presenter.onBind();
      verify(keyShortcutPresenter, times(3)).register(keyShortcutCaptor.capture());
      List<KeyShortcut> shortcuts = keyShortcutCaptor.getAllValues();

      // testing keys
      KeyShortcut docListKey = shortcuts.get(0);
      KeyShortcut editorKey = shortcuts.get(1);
      KeyShortcut searchKey = shortcuts.get(2);

      // testing handlers
      // doc key handler
      HistoryToken docToken = new HistoryToken();
      when(history.getHistoryToken()).thenReturn(docToken);
      docListKey.getHandler().onKeyShortcut(null);
      assertThat(docToken.getView(), Matchers.is(MainView.Documents));
      verify(history).newItem(docToken.toTokenString());

      // editor key handler on selected doc is null
      when(messages.noDocumentSelected()).thenReturn("no doc selected");
      editorKey.getHandler().onKeyShortcut(null);
      verify(eventBus).fireEvent(isA(NotificationEvent.class));

      // editor key handler on selected doc is NOT null
      HistoryToken editorToken = new HistoryToken();
      when(history.getHistoryToken()).thenReturn(editorToken);
      DocumentInfo selectedDocument = mock(DocumentInfo.class);
      presenter.setStatesForTest(null ,null, MainView.Documents, selectedDocument);
      editorKey.getHandler().onKeyShortcut(null);
      assertThat(editorToken.getView(), Matchers.is(MainView.Editor));
      verify(history).newItem(editorToken.toTokenString());

      // search key handler
      HistoryToken searchToken = new HistoryToken();
      when(history.getHistoryToken()).thenReturn(searchToken);
      searchKey.getHandler().onKeyShortcut(null);
      assertThat(searchToken.getView(), Matchers.is(MainView.Search));
      verify(history).newItem(searchToken.toTokenString());
   }

   @Test
   public void onShowSideMenu()
   {
      presenter.onShowSideMenu(new ShowSideMenuEvent(true));
      verify(display).showSideMenu(true);

      presenter.onShowSideMenu(new ShowSideMenuEvent(false));
      verify(display).showSideMenu(false);
   }

   @Test
   public void onWorkspaceContextUpdateToReadOnly()
   {
      when(messages.notifyReadOnlyWorkspace()).thenReturn("readonly workspace");
      ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

      presenter.onWorkspaceContextUpdated(new WorkspaceContextUpdateEvent(contextUpdateData(false)));

      verify(eventBus).fireEvent(eventCaptor.capture());
      NotificationEvent event = eventCaptor.getValue();
      assertThat(event.getMessage(), Matchers.equalTo("readonly workspace"));
      verify(display).setReadOnlyVisible(userWorkspace.hasReadOnlyAccess());
   }

   @Test
   public void onWorkspaceContextUpdateToEditable()
   {
      when(messages.notifyEditableWorkspace()).thenReturn("editable workspace");
      ArgumentCaptor<NotificationEvent> eventCaptor = ArgumentCaptor.forClass(NotificationEvent.class);

      presenter.onWorkspaceContextUpdated(new WorkspaceContextUpdateEvent(contextUpdateData(true)));

      verify(eventBus).fireEvent(eventCaptor.capture());
      NotificationEvent event = eventCaptor.getValue();
      assertThat(event.getMessage(), Matchers.equalTo("editable workspace"));
      verify(display).setReadOnlyVisible(userWorkspace.hasReadOnlyAccess());
   }

   @Test
   public void canSwitchToEditorView()
   {
      // Given:
      presenter.setStatesForTest(projectStats, selectedDocumentStats, null, null);

      // When:
      presenter.showView(MainView.Editor);

      // Then:
      verify(translationPresenter).revealDisplay();
      verify(searchResultPresenter).concealDisplay();
      verify(sideMenuPresenter).setOptionMenu(MainView.Editor);
      verify(display).showInMainView(MainView.Editor);
      verify(display).setStats(statsCaptor.capture(), eq(true));

      assertThat(statsCaptor.getValue(), Matchers.sameInstance(selectedDocumentStats));
   }

   @Test
   public void canSwitchToSearchView()
   {
      // Given: current view is editor
      when(messages.projectWideSearchAndReplace()).thenReturn("search and replace");
      presenter.setStatesForTest(projectStats, selectedDocumentStats, MainView.Editor, null);

      // When:
      presenter.showView(MainView.Search);

      // Then:
      verify(translationPresenter).saveEditorPendingChange();
      verify(display).setDocumentLabel("", "search and replace");
      verify(translationPresenter).concealDisplay();
      verify(searchResultPresenter).revealDisplay();
      verify(sideMenuPresenter).setOptionMenu(MainView.Search);
      verify(display).showInMainView(MainView.Search);
      verify(display).setStats(statsCaptor.capture(), eq(true));

      assertThat(statsCaptor.getValue(), Matchers.sameInstance(projectStats));
   }

   @Test
   public void canSwitchToDocumentView()
   {
      // Given: current selected document is null
      assertThat(presenter.getSelectedDocIdOrNull(), Matchers.is(Matchers.nullValue()));
      assertThat(presenter.getSelectedDocumentInfoOrNull(), Matchers.is(Matchers.nullValue()));
      when(messages.documentListTitle()).thenReturn("Documents");
      presenter.setStatesForTest(projectStats, selectedDocumentStats, null, null);

      // When:
      presenter.showView(MainView.Documents);

      // Then:
      verify(display).setDocumentLabel("", "Documents");
      verify(translationPresenter).concealDisplay();
      verify(searchResultPresenter).concealDisplay();
      verify(sideMenuPresenter).setOptionMenu(MainView.Documents);
      verify(display).showInMainView(MainView.Documents);
      verify(display).setStats(statsCaptor.capture(), eq(true));

      assertThat(statsCaptor.getValue(), Matchers.sameInstance(projectStats));
   }

   @Test
   public void canSelectDocumentInEditorView()
   {
      // Given:
      presenter.setStatesForTest(projectStats, selectedDocumentStats, null, null);
      DocumentId docId = new DocumentId(1L, "");
      // newly selected document has new stats
      TranslationStats newSelectedStats = new TranslationStats(new TransUnitCount(1, 2, 3), new TransUnitWords(4, 5, 6));
      DocumentInfo documentInfo = new DocumentInfo(docId, "a.po", "pot/", new LocaleId("en-US"), newSelectedStats, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      when(documentListPresenter.getDocumentInfo(docId)).thenReturn(documentInfo);
      // current view is editor
      presenter.showView(MainView.Editor);
      verify(display).setStats(statsCaptor.capture(), eq(true));
      assertThat(statsCaptor.getValue(), Matchers.sameInstance(selectedDocumentStats));

      // When:
      presenter.selectDocument(docId);

      // Then:
      display.setDocumentLabel("pot/", "a.po");
      verify(display, atLeastOnce()).setStats(newSelectedStats, true);
      assertThat(presenter.getSelectedDocIdOrNull(), Matchers.is(docId));
   }

   @Test
   public void canSelectDocumentNotInEditorView()
   {
      // Given:
      presenter.setStatesForTest(projectStats, selectedDocumentStats, MainView.Documents, null);
      DocumentId docId = new DocumentId(1L, "");
      // newly selected document has new stats
      TranslationStats newSelectedStats = new TranslationStats(new TransUnitCount(1, 2, 3), new TransUnitWords(4, 5, 6));
      DocumentInfo documentInfo = new DocumentInfo(docId, "a.po", "pot/", new LocaleId("en-US"), newSelectedStats, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      when(documentListPresenter.getDocumentInfo(docId)).thenReturn(documentInfo);

      // When:
      presenter.selectDocument(docId);

      // Then:
      verify(display).enableTab(MainView.Editor, true);
      
      verifyNoMoreInteractions(display);
      assertThat(presenter.getSelectedDocIdOrNull(), Matchers.is(docId));
      assertThat(presenter.getSelectedDocumentInfoOrNull(), Matchers.is(documentInfo));
   }

   @Test
   public void onDocumentStatsUpdateWillDoNothingIfNoSelectedDocument()
   {
      assertThat(presenter.getSelectedDocumentInfoOrNull(), Matchers.is(Matchers.nullValue()));

      presenter.onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(new DocumentId(1L, ""), new TranslationStats()));

      verifyZeroInteractions(display);
   }

   @Test
   public void onDocumentStatsUpdateWillDoNothingIsDifferentDocument()
   {
      // Given: current view is null and selected doc id 2
      presenter.setStatesForTest(projectStats, selectedDocumentStats, null, null);
      presenter.selectDocument(new DocumentId(2L, ""));

      // When:
      presenter.onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(new DocumentId(1L, ""), new TranslationStats()));

      
      // Then:
      verifyZeroInteractions(display);
   }

   @Test
   public void onDocumentStatsUpdateWillSetStatsIfSameDocument()
   {
      // Given: current view is Editor and selected doc id 1
      presenter.setStatesForTest(projectStats, selectedDocumentStats, null, null);
      presenter.showView(MainView.Editor);
      DocumentId docId = new DocumentId(1L, "");
      DocumentInfo documentInfo = new DocumentInfo(docId, "a.po", "pot/", new LocaleId("en-US"), selectedDocumentStats, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      when(documentListPresenter.getDocumentInfo(docId)).thenReturn(documentInfo);
      presenter.selectDocument(docId);
      verify(display, atLeastOnce()).setStats(selectedDocumentStats, true);

      // When:
      TranslationStats newStats = new TranslationStats(new TransUnitCount(9, 9, 9), new TransUnitWords(8, 8, 8));
      presenter.onDocumentStatsUpdated(new DocumentStatsUpdatedEvent(docId, newStats));

      // Then:
      assertThat(selectedDocumentStats.getUnitCount(), Matchers.equalTo(newStats.getUnitCount()));
      assertThat(selectedDocumentStats.getWordCount(), Matchers.equalTo(newStats.getWordCount()));
      verify(display, atLeastOnce()).setStats(newStats, true);
   }

   @Test
   public void onProjectStatsUpdate()
   {
      presenter.setStatesForTest(projectStats, selectedDocumentStats, MainView.Documents, null);

      TranslationStats newProjectStats = new TranslationStats();
      presenter.onProjectStatsRetrieved(new ProjectStatsUpdatedEvent(newProjectStats));

      assertThat(projectStats.getUnitCount(), Matchers.equalTo(newProjectStats.getUnitCount()));
      assertThat(projectStats.getWordCount(), Matchers.equalTo(newProjectStats.getWordCount()));
      verify(display).setStats(eq(newProjectStats), eq(true));
   }

   @Test
   public void onSearchAndReplaceClick()
   {
      when(history.getToken()).thenReturn("view:doc;doc:blah");

      presenter.onSearchAndReplaceClicked();

      verify(history).newItem("view:search;doc:blah");
   }

   @Test
   public void onDocumentsClickWillDoNothingIfNoSelectedDocument()
   {
      when(history.getToken()).thenReturn("doc:blah");

      presenter.onDocumentListClicked();

      verify(history).getToken();
      verifyNoMoreInteractions(history);
   }

   @Test
   public void onDocumentsClickWillFireNewHistoryItemAndSwitchToDocumentView()
   {
      // Given: current token is search view and has selected doc
      DocumentId docId = new DocumentId(1L, "");
      DocumentInfo documentInfo = new DocumentInfo(docId, "a.po", "pot/", new LocaleId("en-US"), selectedDocumentStats, "Translator", new Date(), new HashMap<String, String>(), "last translator", new Date());
      when(documentListPresenter.getDocumentInfo(docId)).thenReturn(documentInfo);
      when(history.getToken()).thenReturn("view:search;doc:pot/a.po");
      presenter.selectDocument(docId);

      // When:
      presenter.onDocumentListClicked();

      // Then:
      verify(history).getToken();
      verify(history).newItem("doc:pot/a.po");
      verifyNoMoreInteractions(history);
   }

   @Test
   public void onEditorClickAndSelectedDocumentIsNull()
   {
      presenter.onEditorClicked();

      verifyNoMoreInteractions(history);
   }

   @Test
   public void onEditorClickAndHasSelectedDocument()
   {
      DocumentInfo selectedDocument = mock(DocumentInfo.class);
      presenter.setStatesForTest(null, null, null, selectedDocument);
      when(history.getToken()).thenReturn("view:search");

      presenter.onEditorClicked();

      verify(history).newItem("view:doc");
   }

   @Test
   public void onEditorClickAndHasSelectedDocumentButAlreadyInEditorView()
   {
      DocumentInfo selectedDocument = mock(DocumentInfo.class);
      presenter.setStatesForTest(null, null, null, selectedDocument);
      when(history.getToken()).thenReturn("view:doc");

      presenter.onEditorClicked();
      verify(history).getToken();
      verifyNoMoreInteractions(history);
   }

   @Test
   public void onKeyShortcutClick()
   {
      presenter.onKeyShortcutsClicked();

      verify(keyShortcutPresenter).showShortcuts();
   }

   private static HasWorkspaceContextUpdateData contextUpdateData(final boolean projectActive)
   {
      return new HasWorkspaceContextUpdateData()
      {
         @Override
         public boolean isProjectActive()
         {
            return projectActive;
         }
      };
   }
}
