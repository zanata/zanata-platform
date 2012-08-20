package org.zanata.webtrans.client.presenter;

import java.util.Comparator;

import org.zanata.webtrans.shared.model.TransHistoryItem;

/**
* @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
*/
public enum TransHistoryVersionComparator implements Comparator<TransHistoryItem>
{
   COMPARATOR;

   @Override
   public int compare(TransHistoryItem one, TransHistoryItem two)
   {
      if (itemIsOldVersion(one) && itemIsOldVersion(two))
      {
         Integer verOne = Integer.parseInt(one.getVersionNum());
         Integer verTwo = Integer.parseInt(two.getVersionNum());
         return verOne.compareTo(verTwo);
      }
      if (itemIsCurrentValue(one))
      {
         //first is current value
         return 1;
      }
      if (itemIsLatestVersion(one) && itemIsCurrentValue(two))
      {
         //first is latest version but second is current value
         return -1;
      }
      if (itemIsLatestVersion(one) && itemIsOldVersion(two))
      {
         return 1;
      }
      //else first is old/digit version and second is not
      return -1;
   }

   private static boolean itemIsOldVersion(TransHistoryItem one)
   {
      return one.getVersionNum().matches("\\d+");
   }

   private static boolean itemIsLatestVersion(TransHistoryItem one)
   {
      //digit following non-digit characters
      return one.getVersionNum().matches("\\d+\\D+");
   }

   private static boolean itemIsCurrentValue(TransHistoryItem one)
   {
      //anything except digit
      return one.getVersionNum().matches("\\D+");
   }
}
