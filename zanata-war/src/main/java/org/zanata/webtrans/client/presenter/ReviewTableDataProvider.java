package org.zanata.webtrans.client.presenter;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitProvidesKey;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class ReviewTableDataProvider extends ListDataProvider<TransUnit>
{
   public ReviewTableDataProvider()
   {
      super(TransUnitProvidesKey.KEY_PROVIDER);
   }

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
