package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.ValidationDetailsPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.validation.ValidationService;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.validation.ValidationObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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

   private final ValidationService validationService;
   private static ValidationDetailsViewUiBinder uiBinder = GWT.create(ValidationDetailsViewUiBinder.class);

   interface ValidationDetailsViewUiBinder extends UiBinder<Widget, ValidationDetailsView>
   {
   }

   @UiField
   LayoutPanel rootPanel;

   @UiField
   VerticalPanel contentPanel;

   @Inject
   public ValidationDetailsView(WebTransMessages messages, Resources resources, final ValidationService validationService)
   {
      initWidget(uiBinder.createAndBindUi(this));
      this.validationService = validationService;

      for (final ValidationObject action : validationService.getValidationList())
      {
         CheckBox chk = new CheckBox(action.getId());
         chk.setValue(action.isEnabled());
         chk.addValueChangeHandler(new ValueChangeHandler<Boolean>()
         {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event)
            {
               validationService.updateStatus(action.getId(), event.getValue());
            }
         });
         chk.setTitle(action.getDescription());
         contentPanel.add(chk);
      }
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void validate(TransUnit tu)
   {
      validationService.execute(tu);
   }

   @Override
   public void clearAllMessage()
   {
      validationService.clearAllMessage();
   }
}
