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

import java.util.List;

import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.presenter.TransUnitsTablePresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.google.common.collect.ImmutableList;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;

import lombok.extern.slf4j.Slf4j;
import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@Test(groups = { "unit-tests" }, description = "This test uses SeamAutowire with mockito to simulate a RPC call environment")
@Slf4j
public class NavigationServiceIntegrationTest
{
   private static final WorkspaceId WORKSPACE_ID = TestFixture.workspaceId();
   private static final HLocale LOCALE = new HLocale(WORKSPACE_ID.getLocaleId());
   // @formatter:off
   private static final List<HTextFlow> TEXT_FLOWS = ImmutableList.<HTextFlow>builder().add(
         TestFixture.makeHTextFlow(0, LOCALE, ContentState.New),
         TestFixture.makeHTextFlow(1, LOCALE, ContentState.New),
         TestFixture.makeHTextFlow(2, LOCALE, ContentState.NeedReview),
         TestFixture.makeHTextFlow(3, LOCALE, ContentState.Approved),
         TestFixture.makeHTextFlow(4, LOCALE, ContentState.NeedReview),
         TestFixture.makeHTextFlow(5, LOCALE, ContentState.New)
   ).build();
   // @formatter:on
   private static final DocumentId DOCUMENT_ID = new DocumentId(1L);

   private NavigationService service;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private EventBus eventBus;
   private ModalNavigationStateHolder navigationStateHolder;
   @Captor
   private ArgumentCaptor<GetTransUnitList> actionCaptor;
   @Captor
   private ArgumentCaptor<AsyncCallback<GetTransUnitListResult>> asyncCallbackCaptor;
   private GetTransUnitActionContext context;
   @Mock
   private TableEditorMessages messages;
   @Mock
   private TransUnitsTablePresenter transUnitsTablePresenter;
   @Mock
   private TargetContentsPresenter targetContentsPresenter;
   @Captor
   private ArgumentCaptor<GwtEvent> eventCaptor;
   private GetTransUnitListResult getTransUnitListResult;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);

      service = new NavigationService(eventBus, dispatcher, new UserConfigHolder(), messages);
      service.addPageDataChangeListener(transUnitsTablePresenter);
      navigationStateHolder = service.getNavigationStateHolder();

      context = new GetTransUnitActionContext(DOCUMENT_ID);

      doAnswer(new Answer<Void>()
      {
         @Override
         public Void answer(InvocationOnMock invocation) throws Throwable
         {
            Object[] arguments = invocation.getArguments();
            GetTransUnitList action = (GetTransUnitList) arguments[0];
            action.setWorkspaceId(WORKSPACE_ID);
            GetTransUnitListHandler handler = new MockHandlerFactory().createGetTransUnitListHandlerWithBehavior(DOCUMENT_ID, TEXT_FLOWS, LOCALE, action.getOffset(), action.getCount());
            getTransUnitListResult = handler.execute(action, null);
            return null;
         }
      }).when(dispatcher).execute(actionCaptor.capture(), asyncCallbackCaptor.capture());
   }

   private void verifyDispatcherAndCallOnSuccess()
   {
      verify(dispatcher, atLeastOnce()).execute(actionCaptor.capture(), asyncCallbackCaptor.capture());
      asyncCallbackCaptor.getValue().onSuccess(getTransUnitListResult);
   }

   @Test
   public void canMockHandler()
   {
      service.requestTransUnitsAndUpdatePageIndex(context.changeCount(6), true);

      assertThat(getTransUnitListResult.getDocumentId(), equalTo(DOCUMENT_ID));
      assertThat(TestFixture.asIds(getTransUnitListResult.getUnits()), contains(0, 1, 2, 3, 4, 5));

      GetTransUnitsNavigationResult navigationResult = getTransUnitListResult.getNavigationIndex();
      assertThat(TestFixture.asLongs(navigationResult.getIdIndexList()), contains(0L, 1L, 2L, 3L, 4L, 5L));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(0L), ContentState.New));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(1L), ContentState.New));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(2L), ContentState.NeedReview));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(3L), ContentState.Approved));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(4L), ContentState.NeedReview));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(new TransUnitId(5L), ContentState.New));
   }

   @Test
   public void canGoToFirstPage()
   {
      service.init(context.changeCount(3));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(0);

      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));

      //go again won't cause another call to server
      service.gotoPage(0);
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));
   }

   private List<Integer> getPageDataModelAsIds()
   {
      return TestFixture.asIds(service.getCurrentPageValues());
   }

   @Test
   public void canGoToLastPageWithNotPerfectDivide()
   {
      service.init(context.changeCount(4));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      service.gotoPage(1);
      verifyNoMoreInteractions(dispatcher);
   }

   @Test
   public void canGoToLastPageWithPerfectDivide() {
      service.init(context.changeCount(3));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      service.gotoPage(1);
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));
   }

   @Test
   public void canHavePageCountGreaterThanActualSize() {
      service.init(context.changeCount(10));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(100);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(0, 1, 2, 3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));

      service.gotoPage(0);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(0, 1, 2, 3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToNextPage()
   {
      service.init(context.changeCount(2));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(2, 3));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      service.gotoPage(2);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(2));

      //can't go any further
      service.gotoPage(2);
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(2));
   }

   @Test
   public void canGoToPreviousPage()
   {
      service.init(context.changeCount(2));
      verifyDispatcherAndCallOnSuccess();

      //should be on first page already
      service.gotoPage(0);
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(0, 1));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));

      service.gotoPage(2);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(2));

      service.gotoPage(1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(2, 3));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      service.gotoPage(0);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(0, 1));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));

      //can't go any further
      service.gotoPage(0);
      verifyNoMoreInteractions(dispatcher);
      assertThat(navigationStateHolder.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToPage()
   {
      service.init(context.changeCount(3));
      verifyDispatcherAndCallOnSuccess();

      service.gotoPage(1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      //page out of bound
      service.gotoPage(7);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationStateHolder.getCurrentPage(), is(1));

      //page is negative
      service.gotoPage(-1);
      verifyDispatcherAndCallOnSuccess();

      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationStateHolder.getCurrentPage(), is(0));
   }

   @Test
   public void onRPCSuccess()
   {
      service.init(context.changeCount(3));
      InOrder inOrder = inOrder(eventBus, transUnitsTablePresenter);
      inOrder.verify(eventBus).fireEvent(LoadingEvent.START_EVENT);
      verifyDispatcherAndCallOnSuccess();

      // behaviour on GetTransUnitList success
      inOrder.verify(transUnitsTablePresenter).showDataForCurrentPage(service.getCurrentPageValues());

      verify(eventBus, atLeastOnce()).fireEvent(eventCaptor.capture());
      TableRowSelectedEvent tableRowSelectedEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), TableRowSelectedEvent.class);
      TransUnitId firstItem = new TransUnitId(TEXT_FLOWS.get(0).getId());
      assertThat(tableRowSelectedEvent.getSelectedId(), Matchers.equalTo(firstItem));

      // behaviour on GetTransUnitNavigation success
      PageCountChangeEvent pageCountChangeEvent = TestFixture.extractFromEvents(eventCaptor.getAllValues(), PageCountChangeEvent.class);
      assertThat(pageCountChangeEvent.getPageCount(), Matchers.is(2));
      inOrder.verify(eventBus).fireEvent(LoadingEvent.FINISH_EVENT);
   }
}
