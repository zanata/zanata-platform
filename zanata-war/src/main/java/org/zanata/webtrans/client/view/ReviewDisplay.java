package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.ui.ReviewActionPanel;
import com.google.inject.ImplementedBy;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

@ImplementedBy(ReviewView2.class)
public interface ReviewDisplay extends WidgetDisplay
{
   // FIXME rhbz953734 - remove this and ReviewView2, ReviewView.ui.xml, ReviewActionPanel
   void setListener(Listener listener);

   interface Listener
   {

   }
}
