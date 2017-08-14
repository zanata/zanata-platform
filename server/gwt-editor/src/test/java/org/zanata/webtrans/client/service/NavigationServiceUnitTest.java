package org.zanata.webtrans.client.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.zanata.webtrans.test.GWTTestData.documentInfo;
import static org.zanata.webtrans.test.GWTTestData.extractFromEvents;
import static org.zanata.webtrans.test.GWTTestData.makeTransUnit;

import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.BookmarkedTextFlowEvent;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.EditorPageSizeChangeEvent;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.RequestSelectTableRowEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEventHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.MainView;
import org.zanata.webtrans.shared.model.GetTransUnitActionContext;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
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
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class NavigationServiceUnitTest {
    private static final int EDITOR_PAGE_SIZE = 3;
    private NavigationService service;
    @Mock
    private EventBus eventBus;
    @Mock
    private CachingDispatchAsync dispatcher;
    private GetTransUnitActionContext initContext;
    private List<TransUnit> data;
    private Map<TransUnitId, ContentState> idStateMap;
    private List<TransUnitId> idIndexList;
    @Captor
    private ArgumentCaptor<GwtEvent<TableRowSelectedEventHandler>> eventCaptor;
    @Captor
    private ArgumentCaptor<GetTransUnitList> actionCaptor;
    @Captor
    private ArgumentCaptor<AbstractAsyncCallback<GetTransUnitListResult>> resultCaptor;
    @Mock
    private NavigationService.PageDataChangeListener pageDataChangeListener;
    @Mock
    private History history;
    private GetTransUnitActionContextHolder contextHolder;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        initData();
        UserConfigHolder configHolder = new UserConfigHolder();
        configHolder.setEditorPageSize(EDITOR_PAGE_SIZE);
        SinglePageDataModelImpl pageModel = new SinglePageDataModelImpl();
        ModalNavigationStateHolder navigationStateHolder =
                new ModalNavigationStateHolder(configHolder);
        contextHolder = new GetTransUnitActionContextHolder(configHolder);
        contextHolder.initContext(documentInfo(1, "a.pot"), null,
                null);
        service =
                new NavigationService(eventBus, dispatcher, configHolder,
                        mock(TableEditorMessages.class), pageModel,
                        navigationStateHolder, contextHolder, history);
        service.addPageDataChangeListener(pageDataChangeListener);

        verify(eventBus).addHandler(DocumentSelectionEvent.getType(), service);
        verify(eventBus).addHandler(NavTransUnitEvent.getType(), service);
        verify(eventBus).addHandler(EditorPageSizeChangeEvent.TYPE, service);

        pageModel.setData(data.subList(0, configHolder.getState()
                .getEditorPageSize()));
        navigationStateHolder.init(idStateMap, idIndexList);
        initContext =
                new GetTransUnitActionContext(documentInfo(1, ""))
                        .withCount(configHolder.getState()
                                .getEditorPageSize());
    }

    private void initData() {
        // @formatter:off
        data = Lists.newArrayList(
            makeTransUnit(1, ContentState.Approved),
            makeTransUnit(2, ContentState.Approved),
            makeTransUnit(3, ContentState.NeedReview),
            makeTransUnit(4, ContentState.New),
            makeTransUnit(5, ContentState.Approved)
        );
        // @formatter:on
        idStateMap = Maps.newHashMap();
        idIndexList = Lists.newArrayList();
        for (TransUnit transUnit : data) {
            idStateMap.put(transUnit.getId(), transUnit.getStatus());
            idIndexList.add(transUnit.getId());
        }
    }

    @Test
    public void onNavigationEventOnSamePage() {
        service.init(initContext);
        service.selectByRowIndex(0);

        service.onNavTransUnit(NavTransUnitEvent.FIRST_ENTRY_EVENT);

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId())
                .isEqualTo(data.get(0).getId());
    }

    @Test
    public void onNavigationEventOnDifferentPage() {
        service.init(initContext);
        service.selectByRowIndex(0);

        service.onNavTransUnit(NavTransUnitEvent.LAST_ENTRY_EVENT);

        verify(dispatcher, times(2)).execute(actionCaptor.capture(),
                resultCaptor.capture());
        GetTransUnitList action = actionCaptor.getValue();
        assertThat(action.getOffset()).isEqualTo(3);
        assertThat(action.getCount()).isEqualTo(EDITOR_PAGE_SIZE);
        assertThat(action.getTargetTransUnitId())
                .isEqualTo(data.get(data.size() - 1).getId());
    }

    @Test
    public void onNextEntry() {
        service.init(initContext);
        service.selectByRowIndex(0);

        service.onNavTransUnit(NavTransUnitEvent.NEXT_ENTRY_EVENT);

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId())
                .isEqualTo(data.get(1).getId());
    }

    @Test
    public void onPreviousEntry() {
        service.init(initContext);
        service.selectByRowIndex(2);
        service.onNavTransUnit(NavTransUnitEvent.PREV_ENTRY_EVENT);

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId())
                .isEqualTo(data.get(1).getId());
    }

    @Test
    public void onNextState() {
        service.init(initContext);
        service.selectByRowIndex(0);

        service.onNavTransUnit(NavTransUnitEvent.NEXT_STATE_EVENT);

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId())
                .isEqualTo(data.get(2).getId());
    }

    @Test
    public void onPreviousState() {
        service.init(initContext);
        service.selectByRowIndex(2);

        service.onNavTransUnit(NavTransUnitEvent.PREV_STATE_EVENT);

        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(tableRowSelectedEvent.getSelectedId())
                .isEqualTo(data.get(2).getId());
    }

    @Test
    public void testOnTransUnitUpdatedInCurrentPage() throws Exception {
        // Given: updated trans unit is from same document and it's on current
        // page
        service.init(initContext);
        HasTransUnitUpdatedData updatedData =
                mock(HasTransUnitUpdatedData.class, withSettings()
                        .defaultAnswer(RETURNS_DEEP_STUBS));
        when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(
                initContext.getDocument().getId());
        TransUnit updatedTU = data.get(0);
        when(updatedData.getUpdateInfo().getTransUnit()).thenReturn(updatedTU);
        EditorClientId editorClientId = new EditorClientId("sessionId", 1);
        when(updatedData.getEditorClientId()).thenReturn(editorClientId);
        when(updatedData.getUpdateType()).thenReturn(
                TransUnitUpdated.UpdateType.WebEditorSave);

        // When:
        service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

        // Then:
        verify(pageDataChangeListener).refreshRow(updatedTU, editorClientId,
                TransUnitUpdated.UpdateType.WebEditorSave);

    }

    @Test
    public void testOnTransUnitUpdatedNotInCurrentPage() throws Exception {
        // Given: updated trans unit is from same document but NOT on current
        // page
        service.init(initContext);
        HasTransUnitUpdatedData updatedData =
                mock(HasTransUnitUpdatedData.class, withSettings()
                        .defaultAnswer(RETURNS_DEEP_STUBS));
        when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(
                initContext.getDocument().getId());
        // updated TU has something different so that we can assert it won't
        // update current page data model
        TransUnit updatedTU =
                TransUnit.Builder.from(data.get(data.size() - 1))
                        .setSourceComment("different").build();
        when(updatedData.getUpdateInfo().getTransUnit()).thenReturn(updatedTU);

        // When:
        service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

        // Then:
        verifyZeroInteractions(pageDataChangeListener);
        assertThat(data.get(data.size() - 1).getSourceComment())
                .isNotEqualTo(updatedTU.getSourceComment());
    }

    @Test
    public void testOnTransUnitUpdatedNotInCurrentDocument() throws Exception {
        // Given: updated trans unit is from another document
        service.init(initContext);
        HasTransUnitUpdatedData updatedData =
                mock(HasTransUnitUpdatedData.class, withSettings()
                        .defaultAnswer(RETURNS_DEEP_STUBS));
        when(updatedData.getUpdateInfo().getDocumentId()).thenReturn(
                initContext.getDocument().getId());

        // When:
        service.onTransUnitUpdated(new TransUnitUpdatedEvent(updatedData));

        // Then:
        verifyZeroInteractions(pageDataChangeListener);

    }

    @Test
    public void testUpdateDataModel() throws Exception {
        service.init(initContext);
        service.selectByRowIndex(0);
        assertThat(service.getSelectedOrNull().getStatus())
                .isEqualTo(ContentState.Approved);

        service.updateDataModel(makeTransUnit(service
                .getSelectedOrNull().getId().getId(), ContentState.NeedReview));

        assertThat(service.getSelectedOrNull().getStatus())
                .isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void testOnDocumentSelected() throws Exception {
        DocumentInfo documentInfo = documentInfo(2, "");
        DocumentId documentId = documentInfo.getId();
        service.onDocumentSelected(new DocumentSelectionEvent(documentInfo));

        verify(dispatcher).execute(actionCaptor.capture(),
                resultCaptor.capture());
        GetTransUnitList getTransUnitList = actionCaptor.getValue();
        assertThat(getTransUnitList.getDocumentId()).isEqualTo(documentId);
    }

    @Test
    public void testOnPageSizeChange() throws Exception {
        service.init(initContext);

        service.onPageSizeChange(new EditorPageSizeChangeEvent(5));

        verify(dispatcher, times(2)).execute(actionCaptor.capture(),
                resultCaptor.capture());
        GetTransUnitList getTransUnitList = actionCaptor.getValue();
        assertThat(getTransUnitList.getCount()).isEqualTo(5);

    }

    @Test
    public void testSelectByRowIndex() throws Exception {
        service.init(initContext);
        service.selectByRowIndex(1);

        assertThat(service.getCurrentRowIndexOnPage()).isEqualTo(1);
        assertThat(service.getSelectedOrNull()).isEqualTo(data.get(1));
    }

    @Test
    public void testFindRowIndexById() throws Exception {
        assertThat(service.findRowIndexById(new TransUnitId(2)))
                .isEqualTo(1);

        // not in current page
        assertThat(service.findRowIndexById(new TransUnitId(99)))
                .isEqualTo(NavigationService.UNDEFINED);
    }

    @Test
    public void testGetSelectedOrNull() throws Exception {
        service.init(initContext);

        assertThat(service.getSelectedOrNull()).isNull();

        service.selectByRowIndex(1);

        assertThat(service.getSelectedOrNull()).isEqualTo(data.get(1));
    }

    @Test
    public void testGetCurrentPageValues() throws Exception {
        assertThat(service.getCurrentPageValues())
                .isEqualTo(data.subList(0, EDITOR_PAGE_SIZE));
    }

    @Test
    public void testGetByIdOrNull() throws Exception {
        assertThat(service.getByIdOrNull(new TransUnitId(2)))
                .isEqualTo(data.get(1));

        // not in current page
        assertThat(service.getByIdOrNull(new TransUnitId(99))).isNull();
    }

    @Test
    public void onBookmarkedTextFlowOnSamePage() {
        contextHolder.changeOffset(1);
        TransUnitId targetId = new TransUnitId(1);

        service.onBookmarkableTextFlow(new BookmarkedTextFlowEvent(1, targetId));

        verify(eventBus).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent event =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        assertThat(event.getSelectedId()).isEqualTo(targetId);
        verifyZeroInteractions(dispatcher);
    }

    @Test
    public void onBookmarkedTextFlowOnDifferentPage() {
        // current offset is 1
        contextHolder.changeOffset(1);
        TransUnitId targetId = new TransUnitId(1);
        // target offset is 2
        BookmarkedTextFlowEvent bookmarkedTextFlowEvent =
                new BookmarkedTextFlowEvent(2, targetId);
        NavigationService serviceSpy = spy(service);
        doNothing().when(serviceSpy).execute(bookmarkedTextFlowEvent);

        serviceSpy.onBookmarkableTextFlow(bookmarkedTextFlowEvent);

        verify(serviceSpy).execute(bookmarkedTextFlowEvent);
    }

    @Test
    public void onRequestSelectTableRowSamePage() {
        TransUnitId selectingId = new TransUnitId(1);

        DocumentInfo docInfo = documentInfo();

        HistoryToken mockHistoryToken = mock(HistoryToken.class);
        when(history.getHistoryToken()).thenReturn(mockHistoryToken);

        // When:
        service.onRequestSelectTableRow(new RequestSelectTableRowEvent(docInfo,
                selectingId));

        // Then:
        verify(mockHistoryToken).setView(MainView.Editor);
        verify(mockHistoryToken).setDocumentPath(
                docInfo.getPath() + docInfo.getName());
        verify(mockHistoryToken).clearEditorFilterAndSearch();
        verify(mockHistoryToken).setTextFlowId(selectingId.toString());
    }
}
