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
import org.zanata.webtrans.client.editor.table.TableConstants;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.collect.Lists;

@Test(groups = { "unit-tests" })
public class TransUnitNavigationServiceTest
{
   private TransUnitNavigationService navigationService;

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
   }

   @BeforeMethod
   protected void setUp() throws Exception
   {
      navigationService = new TransUnitNavigationService();
      navigationService.init(transIdStateMap, idIndexList, TableConstants.PAGE_SIZE);
   }

   @Test
   public void testGetInitialPageSize()
   {
      assertEquals(navigationService.getCurrentPage(), 0);
   }

   @Test
   public void testGetInitialRowIndex()
   {
      assertEquals(navigationService.getCurrentRowIndex(), 0);
   }

   @Test
   public void testGetNextRowIndex()
   {
      assertEquals(navigationService.getNextRowIndex(), 1);

      navigationService.updateCurrentPageAndRowIndex(0, 3);
      assertEquals(navigationService.getCurrentRowIndex(), 3);
      assertEquals(navigationService.getNextRowIndex(), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 5);
      assertEquals(navigationService.getCurrentRowIndex(), 5);
      assertEquals(navigationService.getNextRowIndex(), 6);

   }

   @Test
   public void testGetPrevRowIndex()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 1);
      assertEquals(navigationService.getCurrentRowIndex(), 1);
      assertEquals(navigationService.getPrevRowIndex(), 0);

      navigationService.updateCurrentPageAndRowIndex(0, 4);
      assertEquals(navigationService.getCurrentRowIndex(), 4);
      assertEquals(navigationService.getPrevRowIndex(), 3);
   }

   @Test
   public void testGetRowIndex()
   {
      assertEquals(navigationService.getRowIndex(tuList.get(0), false, tuList), new Integer(0));

      assertEquals(navigationService.getRowIndex(tuList.get(1), true, tuList), new Integer(1));
      assertEquals(navigationService.getRowIndex(tuList.get(2), false, tuList), new Integer(2));

   }

   @Test
   public void testGetPreviousStateRowIndexNewAndFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 8);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 6);

      navigationService.updateCurrentPageAndRowIndex(0, 4);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 2);
   }

   @Test
   public void testGetPreviousStateRowIndexNew()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 8);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 5);

      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.NEW_PREDICATE), 0);

   }

   @Test
   public void testGetPreviousStateRowIndexFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 6);

      navigationService.updateCurrentPageAndRowIndex(0, 6);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 3);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 2);
   }

   @Test
   public void testGetNextStateRowIndexNewAndFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 2);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 4);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 5);

      navigationService.updateCurrentPageAndRowIndex(0, 7);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);
   }

   @Test
   public void testGetNextStateRowIndexNew()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 1);

      navigationService.updateCurrentPageAndRowIndex(0, 5);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.NEW_PREDICATE), 9);

   }

   @Test
   public void testGetNextStateRowIndexFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 2);

      navigationService.updateCurrentPageAndRowIndex(0, 3);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 10);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_PREDICATE), 10);
   }

   @Test
   public void testUpdateMapAndNavigate()
   {
      navigationService.updateState(9L, ContentState.Approved);

      navigationService.updateCurrentPageAndRowIndex(0, 10);
      assertEquals(navigationService.getPreviousStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 8);

      navigationService.updateState(3L, ContentState.NeedReview);

      navigationService.updateCurrentPageAndRowIndex(0, 2);
      assertEquals(navigationService.getNextStateRowIndex(UserConfigHolder.FUZZY_OR_NEW_PREDICATE), 3);
   }

   @Test
   public void canGetTargetPage() {
      // given page size is 3 and we have 11 trans unit
      // 0 1 2 | 3 4 5 | 6 7 8 | 9 10
      navigationService.init(transIdStateMap, idIndexList, 3);

      assertThat(navigationService.getTargetPage(0), Matchers.equalTo(0));
      assertThat(navigationService.getTargetPage(2), Matchers.equalTo(0));
      assertThat(navigationService.getTargetPage(3), Matchers.equalTo(1));
      assertThat(navigationService.getTargetPage(7), Matchers.equalTo(2));
      assertThat(navigationService.getTargetPage(9), Matchers.equalTo(3));
      assertThat(navigationService.getTargetPage(10), Matchers.equalTo(3));
   }
}
