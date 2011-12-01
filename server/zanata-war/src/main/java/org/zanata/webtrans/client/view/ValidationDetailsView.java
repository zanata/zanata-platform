package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.ValidationDetailsPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationDetailsView extends Composite implements ValidationDetailsPresenter.Display
{

   private static ValidationDetailsViewUiBinder uiBinder = GWT.create(ValidationDetailsViewUiBinder.class);

   interface ValidationDetailsViewUiBinder extends UiBinder<Widget, ValidationDetailsView>
   {
   }

   @UiField
   LayoutPanel rootPanel;

   @UiField
   VerticalPanel contentPanel;
   
   private CheckBox htmlXML;

   @Inject
   public ValidationDetailsView(WebTransMessages messages, Resources resources)
   {
      initWidget(uiBinder.createAndBindUi(this));
      htmlXML = new CheckBox("HTML and XML");
      contentPanel.add(htmlXML);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

}
