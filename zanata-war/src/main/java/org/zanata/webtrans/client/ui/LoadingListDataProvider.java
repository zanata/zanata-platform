package org.zanata.webtrans.client.ui;

import com.google.gwt.view.client.ListDataProvider;

public class LoadingListDataProvider<T> extends ListDataProvider<T>
{
   public void setLoading(boolean loading)
   {
      if (loading)
      {
         updateRowCount(0, false);
      }
      else
      {
         updateRowCount(getList().size(), true);
      }
   }
}
