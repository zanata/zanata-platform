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
package org.zanata.webtrans.client.service;

import com.google.common.collect.ImmutableList;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import net.customware.gwt.presenter.client.EventBus;
import org.hibernate.transform.ResultTransformer;
import org.jglue.cdiunit.ContextController;
import org.jglue.cdiunit.ProducesAlternative;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.test.CdiUnitRunner;
import org.zanata.webtrans.shared.model.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.search.FilterConstraints;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.presenter.TransUnitsTablePresenter;
import org.zanata.webtrans.shared.ui.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.EditorFilter;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.zanata.test.EntityTestData.makeHTextFlow;
import static org.zanata.webtrans.test.GWTTestData.documentInfo;
import static org.zanata.webtrans.test.GWTTestData.extractFromEvents;
import static org.zanata.webtrans.test.GWTTestData.workspaceId;
// This test uses mockito to simulate an RPC call environment

@RunWith(CdiUnitRunner.class)
public class NavigationServiceIntegrationTest {

    private static final WorkspaceId WORKSPACE_ID = workspaceId();
    private static final HLocale LOCALE =
            new HLocale(WORKSPACE_ID.getLocaleId());
    // @formatter:off
    private static final List<HTextFlow> TEXT_FLOWS = ImmutableList.of(
            makeHTextFlow(0, LOCALE, ContentState.New),
            makeHTextFlow(1, LOCALE, ContentState.New),
            makeHTextFlow(2, LOCALE, ContentState.NeedReview),
            makeHTextFlow(3, LOCALE, ContentState.Approved),
            makeHTextFlow(4, LOCALE, ContentState.NeedReview),
            makeHTextFlow(5, LOCALE, ContentState.New));
    // @formatter:on
    private static final DocumentInfo DOCUMENT = documentInfo(1, "");
    @Inject
    private ContextController contextController;
    @Inject
    @Any
    private GetTransUnitListHandler handler;
    // used by GetTransUnitListHandler
    @Produces
    @Mock
    @ProducesAlternative
    private TextFlowDAO textFlowDAO;
    @Produces
    @Mock
    private LocaleService localeServiceImpl;
    @Produces
    @Mock
    @ProducesAlternative
    private ResourceUtils resourceUtils;
    @Produces
    @Mock
    @ProducesAlternative
    private ZanataIdentity identity;
    @Produces
    @Mock
    private TextFlowSearchService textFlowSearchServiceImpl;
    @Produces
    @Mock
    private org.zanata.service.ValidationService validationServiceImpl;
    private NavigationService service;
    @Mock
    private CachingDispatchAsync dispatcher;
    @Mock
    private EventBus eventBus;
    private ModalNavigationStateHolder navigationStateHolder;
    @Captor
    private ArgumentCaptor<GetTransUnitList> actionCaptor;
    @Captor
    private ArgumentCaptor<AsyncCallback<GetTransUnitListResult>>
            asyncCallbackCaptor;
    private GetTransUnitActionContext context;
    @Mock
    private TableEditorMessages messages;
    @Mock
    private TransUnitsTablePresenter transUnitsTablePresenter;
    @Mock
    private History history;
    @Captor
    private ArgumentCaptor<GwtEvent<EventHandler>> eventCaptor;
    private GetTransUnitListResult getTransUnitListResult;
    private SinglePageDataModelImpl pageModel;
    private UserConfigHolder configHolder;

    @Before
    public void setUp() throws Exception {
        // This works the same as annotating each method with @InRequestScope
        contextController.openRequest();
        pageModel = new SinglePageDataModelImpl();
        configHolder = new UserConfigHolder();
        navigationStateHolder = new ModalNavigationStateHolder(configHolder);
        GetTransUnitActionContextHolder contextHolder =
                new GetTransUnitActionContextHolder(configHolder);
        contextHolder.initContext(DOCUMENT, null, EditorFilter.ALL);
        service = new NavigationService(eventBus, dispatcher, configHolder,
                messages, pageModel, navigationStateHolder, contextHolder,
                history);
        service.addPageDataChangeListener(transUnitsTablePresenter);
        context = new GetTransUnitActionContext(DOCUMENT);
        doAnswer((Answer<Void>) invocation -> {
            Object[] arguments = invocation.getArguments();
            GetTransUnitList action = (GetTransUnitList) arguments[0];
            action.setWorkspaceId(WORKSPACE_ID);
            mockGetTransUnitLastHandlerBehaviour(DOCUMENT.getId(),
                    TEXT_FLOWS, LOCALE, action.getOffset(),
                    action.getCount());
            getTransUnitListResult = handler.execute(action, null);
            return null;
        }).when(dispatcher).execute(actionCaptor.capture(),
                asyncCallbackCaptor.capture());
    }

    public void mockGetTransUnitLastHandlerBehaviour(DocumentId documentId,
            List<HTextFlow> hTextFlows, HLocale hLocale, int startIndex,
            int count) {
        int maxSize = Math.min(startIndex + count, hTextFlows.size());
        when(textFlowDAO.getTextFlowsByDocumentId(documentId.getId(),
                startIndex, count))
                .thenReturn(hTextFlows.subList(startIndex, maxSize));
        when(localeServiceImpl.validateLocaleByProjectIteration(
                any(LocaleId.class), anyString(), anyString()))
                .thenReturn(hLocale);
        when(resourceUtils.getNumPlurals(any(HDocument.class),
                any(HLocale.class))).thenReturn(1);
        // trans unit navigation index handler
        when(textFlowDAO.getNavigationByDocumentId(eq(documentId), eq(hLocale),
                isA(ResultTransformer.class), isA(FilterConstraints.class)))
                .thenReturn(hTextFlows);
    }

    private void verifyDispatcherAndCallOnSuccess() {
        verify(dispatcher, atLeastOnce()).execute(actionCaptor.capture(),
                asyncCallbackCaptor.capture());
        asyncCallbackCaptor.getValue().onSuccess(getTransUnitListResult);
    }

    @Test
    public void canMockHandler() {
        service.requestTransUnitsAndUpdatePageIndex(context.withCount(6), true);
        assertThat(getTransUnitListResult.getDocumentId())
                .isEqualTo(DOCUMENT.getId());
        assertThat(getIntIds(getTransUnitListResult.getUnits()))
                .contains(0, 1, 2, 3, 4, 5);
        GetTransUnitsNavigationResult navigationResult =
                getTransUnitListResult.getNavigationIndex();
        assertThat(getLongIds(navigationResult.getIdIndexList()))
                .contains(0L, 1L, 2L, 3L, 4L, 5L);
        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(0L), ContentState.New);
        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(1L), ContentState.New);

        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(2L), ContentState.NeedReview);
        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(3L), ContentState.Approved);
        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(4L), ContentState.NeedReview);
        assertThat(navigationResult.getTransIdStateList())
                .containsEntry(new TransUnitId(5L), ContentState.New);
    }

    @Test
    public void canGoToFirstPage() {
        service.init(context.withCount(3));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(0);
        assertThat(getPageDataModelAsIds()).contains(0, 1, 2);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
        // go again won't cause another call to server
        service.gotoPage(0);
        verifyNoMoreInteractions(dispatcher);
        assertThat(getPageDataModelAsIds()).contains(0, 1, 2);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
    }

    private List<Integer> getPageDataModelAsIds() {
        return getIntIds(pageModel.getData());
    }

    @Test
    public void canGoToLastPageWithNotPerfectDivide() {
        service.init(context.withCount(4));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        service.gotoPage(1);
        verifyNoMoreInteractions(dispatcher);
    }

    @Test
    public void canGoToLastPageWithPerfectDivide() {
        service.init(context.withCount(3));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        service.gotoPage(1);
        verifyNoMoreInteractions(dispatcher);
        assertThat(getPageDataModelAsIds()).contains(3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
    }

    @Test
    public void canHavePageCountGreaterThanActualSize() {
        service.init(context.withCount(10));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(100);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(0, 1, 2, 3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
        service.gotoPage(0);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(0, 1, 2, 3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
    }

    @Test
    public void canGoToNextPage() {
        service.init(context.withCount(2));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(2, 3);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        service.gotoPage(2);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(2);
        // can't go any further
        service.gotoPage(2);
        verifyNoMoreInteractions(dispatcher);
        assertThat(getPageDataModelAsIds()).contains(4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(2);
    }

    @Test
    public void canGoToPreviousPage() {
        service.init(context.withCount(2));
        verifyDispatcherAndCallOnSuccess();
        // should be on first page already
        service.gotoPage(0);
        verifyNoMoreInteractions(dispatcher);
        assertThat(getPageDataModelAsIds()).contains(0, 1);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
        service.gotoPage(2);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(2);
        service.gotoPage(1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(2, 3);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        service.gotoPage(0);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(0, 1);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
        // can't go any further
        service.gotoPage(0);
        verifyNoMoreInteractions(dispatcher);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
    }

    @Test
    public void canGoToPage() {
        service.init(context.withCount(3));
        verifyDispatcherAndCallOnSuccess();
        service.gotoPage(1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        // page out of bound
        service.gotoPage(7);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(3, 4, 5);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(1);
        // page is negative
        service.gotoPage(-1);
        verifyDispatcherAndCallOnSuccess();
        assertThat(getPageDataModelAsIds()).contains(0, 1, 2);
        assertThat(navigationStateHolder.getCurrentPage()).isEqualTo(0);
    }

    @Test
    public void onRPCSuccess() {
        service.init(context.withCount(3));
        InOrder inOrder = inOrder(eventBus, transUnitsTablePresenter);
        inOrder.verify(eventBus).fireEvent(LoadingEvent.START_EVENT);
        verifyDispatcherAndCallOnSuccess();
        // behaviour on GetTransUnitList success
        inOrder.verify(transUnitsTablePresenter)
                .showDataForCurrentPage(service.getCurrentPageValues());
        verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
        TableRowSelectedEvent tableRowSelectedEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        TableRowSelectedEvent.class);
        TransUnitId firstItem = new TransUnitId(TEXT_FLOWS.get(0).getId());
        assertThat(tableRowSelectedEvent.getSelectedId()).isEqualTo(firstItem);
        // behaviour on GetTransUnitNavigation success
        PageCountChangeEvent pageCountChangeEvent =
                extractFromEvents(eventCaptor.getAllValues(),
                        PageCountChangeEvent.class);
        assertThat(pageCountChangeEvent.getPageCount()).isEqualTo(2);
        inOrder.verify(eventBus).fireEvent(LoadingEvent.FINISH_EVENT);
    }

    private static List<Integer> getIntIds(List<TransUnit> transUnits) {
        return transUnits.stream().map(
                it -> (int) it.getId().getId()).collect(toList());
    }

    private static List<Long> getLongIds(List<TransUnitId> transUnitIds) {
        return transUnitIds.stream().map(TransUnitId::getId).collect(toList());
    }

}
