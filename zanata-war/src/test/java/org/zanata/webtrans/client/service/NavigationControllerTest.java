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

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.dao.TextFlowDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import org.zanata.model.TestFixture;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.LocaleService;
import org.zanata.service.TextFlowSearchService;
import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.presenter.TransUnitEditPresenter;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
import org.zanata.webtrans.server.rpc.GetTransUnitsNavigationHandler;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.AbstractWorkspaceAction;
import org.zanata.webtrans.shared.rpc.GetTransUnitList;
import org.zanata.webtrans.shared.rpc.GetTransUnitListResult;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigation;
import org.zanata.webtrans.shared.rpc.GetTransUnitsNavigationResult;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;

import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.shared.Action;
import net.customware.gwt.dispatch.shared.ActionException;
import net.customware.gwt.dispatch.shared.Result;
import net.customware.gwt.presenter.client.EventBus;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Test
public class NavigationControllerTest
{
   private static final Logger log = LoggerFactory.getLogger(NavigationControllerTest.class);
   private HLocale hLocale = new HLocale(new LocaleId("en"));
   private WorkspaceId workspaceId = new WorkspaceId(new ProjectIterationId("project", "master"), hLocale.getLocaleId());

   // @formatter:off
   private final List<HTextFlow> hTextFlows = Lists.newArrayList(
         TestFixture.makeHTextFlow(0, hLocale, ContentState.New),
         TestFixture.makeHTextFlow(1, hLocale, ContentState.New),
         TestFixture.makeHTextFlow(2, hLocale, ContentState.NeedReview),
         TestFixture.makeHTextFlow(3, hLocale, ContentState.Approved),
         TestFixture.makeHTextFlow(4, hLocale, ContentState.NeedReview),
         TestFixture.makeHTextFlow(5, hLocale, ContentState.New)
   );
   // @formatter:on
   private static final boolean NOT_FORCE_RELOAD = false;
   private static final boolean FORCE_RELOAD = true;

   private NavigationController controller;
   @Mock
   private CachingDispatchAsync dispatcher;
   @Mock
   private EventBus eventBus;
   private TransUnitNavigationService navigationService;
   private DocumentId documentId = new DocumentId(1L);

   @Captor
   private ArgumentCaptor<AbstractWorkspaceAction> actionCaptor;

   @Captor
   private ArgumentCaptor<AsyncCallback> asyncCallbackCaptor;

   //captured dispatcher arguments
   private GetTransUnitList getTransUnitList;
   private GetTransUnitsNavigation getTransUnitsNavigation;
   private AsyncCallback<GetTransUnitListResult> getTransUnitListCallback;
   private AsyncCallback<GetTransUnitsNavigationResult> getTransUnitsNavigationCallback;
   
   
   //used by GetTransUnitListHandler and GetTransUnitsNavigationHandler
   @Mock
   private TextFlowDAO textFlowDAO;
   @Mock 
   private LocaleService localeServiceImpl;
   @Mock
   private ResourceUtils resourceUtils;
   @Mock
   private ZanataIdentity identity;
   @Mock
   private TextFlowSearchService textFlowSearchServiceImpl;

   private GetTransUnitListHandler getTransUnitListHandler;
   private GetTransUnitsNavigationHandler getTransUnitsNavigationHandler;

   private GetTransUnitActionContext context;
   @Mock private TableEditorMessages messages;
   @Mock private TransUnitEditPresenter transUnitEditPresenter;

   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      setupGetTransUnitListHandler();
      setupGetTransUnitNavigationHandler();
      navigationService = new TransUnitNavigationService();
      UserConfigHolder configHolder = new UserConfigHolder();

      SinglePageDataModelImpl pageModel = new SinglePageDataModelImpl(eventBus, navigationService);
      pageModel.addDataChangeListener(transUnitEditPresenter);
      controller = new NavigationController(eventBus, dispatcher, navigationService, configHolder, messages, pageModel);

      context = GetTransUnitActionContext.of(documentId);
   }

   @SuppressWarnings("unchecked")
   private void verifyDispatcherAndCaptureArguments()
   {
      verify(dispatcher, atLeastOnce()).execute(actionCaptor.capture(), asyncCallbackCaptor.capture());

      List<AbstractWorkspaceAction> actions = actionCaptor.getAllValues();
      List<AsyncCallback> callbacks = asyncCallbackCaptor.getAllValues();

      for (int i = 0, allValuesSize = actions.size(); i < allValuesSize; i++)
      {
         if (actions.get(i) instanceof GetTransUnitList)
         {
            getTransUnitList = (GetTransUnitList) actions.get(i);
            getTransUnitList.setWorkspaceId(workspaceId);
            getTransUnitListCallback = (AsyncCallback<GetTransUnitListResult>) callbacks.get(i);
         }
         else
         {
            getTransUnitsNavigation = (GetTransUnitsNavigation) actions.get(i);
            getTransUnitsNavigation.setWorkspaceId(workspaceId);
            getTransUnitsNavigationCallback = (AsyncCallback<GetTransUnitsNavigationResult>) callbacks.get(i);
         }
      }
   }

   private void setupGetTransUnitListHandler()
   {
      // @formatter:off
      getTransUnitListHandler = SeamAutowire.instance()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("textFlowSearchServiceImpl", textFlowSearchServiceImpl)
            .use("localeServiceImpl", localeServiceImpl)
            .use("resourceUtils", resourceUtils)
            .autowire(GetTransUnitListHandler.class);
      // @formatter:on
      when(textFlowDAO.getTextFlows(documentId.getId())).thenReturn(hTextFlows);
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);
      when(resourceUtils.getNumPlurals(any(HDocument.class), any(HLocale.class))).thenReturn(1);
   }

   private void setupGetTransUnitNavigationHandler()
   {
      // @formatter:off
      getTransUnitsNavigationHandler = SeamAutowire.instance()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .autowire(GetTransUnitsNavigationHandler.class);
      // @formatter:on
      when(textFlowDAO.getNavigationByDocumentId(documentId.getId())).thenReturn(hTextFlows);
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);
   }

   //look at this AWESOME generic work ;)
   private static <A extends Action<R>, R extends Result, H extends ActionHandler<A, R>> R callHandler(H handler, A action)
   {
      R result;
      try
      {
         result = handler.execute(action, null);
      }
      catch (ActionException e)
      {
         throw new RuntimeException("fail to call getTransUnitListHandler.execute()", e);
      }

      log.info("result: {}", result);
      return result;
   }

   private void simulateRPCCallback()
   {
      verifyDispatcherAndCaptureArguments();
      getTransUnitListCallback.onSuccess(callHandler(getTransUnitListHandler, getTransUnitList));
      getTransUnitsNavigationCallback.onSuccess(callHandler(getTransUnitsNavigationHandler, getTransUnitsNavigation));
   }

   @Test
   public void canMockHandler()
   {
      controller.init(context.setCount(6));
      verifyDispatcherAndCaptureArguments();

      GetTransUnitListResult getTransUnitListResult = callHandler(getTransUnitListHandler, getTransUnitList);
      assertThat(getTransUnitListResult.getDocumentId(), equalTo(documentId));
      assertThat(asIds(getTransUnitListResult.getUnits()), contains(0, 1, 2, 3, 4, 5));

      GetTransUnitsNavigationResult navigationResult = callHandler(getTransUnitsNavigationHandler, getTransUnitsNavigation);
      assertThat(navigationResult.getDocumentId(), equalTo(documentId));
      assertThat(navigationResult.getIdIndexList(), contains(0L, 1L, 2L, 3L, 4L, 5L));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(0L, ContentState.New));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(1L, ContentState.New));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(2L, ContentState.NeedReview));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(3L, ContentState.Approved));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(4L, ContentState.NeedReview));
      assertThat(navigationResult.getTransIdStateList(), hasEntry(5L, ContentState.New));
   }

   @Test
   public void canGoToFirstPage()
   {
      controller.init(context.setCount(3));
      simulateRPCCallback();
      controller.gotoFirstPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));

      //go again won't cause another call to server
      controller.gotoFirstPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   private List<Integer> getPageDataModelAsIds()
   {
      SinglePageDataModelImpl dataModel = (SinglePageDataModelImpl) controller.getDataModel();
      return asIds(dataModel.getData());
   }

   @Test
   public void canGoToLastPageWithNotPerfectDivide()
   {
      controller.init(context.setCount(4));
      simulateRPCCallback();
      controller.gotoLastPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoLastPage();
      verifyNoMoreInteractions(dispatcher);
   }

   @Test
   public void canGoToLastPageWithPerfectDivide() {
      controller.init(context.setCount(3));
      simulateRPCCallback();
      controller.gotoLastPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoLastPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));
   }

   @Test
   public void canHavePageCountGreaterThanActualSize() {
      controller.init(context.setCount(10));
      simulateRPCCallback();
      controller.gotoLastPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2, 3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(0));

      controller.gotoFirstPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2, 3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToNextPage()
   {
      controller.init(context.setCount(2));
      simulateRPCCallback();
      controller.gotoNextPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(2, 3));
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoNextPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));

      //can't go any further
      controller.gotoNextPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));
   }

   @Test
   public void canGoToPreviousPage()
   {
      controller.init(context.setCount(2));
      simulateRPCCallback();
      //should be on first page already
      controller.gotoPreviousPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(getPageDataModelAsIds(), contains(0, 1));
      assertThat(navigationService.getCurrentPage(), is(0));

      controller.gotoLastPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));

      controller.gotoPreviousPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(2, 3));
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoPreviousPage();
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(0, 1));
      assertThat(navigationService.getCurrentPage(), is(0));

      //can't go any further
      controller.gotoPreviousPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToPage()
   {
      controller.init(context.setCount(3));
      simulateRPCCallback();
      controller.gotoPage(1, NOT_FORCE_RELOAD);
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      //page out of bound
      controller.gotoPage(7, NOT_FORCE_RELOAD);
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      //page is negative
      controller.gotoPage(-1, NOT_FORCE_RELOAD);
      simulateRPCCallback();
      assertThat(getPageDataModelAsIds(), contains(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canForceReload()
   {
      controller.init(context.setCount(3));
      simulateRPCCallback();
      controller.gotoPage(1, NOT_FORCE_RELOAD);
      simulateRPCCallback();
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoPage(1, NOT_FORCE_RELOAD);
      verifyNoMoreInteractions(dispatcher);
      assertThat(navigationService.getCurrentPage(), is(1));

      controller.gotoPage(1, FORCE_RELOAD);
      simulateRPCCallback();
      assertThat(navigationService.getCurrentPage(), is(1));
   }

   private static List<Integer> asIds(List<TransUnit> transUnits)
   {
      return Lists.newArrayList(Collections2.transform(transUnits, new Function<TransUnit, Integer>()
      {
         @Override
         public Integer apply(TransUnit from)
         {
            return (int) from.getId().getId();
         }
      }));
   }
}
