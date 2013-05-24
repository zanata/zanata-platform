package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

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
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.model.TestFixture;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.TranslationMemoryDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.AuditInfo;
import org.zanata.webtrans.shared.model.DiffMode;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class TransMemoryPresenterTest
{
   private TransMemoryPresenter presenter;

   @Mock
   private TranslationMemoryDisplay display;
   @Mock
   private EventBus eventBus;
   @Mock
   private Identity identity;
   @Mock
   private UserWorkspaceContext userWorkspaceContext;
   @Mock
   private WorkspaceContext workspaceContext;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private TransMemoryDetailsPresenter transMemoryDetailsPresenter;
   @Mock
   private WebTransMessages messages;
   @Mock
   private KeyShortcutPresenter keyShortcutPresenter;
   @Mock
   private TransMemoryMergePresenter transMemoryMergePresenter;
   @Mock
   private HasValue<SearchType> searchType;
   @Mock
   private HasText tMTextBox;
   @Mock
   private ArrayList<TransMemoryResultItem> currentResult;
   @Captor
   private ArgumentCaptor<GetTranslationMemory> getTMActionCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetTranslationMemoryResult>> callbackCaptor;
   @Mock
   private TransMemoryResultItem transMemoryResultItem;
   @Captor
   private ArgumentCaptor<CopyDataToEditorEvent> copyTMEventCaptor;
   private UserConfigHolder configHolder;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      configHolder = new UserConfigHolder();
      presenter = new TransMemoryPresenter(display, eventBus, dispatcher, messages, transMemoryDetailsPresenter, userWorkspaceContext, transMemoryMergePresenter, keyShortcutPresenter, configHolder);

      verify(display).setDisplayMode(configHolder.getState().getTransMemoryDisplayMode());
   }

   @Test
   public void onBind()
   {
      when(display.getSearchType()).thenReturn(searchType);
      when(messages.searchTM()).thenReturn("Search TM");

      presenter.bind();
      
      verify(searchType).setValue(SearchType.FUZZY);
      verify(eventBus).addHandler(TransUnitSelectionEvent.getType(), presenter);
      verify(eventBus).addHandler(TransMemoryShortcutCopyEvent.getType(), presenter);
      verify(display).setListener(presenter);
      verify(keyShortcutPresenter).register(isA(KeyShortcut.class));
   }

   @Test
   public void onTMMergeClick()
   {
      when(display.getSearchType()).thenReturn(searchType);

      presenter.onTMMergeClick();

      verify(transMemoryMergePresenter).prepareTMMerge();
   }

   @Test
   public void showDiffLegend()
   {
      when(display.getSearchType()).thenReturn(searchType);

      presenter.showDiffLegend(true);

      verify(display).showDiffLegend(true);
   }

   @Test
   public void hideDiffLegend()
   {
      when(display.getSearchType()).thenReturn(searchType);

      presenter.showDiffLegend(false);

      verify(display).showDiffLegend(false);
   }

   @Test
   public void showTMDetails()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), ContentState.Approved, 0, 0);
      when(display.getSearchType()).thenReturn(searchType);

      presenter.showTMDetails(object);

      verify(transMemoryDetailsPresenter).show(object);
   }

   @Test
   public void fireCopyEvent()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), ContentState.Approved, 0, 0);
      ArgumentCaptor<CopyDataToEditorEvent> eventCaptor = ArgumentCaptor.forClass(CopyDataToEditorEvent.class);
      
      when(display.getSearchType()).thenReturn(searchType);

      presenter.fireCopyEvent(object);

      verify(eventBus).fireEvent(eventCaptor.capture());
   }

   @Test
   public void createTMRequestForTransUnit()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug", ProjectType.Podir), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(new Long(1), ""), "test", "test/path", LocaleId.EN_US, new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator"));

      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(tMTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(workspaceContext);
      when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);

      presenter.createTMRequestForTransUnit(TestFixture.makeTransUnit(1));

      verify(display).startProcessing();
      verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
   }

   @Test
   public void willDoNothingIfAlreadyHaveSubmittedRequest()
   {
      // Given: already have submitted request
      GetTranslationMemory submittedRequest = mock(GetTranslationMemory.class);
      presenter.setStatesForTesting(null, submittedRequest);
      LocaleId localeId = new LocaleId("zh");
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master", ProjectType.Podir);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId()));
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(new Long(1), ""), "doc.txt", "/pot", new LocaleId("en-US"), new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator")));

      // When:
      presenter.createTMRequestForTransUnit(TestFixture.makeTransUnit(1));

      // Then:
      verifyZeroInteractions(dispatcher);
   }

   @Test
   public void onFocus()
   {
      boolean isFocused = true;

      when(display.getSearchType()).thenReturn(searchType);

      presenter.onFocus(isFocused);

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.TM, isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void onBlur()
   {
      boolean isFocused = false;

      when(display.getSearchType()).thenReturn(searchType);

      presenter.onFocus(isFocused);

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.TM, isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void canFireSearchEvent()
   {
      // Given:
      LocaleId targetLocale = new LocaleId("zh");
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master", ProjectType.Podir);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(new WorkspaceContext(new WorkspaceId(projectIterationId, targetLocale), "workspaceName", targetLocale.getId()));
      LocaleId sourceLocale = new LocaleId("en-US");
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(new Long(1), ""), "doc.txt", "/pot", sourceLocale, new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator")));
      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(tMTextBox.getText()).thenReturn("search query");
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);

      // When:
      presenter.fireSearchEvent();

      // Then:
      InOrder inOrder = inOrder(display, dispatcher);
      inOrder.verify(display).startProcessing();
      inOrder.verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
      // verify action
      GetTranslationMemory action = getTMActionCaptor.getValue();
      assertThat(action.getSearchType(), Matchers.equalTo(SearchType.FUZZY));
      assertThat(action.getLocaleId(), Matchers.equalTo(targetLocale));
      assertThat(action.getSourceLocaleId(), Matchers.equalTo(sourceLocale));
      assertThat(action.getQuery().getQueries(), Matchers.contains("search query"));
   }

   @Test
   public void fireSearchEventCallbackOnFailure()
   {
      // Given:
      LocaleId localeId = new LocaleId("zh");
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master", ProjectType.Podir);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId()));
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(new Long(1), ""), "doc.txt", "/pot", new LocaleId("en-US"), new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator")));
      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(display.getSearchType()).thenReturn(searchType);

      // When:
      presenter.fireSearchEvent();

      // Then:
      InOrder inOrder = inOrder(display, dispatcher);
      inOrder.verify(display).startProcessing();
      inOrder.verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
      // verify callback on failure
      AsyncCallback<GetTranslationMemoryResult> callback = callbackCaptor.getValue();
      callback.onFailure(new RuntimeException("fail"));

      inOrder.verify(display).stopProcessing(false);
   }

   @Test
   public void fireSearchEventCallbackOnSuccess()
   {
      // Given:
      LocaleId localeId = new LocaleId("zh");
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master", ProjectType.Podir);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId()));
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(new Long(1), ""), "doc.txt", "/pot", new LocaleId("en-US"), new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator")));
      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(tMTextBox.getText()).thenReturn("search query");
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);

      // When:
      presenter.fireSearchEvent();

      // Then:
      InOrder inOrder = inOrder(display, dispatcher);
      inOrder.verify(display).startProcessing();
      inOrder.verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
      // verify callback on success
      AsyncCallback<GetTranslationMemoryResult> callback = callbackCaptor.getValue();
      ArrayList<TransMemoryResultItem> transMemories = Lists.newArrayList(transMemoryResultItem);
      callback.onSuccess(new GetTranslationMemoryResult(getTMActionCaptor.getValue(), transMemories));

      inOrder.verify(display).renderTable(transMemories, Lists.newArrayList("search query"));
      inOrder.verify(display).stopProcessing(true);
   }

   @Test
   public void fireSearchEventCallbackOnSuccessButResultIsEmpty()
   {
      // Given:
      LocaleId localeId = new LocaleId("zh");
      ProjectIterationId projectIterationId = new ProjectIterationId("project", "master", ProjectType.Podir);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(new WorkspaceContext(new WorkspaceId(projectIterationId, localeId), "workspaceName", localeId.getId()));
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(new Long(1), ""), "doc.txt", "/pot", new LocaleId("en-US"), new ContainerTranslationStatistics(), new AuditInfo(new Date(), "Translator"), new HashMap<String, String>(), new AuditInfo(new Date(), "last translator")));
      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);

      // When:
      presenter.fireSearchEvent();

      // Then:
      InOrder inOrder = inOrder(display, dispatcher);
      inOrder.verify(display).startProcessing();
      inOrder.verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
      // verify callback on success
      AsyncCallback<GetTranslationMemoryResult> callback = callbackCaptor.getValue();
      ArrayList<TransMemoryResultItem> transMemories = Lists.newArrayList();
      callback.onSuccess(new GetTranslationMemoryResult(getTMActionCaptor.getValue(), transMemories));

      inOrder.verify(display).stopProcessing(false);
   }

   @Test
   public void testOnTransUnitSelected()
   {
      TransUnit selection = TestFixture.makeTransUnit(1);
      TransMemoryPresenter spyPresenter = spy(presenter);
      doNothing().when(spyPresenter).createTMRequestForTransUnit(selection);

      spyPresenter.onTransUnitSelected(new TransUnitSelectionEvent(selection));

      verify(spyPresenter).createTMRequestForTransUnit(selection);
   }

   @Test
   public void testOnTransMemoryCopy()
   {
      presenter.setStatesForTesting(Lists.newArrayList(transMemoryResultItem), null);
      when(userWorkspaceContext.hasReadOnlyAccess()).thenReturn(false);
      List<String> targetContents = Lists.newArrayList("a");
      when(transMemoryResultItem.getTargetContents()).thenReturn(targetContents);

      presenter.onTransMemoryCopy(new TransMemoryShortcutCopyEvent(0));

      verify(eventBus).fireEvent(copyTMEventCaptor.capture());
      assertThat(copyTMEventCaptor.getValue().getTargetResult(), Matchers.equalTo(targetContents));
   }

   @Test
   public void onClearContent()
   {
      List<TransMemoryResultItem> currentResult = Lists.newArrayList(transMemoryResultItem);
      presenter.setStatesForTesting(currentResult, null);
      when(display.getTmTextBox()).thenReturn(tMTextBox);

      presenter.clearContent();

      verify(tMTextBox).setText("");
      verify(display).clearTableContent();
      assertThat(currentResult, Matchers.<TransMemoryResultItem>empty());
   }

   @Test
   public void onDiffModeChanged()
   {
      List<TransMemoryResultItem> currentResult = Lists.newArrayList(transMemoryResultItem);
      presenter.setStatesForTesting(currentResult, null);

      presenter.onDiffModeChanged();

      verify(display).redrawTable(currentResult);
   }

   @Test
   public void willIgnoreDiffModeChangeIfNoCurrentResult()
   {
      presenter.setStatesForTesting(null, null);

      presenter.onDiffModeChanged();

      verify(display, never()).redrawTable(Mockito.anyList());
   }

   @Test
   public void onEditorConfigOptionChange()
   {
      List<TransMemoryResultItem> currentResult = Lists.newArrayList(transMemoryResultItem);
      presenter.setStatesForTesting(currentResult, null);
      configHolder.setTMDisplayMode(DiffMode.HIGHLIGHT);

      presenter.onUserConfigChanged(UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT);

      verify(display).setDisplayMode(configHolder.getState().getTransMemoryDisplayMode());
      verify(display).redrawTable(currentResult);
   }

   @Test
   public void ignoreIfNotEditorConfigOptionChange()
   {
      presenter.onUserConfigChanged(UserConfigChangeEvent.DOCUMENT_CONFIG_CHANGE_EVENT);

      verifyNoMoreInteractions(display);
   }
}
