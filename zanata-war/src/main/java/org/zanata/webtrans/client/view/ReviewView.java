package org.zanata.webtrans.client.view;

import org.zanata.webtrans.shared.model.TransUnit;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewView extends Composite implements ReviewDisplay
{
   private static ReviewViewUiBinder ourUiBinder = GWT.create(ReviewViewUiBinder.class);
   private final FlowPanel root;

   @UiField
   InlineLabel acceptAllIcon;
   @UiField
   InlineLabel rejectAllIcon;
   private Listener listener;

   @Inject
   public ReviewView()
   {
      root = ourUiBinder.createAndBindUi(this);

   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public Widget asWidget()
   {
      return root;
   }

   interface ReviewViewUiBinder extends UiBinder<FlowPanel, ReviewView>
   {
   }
}
