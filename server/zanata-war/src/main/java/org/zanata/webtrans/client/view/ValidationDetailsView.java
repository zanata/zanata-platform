package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.ValidationDetailsPresenter;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.validation.ValidationService;
import org.zanata.webtrans.shared.validation.ValidationObject;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ValidationDetailsView extends Composite implements ValidationDetailsPresenter.Display
{

   private final ValidationService validationService;
   private static ValidationDetailsViewUiBinder uiBinder = GWT.create(ValidationDetailsViewUiBinder.class);

   private final Resources resources;
   private final NavigationMessages messages;

   interface ValidationDetailsViewUiBinder extends UiBinder<Widget, ValidationDetailsView>
   {
   }

   @UiField
   LayoutPanel rootPanel;

   @UiField
   VerticalPanel contentPanel;

   @Inject
   public ValidationDetailsView(NavigationMessages messages, Resources resources, final ValidationService validationService)
   {
      initWidget(uiBinder.createAndBindUi(this));
      this.validationService = validationService;
      this.resources = resources;
      this.messages = messages;
      refreshValidationList();

   }

   private void refreshValidationList()
   {
      contentPanel.clear();

      for (final ValidationObject action : validationService.getValidationList())
      {
         HorizontalPanel hp = new HorizontalPanel();
         hp.setSpacing(5);
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

         PushButton btn = new PushButton(new Image(resources.alertButton()));
         btn.setStyleName("gwt-Button");
         btn.setTitle(messages.clickHereForMoreInfo());

         if (action.hasError())
         {
            btn.addClickHandler(new ClickHandler()
            {
               @Override
               public void onClick(ClickEvent event)
               {
                  StringBuilder sb = new StringBuilder();
                  for (String error : action.getError())
                  {
                     sb.append(error);
                     sb.append("\n");
                  }
                  Window.alert(sb.toString());
               }
            });
            btn.setVisible(true);
         }
         else
         {
            btn.setVisible(false);
         }

         hp.add(chk);
         hp.add(btn);
         
         contentPanel.add(hp);
      }
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void validate(String source, String target)
   {
      validationService.execute(source, target);
      refreshValidationList();
   }

   @Override
   public void clearAllMessage()
   {
      validationService.clearAllMessage();
      refreshValidationList();
   }
}
