package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideMenuView extends Composite implements SideMenuDisplay
{
   private static SideMenuViewUiBinder uiBinder = GWT.create(SideMenuViewUiBinder.class);
   private Listener listener;

   interface SideMenuViewUiBinder extends UiBinder<Widget, SideMenuView>
   {
   }

   interface Styles extends CssResource
   {
      String selectedButton();

      String alertTab();

      String notificationLabel();

      String menuButton();

      String mainPanel();
   }

   @UiField
   Styles style;

   @UiField
   InlineLabel notificationTab, optionsTab, validationOptionsTab, chatTab, notificationLabel;
   
   @UiField
   TabLayoutPanel container;
   
   @Inject
   public SideMenuView(final WebTransMessages messages, final OptionsDisplay optionView, final ValidationOptionsPresenter.Display validationOptionView, final WorkspaceUsersDisplay workspaceUsersView, final NotificationDisplay notificationView)
   {
      initWidget(uiBinder.createAndBindUi(this));


      notificationTab.setTitle(messages.notification());
      optionsTab.setTitle(messages.options());
      validationOptionsTab.setTitle(messages.validationOptions());
      chatTab.setTitle(messages.chatRoom());

      container.add(notificationView.asWidget());
      container.add(workspaceUsersView.asWidget());
      container.add(optionView.asWidget());
      container.add(validationOptionView.asWidget());
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @UiHandler("optionsTab")
   public void onOptionsClick(ClickEvent event)
   {
      listener.onOptionsClick();
   }

   @UiHandler("notificationTab")
   public void onNotificationClick(ClickEvent event)
   {
      listener.onNotificationClick();
   }

   @UiHandler("validationOptionsTab")
   public void onValidationOptionsClick(ClickEvent event)
   {
      listener.onValidationOptionsClick();
   }

   @UiHandler("chatTab")
   public void onChatClick(ClickEvent event)
   {
      listener.onChatClick();
   }

   @Override
   public void setSelectedTab(int view)
   {
      optionsTab.removeStyleName(style.selectedButton());
      validationOptionsTab.removeStyleName(style.selectedButton());
      chatTab.removeStyleName(style.selectedButton());
      notificationTab.removeStyleName(style.selectedButton());
      
      switch (view)
      {
         case OPTION_VIEW: 
            container.selectTab(OPTION_VIEW);
            optionsTab.addStyleName(style.selectedButton());
            break;
         case VALIDATION_OPTION_VIEW: 
            container.selectTab(VALIDATION_OPTION_VIEW);
            validationOptionsTab.addStyleName(style.selectedButton());
            break;
         case WORKSPACEUSER_VIEW: 
            container.selectTab(WORKSPACEUSER_VIEW);
            chatTab.addStyleName(style.selectedButton());
            setChatTabAlert(false);
            break;
         case NOTIFICATION_VIEW:
            container.selectTab(NOTIFICATION_VIEW);
            notificationTab.addStyleName(style.selectedButton());
         break;
         default:
            break;
      }
   }

   @Override
   public void setChatTabAlert(boolean alert)
   {
      if(alert)
      {
         chatTab.addStyleName(style.alertTab());
      }
      else
      {
         chatTab.removeStyleName(style.alertTab());
      }
   }

   @Override
   public int getCurrentTab()
   {
      return container.getSelectedIndex();
   }

   @Override
   public void setNotificationText(int count, Severity severity)
   {
      notificationLabel.setText(String.valueOf(count));
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void setChatTabVisible(boolean visible)
   {
      chatTab.setVisible(visible);
   }

   @Override
   public void setValidationOptionsTabVisible(boolean visible)
   {
      validationOptionsTab.setVisible(visible);
   }

   @Override
   public void setOptionsTabVisible(boolean visible)
   {
      optionsTab.setVisible(visible);
   }
}
