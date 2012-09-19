package org.zanata.webtrans.client.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Test(groups = { "unit-tests" })
@Slf4j
public class ModalNavigationStateHolderTest
{
   private ModalNavigationStateHolder navigationStateHolder;

   // @formatter:off
   private final List<TransUnit> tuList = Lists.newArrayList(
       TestFixture.makeTransUnit(0, ContentState.New),
       TestFixture.makeTransUnit(1, ContentState.New),
       TestFixture.makeTransUnit(2, ContentState.NeedReview),
       TestFixture.makeTransUnit(3, ContentState.Approved),
       TestFixture.makeTransUnit(4, ContentState.NeedReview),
       TestFixture.makeTransUnit(5, ContentState.New),
       TestFixture.makeTransUnit(6, ContentState.NeedReview),
       TestFixture.makeTransUnit(7, ContentState.Approved),
       TestFixture.makeTransUnit(8, ContentState.New),
       TestFixture.makeTransUnit(9, ContentState.New),
       TestFixture.makeTransUnit(10, ContentState.NeedReview)
   );
   private HashMap<Long,ContentState> transIdStateMap;
   private ArrayList<Long> idIndexList;
   // @formatter:on

   @BeforeClass
   protected void setUpTestData()
   {
      transIdStateMap = new HashMap<Long, ContentState>();
      idIndexList = new ArrayList<Long>();

      for (TransUnit tu : tuList)
      {
         transIdStateMap.put(tu.getId().getId(), tu.getStatus());
         idIndexList.add(tu.getId().getId());
      }
      ModalNavigationStateHolderTest.log.info("TransUnit list size: {}", tuList.size());
      ModalNavigationStateHolderTest.log.info("transIdStateMap : \n\t{}", transIdStateMap);
      ModalNavigationStateHolderTest.log.info("idIndexList : \n\t{}", idIndexList);
   }

   @BeforeMethod
   protected void setUp() throws Exception
   {
      navigationStateHolder = new ModalNavigationStateHolder();
      navigationStateHolder.init(transIdStateMap, idIndexList, 50);
   }

   @Test
   public void testGetInitialPageSize()
   {
      assertEquals(navigationStateHolder.getCurrentPage(), 0);
   }

   @Test
   public void testGetNextRowIndex()
   {
      navigationStateHolder.updateRowIndexInDocument(0);

      assertEquals(navigationStateHolder.getNextRowIndex(), 1);

      navigationStateHolder.updateRowIndexInDocument(3);
      assertEquals(navigationStateHolder.getNextRowIndex(), 4);

      navigationStateHolder.updateRowIndexInDocument(5);
      assertEquals(navigationStateHolder.getNextRowIndex(), 6);

   }

   @Test
   public void testGetPrevRowIndex()
   {
      navigationStateHolder.updateRowIndexInDocument(1);
      assertEquals(navigationStateHolder.getPrevRowIndex(), 0);

      navigationStateHolder.updateRowIndexInDocument(4);
      assertEquals(navigationStateHolder.getPrevRowIndex(), 3);
   }

   @Test
   public void testGetPreviousStateRowIndexNewAndFuzzy()
   {
      navigationStateHolder.updateRowIndexInDocument(9);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);

      navigationStateHolder.updateRowIndexInDocument(8);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 6);

      navigationStateHolder.updateRowIndexInDocument(4);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 2);

      navigationStateHolder.updateRowIndexInDocument(0);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 0);
   }

   @Test
   public void testGetPreviousStateRowIndexNew()
   {
      navigationStateHolder.updateRowIndexInDocument(9);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 8);

      navigationStateHolder.updateRowIndexInDocument(8);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 5);

      navigationStateHolder.updateRowIndexInDocument(0);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 0);

   }

   @Test
   public void testGetPreviousStateRowIndexFuzzy()
   {
      navigationStateHolder.updateRowIndexInDocument(9);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 6);

      navigationStateHolder.updateRowIndexInDocument(6);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);

      navigationStateHolder.updateRowIndexInDocument(3);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 2);
   }

   @Test
   public void testGetNextStateRowIndexNewAndFuzzy()
   {
      navigationStateHolder.updateRowIndexInDocument(2);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 4);

      navigationStateHolder.updateRowIndexInDocument(4);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 5);

      navigationStateHolder.updateRowIndexInDocument(7);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);
   }

   @Test
   public void testGetNextStateRowIndexNew()
   {
      navigationStateHolder.updateRowIndexInDocument(0);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 1);

      navigationStateHolder.updateRowIndexInDocument(5);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 8);

      navigationStateHolder.updateRowIndexInDocument(9);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 9);

   }

   @Test
   public void testGetNextStateRowIndexFuzzy()
   {
      navigationStateHolder.updateRowIndexInDocument(0);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 2);

      navigationStateHolder.updateRowIndexInDocument(3);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);

      navigationStateHolder.updateRowIndexInDocument(10);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 10);
   }

   @Test
   public void testUpdateMapAndNavigate()
   {
      navigationStateHolder.updateState(9L, ContentState.Approved);

      navigationStateHolder.updateRowIndexInDocument(10);
      assertEquals(navigationStateHolder.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);

      navigationStateHolder.updateState(3L, ContentState.NeedReview);

      navigationStateHolder.updateRowIndexInDocument(2);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 3);
   }

   @Test
   public void canGetTargetPage() {
      // given page size is 3 and we have 11 trans unit
      // 0 1 2 | 3 4 5 | 6 7 8 | 9 10
      navigationStateHolder.init(transIdStateMap, idIndexList, 3);

      assertThat(navigationStateHolder.getTargetPage(0), Matchers.equalTo(0));
      assertThat(navigationStateHolder.getTargetPage(2), Matchers.equalTo(0));
      assertThat(navigationStateHolder.getTargetPage(3), Matchers.equalTo(1));
      assertThat(navigationStateHolder.getTargetPage(7), Matchers.equalTo(2));
      assertThat(navigationStateHolder.getTargetPage(9), Matchers.equalTo(3));
      assertThat(navigationStateHolder.getTargetPage(10), Matchers.equalTo(3));
   }

   @Test
   public void canUpdatePageSize()
   {
      navigationStateHolder.init(transIdStateMap, idIndexList, 3);
      assertThat(navigationStateHolder.getPageCount(), Matchers.equalTo(4));
      navigationStateHolder.updateRowIndexInDocument(3);
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);

      navigationStateHolder.updatePageSize(4);
      assertThat(navigationStateHolder.getPageCount(), Matchers.equalTo(3));
      assertEquals(navigationStateHolder.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);
   }
}
