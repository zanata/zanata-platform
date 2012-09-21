package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.presenter.NotificationPresenter;
import org.zanata.webtrans.client.presenter.SideMenuPresenter;
import org.zanata.webtrans.client.presenter.ValidationOptionsPresenter;
import org.zanata.webtrans.client.presenter.WorkspaceUsersPresenter;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SideMenuView extends Composite implements SideMenuPresenter.Display
{
   private static SideMenuViewUiBinder uiBinder = GWT.create(SideMenuViewUiBinder.class);

   interface SideMenuViewUiBinder extends UiBinder<Widget, SideMenuView>
   {
   }

   interface Styles extends CssResource
   {
      String selectedButton();

      String alertTab();
   }

   @UiField
   Styles style;

   @UiField
   InlineLabel notificationTab, editorOptionsTab, validationOptionsTab, chatTab, notificationLabel;
   
   @UiField
   TabLayoutPanel container;
   
   private final Widget editorOptionView, validationOptionView, workspaceUsersView, notificationView;

   @Inject
   public SideMenuView(final WebTransMessages messages, final EditorOptionsDisplay editorOptionView, final ValidationOptionsPresenter.Display validationOptionView, final WorkspaceUsersPresenter.Display workspaceUsersView, final NotificationPresenter.Display notificationView)
   {
      initWidget(uiBinder.createAndBindUi(this));
      notificationTab.setTitle(messages.notification());
      editorOptionsTab.setTitle(messages.editorOptions());
      validationOptionsTab.setTitle(messages.validationOptions());
      chatTab.setTitle(messages.chatRoom());
      
      this.editorOptionView = editorOptionView.asWidget();
      this.validationOptionView = validationOptionView.asWidget();
      this.workspaceUsersView = workspaceUsersView.asWidget();
      this.notificationView = notificationView.asWidget();
      
      container.add(notificationView.asWidget());
      container.add(workspaceUsersView.asWidget());
      container.add(editorOptionView.asWidget());
      container.add(validationOptionView.asWidget());
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public HasClickHandlers getEditorOptionsButton()
   {
      return editorOptionsTab;
   }

   @Override
   public HasClickHandlers getNotificationButton()
   {
      return notificationTab;
   }

   @Override
   public HasClickHandlers getValidationOptionsButton()
   {
      return validationOptionsTab;
   }

   @Override
   public HasClickHandlers getChatButton()
   {
      return chatTab;
   }

   @Override
   public void setSelectedTab(int view)
   {
      editorOptionsTab.removeStyleName(style.selectedButton());
      validationOptionsTab.removeStyleName(style.selectedButton());
      chatTab.removeStyleName(style.selectedButton());
      notificationTab.removeStyleName(style.selectedButton());
      
      switch (view)
      {
         case EDITOR_OPTION_VIEW: 
            container.selectTab(EDITOR_OPTION_VIEW);
            editorOptionsTab.addStyleName(style.selectedButton());
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
   public HasVisibility getEditorOptionsTab()
   {
      return editorOptionsTab;
   }

   @Override
   public HasVisibility getValidationOptionsTab()
   {
      return validationOptionsTab;
   }

   @Override
   public HasVisibility getChatTab()
   {
      return chatTab;
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
}
