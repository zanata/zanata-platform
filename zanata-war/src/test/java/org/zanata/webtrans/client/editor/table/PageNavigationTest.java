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
package org.zanata.webtrans.client.editor.table;

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
import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.TransUnitNavigationService;
import org.zanata.webtrans.server.rpc.GetTransUnitListHandler;
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

import net.customware.gwt.dispatch.shared.ActionException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@Test
public class PageNavigationTest
{
   private static final Logger log = LoggerFactory.getLogger(PageNavigationTest.class);
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
   public static final boolean NOT_FORCE_RELOAD = false;
   private PageNavigation page;
   @Mock
   private CachingDispatchAsync dispatcher;
   private TransUnitNavigationService navigationService;
   private DocumentId documentId = new DocumentId(1L);

   @Captor
   private ArgumentCaptor<AbstractWorkspaceAction> actionCaptor;

   @Captor
   private ArgumentCaptor<AbstractAsyncCallback> asyncCallbackCaptor;

   //captured dispatcher arguments
   private GetTransUnitList getTransUnitList;
   private GetTransUnitsNavigation getTransUnitsNavigation;
   private AbstractAsyncCallback<GetTransUnitListResult> getTransUnitListCallback;
   private AbstractAsyncCallback<GetTransUnitsNavigationResult> getTransUnitsNavigationCallback;
   
   
   //used by GetTransUnitListHandler
   @Mock
   private TextFlowDAO textFlowDAO;
   @Mock 
   private LocaleService localeServiceImpl;
   @Mock
   private ResourceUtils resourceUtils;
   @Mock
   private ZanataIdentity identity;
   private GetTransUnitListHandler getTransUnitListHandler;
   private GetTransUnitActionContext context;


   @BeforeMethod
   public void setUp() throws Exception
   {
      MockitoAnnotations.initMocks(this);
      mockGetTransUnitListHandler();
      navigationService = new TransUnitNavigationService();
      page = new PageNavigation(dispatcher, navigationService);

      context = GetTransUnitActionContext.of(documentId);
   }

   @SuppressWarnings("unchecked")
   private void verifyDispatcherAndCaptureArguments()
   {
      verify(dispatcher, atLeastOnce()).execute(actionCaptor.capture(), asyncCallbackCaptor.capture());

      List<AbstractWorkspaceAction> actions = actionCaptor.getAllValues();
      List<AbstractAsyncCallback> callbacks = asyncCallbackCaptor.getAllValues();

      for (int i = 0, allValuesSize = actions.size(); i < allValuesSize; i++)
      {
         if (actions.get(i) instanceof GetTransUnitList)
         {
            getTransUnitList = (GetTransUnitList) actions.get(i);
            getTransUnitList.setWorkspaceId(workspaceId);
            getTransUnitListCallback = (AbstractAsyncCallback<GetTransUnitListResult>) callbacks.get(i);
         }
         else
         {
            getTransUnitsNavigation = (GetTransUnitsNavigation) actions.get(i);
            getTransUnitsNavigation.setWorkspaceId(workspaceId);
            getTransUnitsNavigationCallback = (AbstractAsyncCallback<GetTransUnitsNavigationResult>) callbacks.get(i);
         }
      }
   }

   private void mockGetTransUnitListHandler()
   {
      // @formatter:off
      getTransUnitListHandler = SeamAutowire.instance()
            .use("identity", identity)
            .use("textFlowDAO", textFlowDAO)
            .use("localeServiceImpl", localeServiceImpl)
            .use("resourceUtils", resourceUtils)
            .autowire(GetTransUnitListHandler.class);
      // @formatter:on
      when(textFlowDAO.getTransUnitList(documentId.getId())).thenReturn(hTextFlows);
      when(localeServiceImpl.validateLocaleByProjectIteration(any(LocaleId.class), anyString(), anyString())).thenReturn(hLocale);
      when(resourceUtils.getNumPlurals(any(HDocument.class), any(HLocale.class))).thenReturn(1);
   }

   private GetTransUnitListResult callGetTransUnitListHandler()
   {
      GetTransUnitListResult result;
      try
      {
         result = getTransUnitListHandler.execute(getTransUnitList, null);
      }
      catch (ActionException e)
      {
         throw new RuntimeException("fail to call getTransUnitListHandler.execute()", e);
      }

      log.info("result: {}", result);
      return result;
   }

   private void simulateGetTransUnitListCallback()
   {
      verifyDispatcherAndCaptureArguments();
      getTransUnitListCallback.onSuccess(callGetTransUnitListHandler());
   }

   @Test
   public void canMockHandler()
   {
      page.init(context.setCount(6));
      verifyDispatcherAndCaptureArguments();
      GetTransUnitListResult result = callGetTransUnitListHandler();
      assertThat(result.getDocumentId(), equalTo(documentId));
      assertThat(result.getTotalCount(), equalTo(hTextFlows.size()));
      assertThat(asIds(result.getUnits()), hasItems(0, 1, 2, 3, 4, 5));
   }

   @Test
   public void canGoToFirstPage()
   {
      page.init(context.setCount(3));
      simulateGetTransUnitListCallback();
      page.gotoFirstPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));

      //go again won't cause another call to server
      page.gotoFirstPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToLastPageWithNotPerfectDivide()
   {
      page.init(context.setCount(4));
      simulateGetTransUnitListCallback();
      page.gotoLastPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      page.gotoLastPage();
      verifyNoMoreInteractions(dispatcher);
   }

   @Test
   public void canGoToLastPageWithPerfectDivide() {
      page.init(context.setCount(3));
      simulateGetTransUnitListCallback();
      page.gotoLastPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      page.gotoLastPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));
   }

   @Test
   public void canHavePageCountGreaterThanActualSize() {
      page.init(context.setCount(10));
      simulateGetTransUnitListCallback();
      page.gotoLastPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(6));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(0));

      page.gotoFirstPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(6));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToNextPage()
   {
      page.init(context.setCount(2));
      simulateGetTransUnitListCallback();
      page.gotoNextPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(2, 3));
      assertThat(navigationService.getCurrentPage(), is(1));

      page.gotoNextPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));

      //can't go any further
      page.gotoNextPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));
   }

   @Test
   public void canGoToPreviousPage()
   {
      page.init(context.setCount(2));
      simulateGetTransUnitListCallback();
      //should be on first page already
      page.gotoPreviousPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1));
      assertThat(navigationService.getCurrentPage(), is(0));

      page.gotoLastPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(4, 5));
      assertThat(navigationService.getCurrentPage(), is(2));

      page.gotoPreviousPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(2, 3));
      assertThat(navigationService.getCurrentPage(), is(1));

      page.gotoPreviousPage();
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(2));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1));
      assertThat(navigationService.getCurrentPage(), is(0));

      //can't go any further
      page.gotoPreviousPage();
      verifyNoMoreInteractions(dispatcher);
      assertThat(navigationService.getCurrentPage(), is(0));
   }

   @Test
   public void canGoToPage()
   {
      page.init(context.setCount(3));
      simulateGetTransUnitListCallback();
      page.gotoPage(1, NOT_FORCE_RELOAD);
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      //page out of bound
      page.gotoPage(7, NOT_FORCE_RELOAD);
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(3, 4, 5));
      assertThat(navigationService.getCurrentPage(), is(1));

      page.gotoPage(0, NOT_FORCE_RELOAD);
      simulateGetTransUnitListCallback();
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));

      //page is negative
      page.gotoPage(-1, NOT_FORCE_RELOAD);
      verifyNoMoreInteractions(dispatcher);
      assertThat(page.getCurrentPageItems().size(), is(3));
      assertThat(asIds(page.getCurrentPageItems()), hasItems(0, 1, 2));
      assertThat(navigationService.getCurrentPage(), is(0));
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
