package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.validation.ValidationService;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.validation.ValidationObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationOptionsView extends Composite implements ValidationOptionsPresenter.Display
{

   private final ValidationService validationService;
   private static ValidationOptionsViewUiBinder uiBinder = GWT.create(ValidationOptionsViewUiBinder.class);

   interface ValidationOptionsViewUiBinder extends UiBinder<Widget, ValidationOptionsView>
   {
   }

   @UiField
   VerticalPanel contentPanel;

   @Inject
   public ValidationOptionsView(final ValidationService validationService)
   {
      initWidget(uiBinder.createAndBindUi(this));
      this.validationService = validationService;

      initValidationList();

   }

   private void initValidationList()
   {
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
   public void validate(TransUnitId id, String source, String target, boolean fireNotification)
   {
      validationService.execute(id, source, target, fireNotification);
   }

   @Override
   public void clearAllMessage()
   {
      validationService.clearAllMessage();
   }
}
