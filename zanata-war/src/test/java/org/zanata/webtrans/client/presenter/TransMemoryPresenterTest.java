package org.zanata.webtrans.client.presenter;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.TranslationStats;
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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;

@Test(groups = { "unit-tests" })
public class TransMemoryPresenterTest
{
   // object under test
   TransMemoryPresenter transMemoryPresenter;

   // injected mocks
   @Mock
   private TranslationMemoryDisplay mockDisplay;
   @Mock
   private EventBus mockEventBus;
   @Mock
   private Identity mockIdentity;
   @Mock
   private UserWorkspaceContext mockUserWorkspaceContext;
   @Mock
   private WorkspaceContext mockWorkspaceContext;
   @Mock
   private CachingDispatchAsync mockDispatcher;
   @Mock
   private TransMemoryDetailsPresenter mockTransMemoryDetailsPresenter;
   @Mock
   private WebTransMessages mockMessages;
   @Mock
   private KeyShortcutPresenter mockKeyShortcutPresenter;
   @Mock
   private TransMemoryMergePresenter mockTransMemoryMergePresenter;
   @Mock
   private HasValue<SearchType> mockSearchType;
   @Mock
   private HasText mockTMTextBox;
   @Mock
   private ArrayList<TransMemoryResultItem> mockCurrentResult;
   @Captor
   private ArgumentCaptor<GetTranslationMemory> GetTranslationCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetTranslationMemoryResult>> callbackCaptor;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      transMemoryPresenter = new TransMemoryPresenter(mockDisplay, mockEventBus, mockDispatcher, mockMessages, mockTransMemoryDetailsPresenter, mockUserWorkspaceContext, mockTransMemoryMergePresenter, mockKeyShortcutPresenter);
   }

   @Test
   public void onBind()
   {
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockMessages.searchTM()).thenReturn("Search TM");

      transMemoryPresenter.bind();
      
      verify(mockSearchType).setValue(SearchType.FUZZY);
      verify(mockEventBus).addHandler(TransUnitSelectionEvent.getType(), transMemoryPresenter);
      verify(mockEventBus).addHandler(TransMemoryShortcutCopyEvent.getType(), transMemoryPresenter);
      verify(mockDisplay).setListener(transMemoryPresenter);
      verify(mockKeyShortcutPresenter).register(isA(KeyShortcut.class));
   }

   @Test
   public void onTMMergeClick()
   {
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.onTMMergeClick();

      verify(mockTransMemoryMergePresenter).prepareTMMerge();
   }

   @Test
   public void showDiffLegend()
   {
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.showDiffLegend(true);

      verify(mockDisplay).showDiffLegend(true);
   }

   @Test
   public void hideDiffLegend()
   {
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.showDiffLegend(false);

      verify(mockDisplay).showDiffLegend(false);
   }

   @Test
   public void showTMDetails()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), 0, 0);
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.showTMDetails(object);

      verify(mockTransMemoryDetailsPresenter).show(object);
   }

   @Test
   public void fireCopyEvent()
   {
      TransMemoryResultItem object = new TransMemoryResultItem(new ArrayList<String>(), new ArrayList<String>(), 0, 0);
      ArgumentCaptor<CopyDataToEditorEvent> eventCaptor = ArgumentCaptor.forClass(CopyDataToEditorEvent.class);
      
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.fireCopyEvent(object);

      verify(mockEventBus).fireEvent(eventCaptor.capture());
   }

   @Test
   public void fireSearchEvent()
   {
      WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("projectSlug", "iterationSlug"), LocaleId.EN_US);
      DocumentInfo docInfo = new DocumentInfo(new DocumentId(1), "test", "test/path", LocaleId.EN_US, new TranslationStats());

      when(mockDisplay.getTmTextBox()).thenReturn(mockTMTextBox);
      when(mockTMTextBox.getText()).thenReturn("query");
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);
      when(mockUserWorkspaceContext.getWorkspaceContext()).thenReturn(mockWorkspaceContext);
      when(mockWorkspaceContext.getWorkspaceId()).thenReturn(workspaceId);
      when(mockUserWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);

      transMemoryPresenter.bind();
      transMemoryPresenter.fireSearchEvent();

      verify(mockDisplay).startProcessing();
      verify(mockDispatcher).execute(GetTranslationCaptor.capture(), callbackCaptor.capture());
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

      when(mockDisplay.getTmTextBox()).thenReturn(mockTMTextBox);
      when(mockTMTextBox.getText()).thenReturn("query");
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockSearchType.getValue()).thenReturn(SearchType.FUZZY);
      when(mockUserWorkspaceContext.getWorkspaceContext()).thenReturn(mockWorkspaceContext);
      when(mockWorkspaceContext.getWorkspaceId()).thenReturn(workspaceId);
      when(mockUserWorkspaceContext.getSelectedDoc()).thenReturn(docInfo);

      transMemoryPresenter.bind();
      transMemoryPresenter.createTMRequestForTransUnit(builder.build());

      verify(mockDisplay).startProcessing();
      verify(mockDispatcher).execute(GetTranslationCaptor.capture(), callbackCaptor.capture());
   }

   @Test
   public void onFocus()
   {
      boolean isFocused = true;

      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.onFocus(isFocused);

      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.TM, isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void onBlur()
   {
      boolean isFocused = false;

      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);

      transMemoryPresenter.bind();
      transMemoryPresenter.onFocus(isFocused);

      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.TM, isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Navigation, !isFocused);
      verify(mockKeyShortcutPresenter).setContextActive(ShortcutContext.Edit, !isFocused);
   }

   @Test
   public void clearContent()
   {
      when(mockDisplay.getSearchType()).thenReturn(mockSearchType);
      when(mockDisplay.getTmTextBox()).thenReturn(mockTMTextBox);

      transMemoryPresenter.bind();
      transMemoryPresenter.clearContent();

      verify(mockTMTextBox).setText("");
      verify(mockDisplay).clearTableContent();
   }
}
