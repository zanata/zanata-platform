package org.zanata.webtrans.client.view;

import java.util.Date;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.util.DateUtil;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationOptionsView extends Composite implements ValidationOptionsDisplay
{
   private static ValidationOptionsViewUiBinder uiBinder = GWT.create(ValidationOptionsViewUiBinder.class);

   interface ValidationOptionsViewUiBinder extends UiBinder<Widget, ValidationOptionsView>
   {
   }

   @UiField
   Label validationOptionsHeader;

   @UiField
   VerticalPanel contentPanel;
   
   @UiField
   PushButton runValidation;

   @UiField
   InlineLabel lastValidationRun;

   private Listener listener;

   private final WebTransMessages messages;
   

   @Inject
   public ValidationOptionsView(WebTransMessages messages)
   {
      initWidget(uiBinder.createAndBindUi(this));
      this.messages = messages;

      validationOptionsHeader.setText(messages.validationOptions());
      runValidation.setText(messages.runValidation());
   }

   @Override
   public HasValueChangeHandlers<Boolean> addValidationSelector(String label, String tooltip, boolean enabled, boolean locked)
   {
      CheckBox chk = new CheckBox(label);
      chk.setValue(enabled);
      chk.setTitle(tooltip);
      chk.setEnabled(!locked);
      contentPanel.add(chk);

      return chk;
   }

   @Override
   public void changeValidationSelectorValue(String label, boolean enabled)
   {
      for (Widget checkbox : contentPanel)
      {
         if (checkbox instanceof CheckBox && ((CheckBox) checkbox).getText().equals(label))
         {
            ((CheckBox) checkbox).setValue(enabled);
         }
      }
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void clearValidationSelector()
   {
      contentPanel.clear();
   }

   @Override
   public void setRunValidationVisible(boolean visible)
   {
      runValidation.setVisible(visible);
      lastValidationRun.setVisible(visible);
   }

   @Override
   public void setRunValidationTitle(String title)
   {
      runValidation.setTitle(title);
   }

   @UiHandler("runValidation")
   public void onRunValidationClicked(ClickEvent event)
   {
      listener.onRunValidation();
      enabledRunValidation(false);
   }
   
   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }
   
   @Override
   public void enabledRunValidation(boolean enabled)
   {
      runValidation.setEnabled(enabled);
   }

   @Override
   public void updateValidationResult(Date endTime)
   {
      if (endTime != null)
      {
         lastValidationRun.setText(messages.lastValidationRun(DateUtil.formatLongDateTime(endTime)));
      }
      else
      {
         lastValidationRun.setText(messages.lastValidationRun("none"));
      }
   }

}
