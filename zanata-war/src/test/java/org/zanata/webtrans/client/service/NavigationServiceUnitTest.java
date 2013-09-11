package org.zanata.webtrans.client.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.BookmarkedTextFlowEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.RequestSelectTableRowEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.HasTransUnitUpdatedData;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class NavigationServiceUnitTest
{
   private static final int EDITOR_PAGE_SIZE = 3;
   private NavigationService service;
   @Mock
   private EventBus eventBus;
   @Mock
   private CachingDispatchAsync dispatcher;
   private GetTransUnitActionContext initContext;
   private List<TransUnit> data;
   private Map<TransUnitId,ContentState> idStateMap;
   private List<TransUnitId> idIndexList;
   @Captor
   private ArgumentCaptor<GwtEvent> eventCaptor;
   @Captor
   private ArgumentCaptor<GetTransUnitList> actionCaptor;
   @Captor
   private ArgumentCaptor<AbstractAsyncCallback<GetTransUnitListResult>> resultCaptor;
   @Mock
   private NavigationService.PageDataChangeListener pageDataChangeListener;
   @Mock
   private History history;
   private GetTransUnitActionContextHolder contextHolder;


   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      initData();
      UserConfigHolder  configHolder = new UserConfigHolder();
      configHolder.setEditorPageSize(EDITOR_PAGE_SIZE);
      SinglePageDataModelImpl pageModel = new SinglePageDataModelImpl();
      ModalNavigationStateHolder navigationStateHolder = new ModalNavigationStateHolder(configHolder);
      contextHolder = new GetTransUnitActionContextHolder(configHolder);
      contextHolder.initContext(TestFixture.documentInfo(1, "a.pot"), null, null);
      service = new NavigationService(eventBus, dispatcher, configHolder, mock(TableEditorMessages.class), pageModel, navigationStateHolder, contextHolder, history);
      service.addPageDataChangeListener(pageDataChangeListener);

      verify(eventBus).addHandler(DocumentSelectionEvent.getType(), service);
      verify(eventBus).addHandler(FindMessageEvent.getType(), service);
      verify(eventBus).addHandler(NavTransUnitEvent.getType(), service);
      verify(eventBus).addHandler(EditorPageSizeChangeEvent.TYPE, service);

      pageModel.setData(data.subList(0, configHolder.getState().getEditorPageSize()));
      navigationStateHolder.init(idStateMap, idIndexList);
      initContext = new GetTransUnitActionContext(TestFixture.documentInfo(1, "")).changeCount(configHolder.getState().getEditorPageSize());
   }

   private void initData()
   {
      // @formatter:off
      data = Lists.newArrayList(
            TestFixture.makeTransUnit(1, ContentState.Approved),
            TestFixture.makeTransUnit(2, ContentState.Approved),
            TestFixture.makeTransUnit(3, ContentState.NeedReview),
            TestFixture.makeTransUnit(4, ContentState.New),
            TestFixture.makeTransUnit(5, ContentState.Approved)
      );
      // @formatter:on
      idStateMap = Maps.newHashMap();
      idIndexList = Lists.newArrayList();
      for (TransUnit transUnit : data)
      {
         idStateMap.put(transUnit.getId(), transUnit.getStatus());
         idIndexList.add(transUnit.getId());
      }
   }

   @Test
   public void onNavigationEventOnSamePage()
   {
      service.init(initContext);
      service.selectByRowIndex(0);

      service.onNavTransUnit(NavTransUnitEvent.FIRST_ENTRY_EVENT);

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(data.get(0).getId()));
   }

   @Test
   public void onNavigationEventOnDifferentPage()
   {
      service.init(initContext);
      service.selectByRowIndex(0);

      service.onNavTransUnit(NavTransUnitEvent.LAST_ENTRY_EVENT);

      verify(dispatcher, times(2)).execute(actionCaptor.capture(), resultCaptor.capture());
      GetTransUnitList action = actionCaptor.getValue();
      assertThat(action.getOffset(), Matchers.equalTo(3));
      assertThat(action.getCount(), Matchers.equalTo(EDITOR_PAGE_SIZE));
      assertThat(action.getTargetTransUnitId(), Matchers.equalTo(data.get(data.size() - 1).getId()));
   }

   @Test
   public void onNextEntry()
   {
      service.init(initContext);
      service.selectByRowIndex(0);

      service.onNavTransUnit(NavTransUnitEvent.NEXT_ENTRY_EVENT);

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(data.get(1).getId()));
   }

   @Test
   public void onPreviousEntry()
   {
      service.init(initContext);
      service.selectByRowIndex(2);
      service.onNavTransUnit(NavTransUnitEvent.PREV_ENTRY_EVENT);

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(data.get(1).getId()));
   }

   @Test
   public void onNextState()
   {
      service.init(initContext);
      service.selectByRowIndex(0);

      service.onNavTransUnit(NavTransUnitEvent.NEXT_STATE_EVENT);

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(data.get(2).getId()));
   }

   @Test
   public void onPreviousState()
   {
      service.init(initContext);
      service.selectByRowIndex(2);

      service.onNavTransUnit(NavTransUnitEvent.PREV_STATE_EVENT);

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(data.get(2).getId()));
   }

   @Test
   public void testOnTransUnitUpdatedInCurrentPage() throws Exception
   {
      // Given: updated trans unit is from same document and it's on current page
      service.init(initContext);
      HasTransUnitUpdatedData updatedData = mock(HasTransUnitUpdatedData.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
      when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(initContext.getDocument().getId());
      TransUnit updatedTU = data.get(0);
      when(updatedData.getUpdateInfo().getTransUnit()).thenReturn(updatedTU);
      EditorClientId editorClientId = new EditorClientId("sessionId", 1);
      when(updatedData.getEditorClientId()).thenReturn(editorClientId);
      when(updatedData.getUpdateType()).thenReturn(TransUnitUpdated.UpdateType.WebEditorSave);

      // When:
      service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

      // Then:
      verify(pageDataChangeListener).refreshRow(updatedTU, editorClientId, TransUnitUpdated.UpdateType.WebEditorSave);

   }

   @Test
   public void testOnTransUnitUpdatedNotInCurrentPage() throws Exception
   {
      // Given: updated trans unit is from same document but NOT on current page
      service.init(initContext);
      HasTransUnitUpdatedData updatedData = mock(HasTransUnitUpdatedData.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
      when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(initContext.getDocument().getId());
      // updated TU has something different so that we can assert it won't update current page data model
      TransUnit updatedTU = TransUnit.Builder.from(data.get(data.size() - 1)).setSourceComment("different").build();
      when(updatedData.getUpdateInfo().getTransUnit()).thenReturn(updatedTU);

      // When:
      service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

      // Then:
      verifyZeroInteractions(pageDataChangeListener);
      assertThat(data.get(data.size() - 1).getSourceComment(), Matchers.not(Matchers.equalTo(updatedTU.getSourceComment())));

   }

   @Test
   public void testOnTransUnitUpdatedNotInCurrentDocument() throws Exception
   {
      // Given: updated trans unit is from another document
      service.init(initContext);
      HasTransUnitUpdatedData updatedData = mock(HasTransUnitUpdatedData.class, withSettings().defaultAnswer(RETURNS_DEEP_STUBS));
      when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(initContext.getDocument().getId());

      // When:
      service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

      // Then:
      verifyZeroInteractions(pageDataChangeListener);

   }

   @Test
   public void testUpdateDataModel() throws Exception
   {
      service.init(initContext);
      service.selectByRowIndex(0);
      assertThat(service.getSelectedOrNull().getStatus(), Matchers.equalTo(ContentState.Approved));

      service.updateDataModel(TestFixture.makeTransUnit(service.getSelectedOrNull().getId().getId(), ContentState.NeedReview));

      assertThat(service.getSelectedOrNull().getStatus(), Matchers.equalTo(ContentState.NeedReview));
   }

   @Test
   public void testOnFindMessage() throws Exception
   {
      service.init(initContext);

      service.onFindMessage(new FindMessageEvent("search"));

      verify(dispatcher, times(2)).execute(actionCaptor.capture(), resultCaptor.capture());
      GetTransUnitList getTransUnitList = actionCaptor.getValue();
      assertThat(getTransUnitList.getPhrase(), Matchers.equalTo("search"));
   }

   @Test
   public void testOnDocumentSelected() throws Exception
   {
      DocumentInfo documentInfo = TestFixture.documentInfo(2, "");
      DocumentId documentId = documentInfo.getId();
      service.onDocumentSelected(new DocumentSelectionEvent(documentInfo));

      verify(dispatcher).execute(actionCaptor.capture(), resultCaptor.capture());
      GetTransUnitList getTransUnitList = actionCaptor.getValue();
      assertThat(getTransUnitList.getDocumentId(), Matchers.equalTo(documentId));
   }

   @Test
   public void testOnPageSizeChange() throws Exception
   {
      service.init(initContext);

      service.onPageSizeChange(new EditorPageSizeChangeEvent(5));

      verify(dispatcher, times(2)).execute(actionCaptor.capture(), resultCaptor.capture());
      GetTransUnitList getTransUnitList = actionCaptor.getValue();
      assertThat(getTransUnitList.getCount(), Matchers.equalTo(5));

   }

   @Test
   public void testSelectByRowIndex() throws Exception
   {
      service.init(initContext);
      service.selectByRowIndex(1);

      assertThat(service.getCurrentRowIndexOnPage(), Matchers.equalTo(1));
      assertThat(service.getSelectedOrNull(), Matchers.equalTo(data.get(1)));
   }

   @Test
   public void testFindRowIndexById() throws Exception
   {
      assertThat(service.findRowIndexById(new TransUnitId(2)), Matchers.equalTo(1));

      // not in current page
      assertThat(service.findRowIndexById(new TransUnitId(99)), Matchers.equalTo(NavigationService.UNDEFINED));
   }

   @Test
   public void testGetSelectedOrNull() throws Exception
   {
      service.init(initContext);

      assertThat(service.getSelectedOrNull(), Matchers.nullValue());

      service.selectByRowIndex(1);

      assertThat(service.getSelectedOrNull(), Matchers.equalTo(data.get(1)));
   }

   @Test
   public void testGetCurrentPageValues() throws Exception
   {
      assertThat(service.getCurrentPageValues(), Matchers.equalTo(data.subList(0, EDITOR_PAGE_SIZE)));
   }

   @Test
   public void testGetByIdOrNull() throws Exception
   {
      assertThat(service.getByIdOrNull(new TransUnitId(2)), Matchers.equalTo(data.get(1)));

      // not in current page
      assertThat(service.getByIdOrNull(new TransUnitId(99)), Matchers.nullValue());
   }

   @Test
   public void onBookmarkedTextFlowOnSamePage()
   {
      contextHolder.changeOffset(1);
      TransUnitId targetId = new TransUnitId(1);

      service.onBookmarkableTextFlow(new BookmarkedTextFlowEvent(1, targetId));

      verify(eventBus).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent event = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      assertThat(event.getSelectedId(), Matchers.equalTo(targetId));
      verifyZeroInteractions(dispatcher);
   }

   @Test
   public void onBookmarkedTextFlowOnDifferentPage()
   {
      // current offset is 1
      contextHolder.changeOffset(1);
      TransUnitId targetId = new TransUnitId(1);
      // target offset is 2
      BookmarkedTextFlowEvent bookmarkedTextFlowEvent = new BookmarkedTextFlowEvent(2, targetId);
      NavigationService serviceSpy = spy(service);
      doNothing().when(serviceSpy).execute(bookmarkedTextFlowEvent);

      serviceSpy.onBookmarkableTextFlow(bookmarkedTextFlowEvent);

      verify(serviceSpy).execute(bookmarkedTextFlowEvent);
   }

   @Test
   public void onRequestSelectTableRowSamePage()
   {
      TransUnitId selectingId = new TransUnitId(1);
      
      DocumentInfo docInfo = TestFixture.documentInfo();
      
      HistoryToken mockHistoryToken = mock(HistoryToken.class);
      when(history.getHistoryToken()).thenReturn(mockHistoryToken);

      // When:
      service.onRequestSelectTableRow(new RequestSelectTableRowEvent(docInfo, selectingId));

      // Then:
      verify(mockHistoryToken).setView(MainView.Editor);
      verify(mockHistoryToken).setDocumentPath(docInfo.getPath() + docInfo.getName());
      verify(mockHistoryToken).clearEditorFilterAndSearch();
      verify(mockHistoryToken).setTextFlowId(selectingId.toString());
   }
}
