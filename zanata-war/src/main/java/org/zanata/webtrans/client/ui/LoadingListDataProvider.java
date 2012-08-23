package org.zanata.webtrans.client.ui;

import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;

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

   public LoadingListDataProvider()
   {
      super();
   }

   public LoadingListDataProvider(ProvidesKey<T> keyProvider)
   {
      super(keyProvider);
   }
}
