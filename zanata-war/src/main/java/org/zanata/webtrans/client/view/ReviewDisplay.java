package org.zanata.webtrans.client.view;

import org.zanata.webtrans.shared.model.TransUnit;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(ReviewView.class)
public interface ReviewDisplay extends WidgetDisplay
{
   void setSelectionModel(SelectionModel<TransUnit> multiSelectionModel);

   void setDataProvider(ListDataProvider<TransUnit> dataProvider);

   void setListener(Listener listener);

   interface Listener
   {

   }
}
