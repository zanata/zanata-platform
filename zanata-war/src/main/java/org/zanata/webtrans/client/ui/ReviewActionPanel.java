package org.zanata.webtrans.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ReviewActionPanel extends Composite
{
   private static ReviewViewUiBinder ourUiBinder = GWT.create(ReviewViewUiBinder.class);
   private final FlowPanel root;

   @UiField
   InlineLabel acceptAllIcon;
   @UiField
   InlineLabel rejectAllIcon;

   @Inject
   public ReviewActionPanel()
   {
      root = ourUiBinder.createAndBindUi(this);

   }

   @Override
   public Widget asWidget()
   {
      return root;
   }

   interface ReviewViewUiBinder extends UiBinder<FlowPanel, ReviewActionPanel>
   {
   }
}
