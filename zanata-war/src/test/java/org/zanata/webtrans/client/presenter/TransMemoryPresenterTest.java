package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.*;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.TranslationMemoryDisplay;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
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
   // object under test
   TransMemoryPresenter presenter;

   // injected mocks
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

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      presenter = new TransMemoryPresenter(display, eventBus, dispatcher, messages, transMemoryDetailsPresenter, userWorkspaceContext, transMemoryMergePresenter, keyShortcutPresenter);
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

      presenter.bind();
      presenter.onTMMergeClick();

      verify(transMemoryMergePresenter).prepareTMMerge();
   }

   @Test
   public void showDiffLegend()
   {
      when(display.getSearchType()).thenReturn(searchType);

      presenter.bind();
      presenter.showDiffLegend(true);

      verify(display).showDiffLegend(true);
   }

   @Test
   public void hideDiffLegend()
   {
      when(display.getSearchType()).thenReturn(searchType);

      presenter.bind();
      presenter.showDiffLegend(false);

      verify(display).showDiffLegend(false);
   }

   @Test
   public void showTMDetails()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), 0, 0);
      when(display.getSearchType()).thenReturn(searchType);

      presenter.bind();
      presenter.showTMDetails(object);

      verify(transMemoryDetailsPresenter).show(object);
   }

   @Test
   public void fireCopyEvent()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), 0, 0);
      ArgumentCaptor<CopyDataToEditorEvent> eventCaptor = ArgumentCaptor.forClass(CopyDataToEditorEvent.class);
      
      when(display.getSearchType()).thenReturn(searchType);

      presenter.bind();
      presenter.fireCopyEvent(object);

      verify(eventBus).fireEvent(eventCaptor.capture());
   }

   @Test
   public void fireSearchEvent()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug"), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1), "test", "test/path", LocaleId.EN_US, new TranslationStats());

      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(tMTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(workspaceContext);
      when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);

      presenter.bind();
      presenter.fireSearchEvent();

      verify(display).startProcessing();
      verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
   }

   @Test
   public void createTMRequestForTransUnit()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug"), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1), "test", "test/path", LocaleId.EN_US, new TranslationStats());

      TransUnitId tuid = new TransUnitId(1);
      ArrayList<String> sources = new ArrayList<String>();
      sources.add("test source");
      ArrayList<String> targets = new ArrayList<String>();
      targets.add("test target");
      ContentState state = ContentState.values()[2];
      TransUnit.Builder builder = TransUnit.Builder.newTransUnitBuilder().setId(tuid).setResId(tuid.toString()).setLocaleId(LocaleId.EN_US).setPlural(false).setSources(sources).setSourceComment("source comment").setTargets(targets).setStatus(state).setLastModifiedBy("peter").setMsgContext("msgContext").setRowIndex(0).setVerNum(1);

      when(display.getTmTextBox()).thenReturn(tMTextBox);
      when(tMTextBox.getText()).thenReturn("query");
      when(display.getSearchType()).thenReturn(searchType);
      when(searchType.getValue()).thenReturn(SearchType.FUZZY);
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(workspaceContext);
      when(workspaceContext.getWorkspaceId()).thenReturn(workspaceId);
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);

      presenter.bind();
      presenter.createTMRequestForTransUnit(builder.build());

      verify(display).startProcessing();
      verify(dispatcher).execute(getTMActionCaptor.capture(), callbackCaptor.capture());
   }

   @Test
   public void onFocus()
   {
      boolean isFocused = true;

      when(display.getSearchType()).thenReturn(searchType);

      presenter.bind();
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

      presenter.bind();
      presenter.onFocus(isFocused);

      verify(keyShortcutPresenter).setContextActive(ShortcutContext.TM, isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(keyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void clearContent()
   {
      when(display.getSearchType()).thenReturn(searchType);
      when(display.getTmTextBox()).thenReturn(tMTextBox);

      presenter.bind();
      presenter.clearContent();

      verify(tMTextBox).setText("");
      verify(display).clearTableContent();
   }

   @Test
   public void canFireSearchEvent()
   {
      // Given:
      LocaleId targetLocale = new LocaleId("zh");
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(TestFixture.workspaceContext(targetLocale));
      LocaleId sourceLocale = new LocaleId("en-US");
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(1), "doc.txt", "/pot", sourceLocale, new TranslationStats()));
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
   public void fireSearchEventCallbackOnSuccess()
   {
      // Given:
      LocaleId targetLocale = new LocaleId("zh");
      when(userWorkspaceContext.getWorkspaceContext()).thenReturn(TestFixture.workspaceContext(targetLocale));
      LocaleId sourceLocale = new LocaleId("en-US");
      when(userWorkspaceContext.getSelectedDoc()).thenReturn(new DocumentInfo(new DocumentId(1), "doc.txt", "/pot", sourceLocale, new TranslationStats()));
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

//   List<String> queries = submittedRequest.getQuery().getQueries();
//
//   if (!result.getMemories().isEmpty())
//   {
//      display.renderTable(result.getMemories(), queries);
//      currentResult = result.getMemories();
//      display.stopProcessing(true);
//   }
//   else
//   {
//      display.stopProcessing(false);
//   }

//   if (result.getRequest().equals(lastRequest))
//   {
//      Log.debug("received TM result for query");
//      displayTMResult(result);
//      lastRequest = null;
//   }
//   else
//   {
//      Log.debug("ignoring old TM result for query");
//      display.stopProcessing(false);
//   }
//   submittedRequest = null;
//   if (lastRequest != null)
//   {
//      // submit the waiting request
//      submitTMRequest(lastRequest);
//   }
}
