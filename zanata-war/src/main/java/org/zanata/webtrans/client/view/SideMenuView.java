package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.SideMenuPresenter;
import org.zanata.webtrans.client.presenter.SideMenuPresenter.Tab;
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
import com.google.gwt.user.client.ui.LayoutPanel;
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
   InlineLabel editorOptionsTab, validationOptionsTab, chatTab;
   
   @UiField
   LayoutPanel container;
   
   private final Widget editorOptionView, validationOptionView, workspaceUsersView;

   @Inject
   public SideMenuView(final WebTransMessages messages, final EditorOptionsDisplay editorOptionView, final ValidationOptionsPresenter.Display validationOptionView, final WorkspaceUsersPresenter.Display workspaceUsersView)
   {
      initWidget(uiBinder.createAndBindUi(this));
      editorOptionsTab.setTitle(messages.editorOptions());
      validationOptionsTab.setTitle(messages.validationOptions());
      chatTab.setTitle(messages.chatRoom());
      
      this.editorOptionView = editorOptionView.asWidget();
      this.validationOptionView = validationOptionView.asWidget();
      this.workspaceUsersView = workspaceUsersView.asWidget();
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
   public void setSelectedTab(Tab tab)
   {
      editorOptionsTab.removeStyleName(style.selectedButton());
      validationOptionsTab.removeStyleName(style.selectedButton());
      chatTab.removeStyleName(style.selectedButton());
      
      container.clear();
      
      switch (tab)
      {
         case EDITOR_OPTION: 
            container.add(editorOptionView);
            editorOptionsTab.addStyleName(style.selectedButton());
            break;
         case VALIDATION_OPTION: 
            container.add(validationOptionView);
            validationOptionsTab.addStyleName(style.selectedButton());
            break;
         case CHAT: 
            container.add(workspaceUsersView);
            chatTab.addStyleName(style.selectedButton());
            setChatTabAlert(false);
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
   public HasVisibility getContainer()
   {
      return container;
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
   public Tab getCurrentTab()
   {
      if (chatTab.getStyleName().contains(style.selectedButton()))
      {
         return Tab.CHAT;
      }
      else if (editorOptionsTab.getStyleName().contains(style.selectedButton()))
      {
         return Tab.EDITOR_OPTION;
      }
      else if (validationOptionsTab.getStyleName().contains(style.selectedButton()))
      {
         return Tab.VALIDATION_OPTION;
      }
      return null;
   }
}
