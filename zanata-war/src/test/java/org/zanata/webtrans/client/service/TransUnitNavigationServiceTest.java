package org.zanata.webtrans.client.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.model.TestFixture;
import org.zanata.webtrans.client.editor.table.TableConstants;
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
   // @formatter:on

   @BeforeMethod
   protected void setUp() throws Exception
   {
      navigationService = new TransUnitNavigationService();
      HashMap<Long, ContentState> transIdStateList = new HashMap<Long, ContentState>();
      ArrayList<Long> idIndexList = new ArrayList<Long>();

      for (TransUnit tu : tuList)
      {
         transIdStateList.put(tu.getId().getId(), tu.getStatus());
         idIndexList.add(tu.getId().getId());
      }
      navigationService.init(transIdStateList, idIndexList, TableConstants.PAGE_SIZE);
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
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 8);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 6);

      navigationService.updateCurrentPageAndRowIndex(0, 4);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 2);
   }

   @Test
   public void testGetPreviousStateRowIndexNew()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 8);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 5);

      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 0);

   }

   @Test
   public void testGetPreviousStateRowIndexFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 6);

      navigationService.updateCurrentPageAndRowIndex(0, 6);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 3);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 2);
   }

   @Test
   public void testGetNextStateRowIndexNewAndFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 2);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 4);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 5);

      navigationService.updateCurrentPageAndRowIndex(0, 7);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 8);
   }

   @Test
   public void testGetNextStateRowIndexNew()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 1);

      navigationService.updateCurrentPageAndRowIndex(0, 5);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 8);

      navigationService.updateCurrentPageAndRowIndex(0, 9);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.NEW_PREDICATE), 9);

   }

   @Test
   public void testGetNextStateRowIndexFuzzy()
   {
      navigationService.updateCurrentPageAndRowIndex(0, 0);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 2);

      navigationService.updateCurrentPageAndRowIndex(0, 3);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 4);

      navigationService.updateCurrentPageAndRowIndex(0, 10);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_PREDICATE), 10);
   }

   @Test
   public void testUpdateMapAndNavigate()
   {
      navigationService.updateState(new Long(9), ContentState.Approved);

      navigationService.updateCurrentPageAndRowIndex(0, 10);
      assertEquals(navigationService.getPreviousStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 8);

      navigationService.updateState(new Long(3), ContentState.NeedReview);

      navigationService.updateCurrentPageAndRowIndex(0, 2);
      assertEquals(navigationService.getNextStateRowIndex(TransUnitNavigationService.FUZZY_OR_NEW_PREDICATE), 3);
   }

}
