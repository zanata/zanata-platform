package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.zanata.webtrans.client.presenter.TransHistoryVersionComparator.COMPARATOR;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.webtrans.shared.model.TransHistoryItem;

import com.google.common.collect.Lists;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class TransHistoryVersionComparatorTest
{
   @Test
   public void canCompareTwoDigitVersion()
   {
      assertThat(COMPARATOR.compare(createHistoryItem("1"), createHistoryItem("2")), Matchers.equalTo(-1));
      assertThat(COMPARATOR.compare(createHistoryItem("2"), createHistoryItem("1")), Matchers.equalTo(1));
   }

   @Test
   public void canCompareDigitToLatest()
   {
      int result = COMPARATOR.compare(createHistoryItem("1"), createHistoryItem("2 latest"));
      assertThat(result, Matchers.equalTo(-1));
   }

   @Test
   public void canCompareLatestToDigit()
   {
      int result = COMPARATOR.compare(createHistoryItem("2 latest"), createHistoryItem("1"));
      assertThat(result, Matchers.equalTo(1));
   }

   @Test
   public void canCompareCurrentToDigit()
   {
      int result = COMPARATOR.compare(createHistoryItem("current"), createHistoryItem("1"));
      assertThat(result, Matchers.equalTo(1));
   }

   @Test
   public void canCompareDigitToCurrent()
   {
      int result = COMPARATOR.compare(createHistoryItem("1"), createHistoryItem("current"));
      assertThat(result, Matchers.equalTo(-1));
   }

   @Test
   public void canCompareLatestToCurrent()
   {
      int result = COMPARATOR.compare(createHistoryItem("1 latest"), createHistoryItem("current"));
      assertThat(result, Matchers.equalTo(-1));
   }

   @Test
   public void canCompareCurrentToLatest()
   {
      int result = COMPARATOR.compare(createHistoryItem("current"), createHistoryItem("1 latest"));
      assertThat(result, Matchers.equalTo(1));
   }

   private static TransHistoryItem createHistoryItem(String versionNum)
   {
      return new TransHistoryItem(versionNum, Lists.<String> newArrayList(), ContentState.Approved, "admin", "12/12/12");
   }
}
